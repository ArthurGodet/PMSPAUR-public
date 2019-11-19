/*
@author Arthur Godet <arth.godet@gmail.com>
@since 17/04/2019
*/
package constraint.order;

import java.util.Arrays;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;

public class PropagatorRules extends Propagator<IntVar> {

    private int[] processingTime;
    private int[] idResource;
    private ISet[] resources;
    private int[] resourceProcSum;
    private int m;

    private IntVar[] order;
    private IntVar[] starts;

    public PropagatorRules(IntVar[] order, IntVar[] starts, int nbMachines, int nbResources, int[] processingTime, int[] idResource) {
        super(ArrayUtils.append(order, starts), PropagatorPriority.LINEAR, true);
        this.order = order;
        this.starts = starts;

        this.m = nbMachines;
        this.processingTime = processingTime;
        this.idResource = idResource;

        resources = new ISet[nbResources];
        resourceProcSum = new int[resources.length];
        for (int r = 0; r < resources.length; r++) {
            resources[r] = SetFactory.makeStoredSet(SetType.LINKED_LIST, 0, model);
        }

        for (int i = 0; i < idResource.length; i++) {
            resources[idResource[i]].add(i);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.instantiation();
    }

    private int sumOtherResource(int resID, int emin) {
        int sum = 0;
        for (int r = 0; r < resources.length; r++) {
            if (r != resID) {
                sum += computeLrj(r, emin);
            }
        }
        return sum;
    }

    private int pMax(int resID) {
        int pmax = -1;
        for (int r = 0; r < resources.length; r++) {
            if (r != resID) {
                for (int i : resources[r]) {
                    pmax = Math.max(pmax, processingTime[i]);
                }
            }
        }
        return pmax;
    }

    private int computeLrj(int r, int emin) {
        int lrj = resourceProcSum[r];
        for (int o = 0; o < order.length && order[o].isInstantiated(); o++) {
            int i = order[o].getValue();
            if (idResource[i] == r && starts[i].getValue() + processingTime[i] >= emin) {
                lrj += starts[i].getValue() + processingTime[i] - emin;
            }
        }
        return lrj;
    }

    private void computeResProcSum() {
        for (int r = 0; r < resourceProcSum.length; r++) {
            resourceProcSum[r] = 0;
            for (int i : resources[r]) {
                resourceProcSum[r] += processingTime[i];
            }
        }
    }

    private int getBiggestResource() {
        int biggestResource = 0;
        int resourceLoad = resourceProcSum[biggestResource];
        for (int r = 1; r < resources.length; r++) {
            if (resourceLoad < resourceProcSum[r]) {
                biggestResource = r;
                resourceLoad = resourceProcSum[biggestResource];
            }
        }
        return biggestResource;
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < order.length) {
            resources[idResource[order[idxVarInProp].getValue()]].remove(order[idxVarInProp].getValue());
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            for (int o = 0; o < order.length && order[o].isInstantiated(); o++) {
                resources[idResource[order[o].getValue()]].remove(order[o].getValue());
            }
        }
        int emptyResources = (int) Arrays.stream(resources).filter(ISet::isEmpty).count();
        computeResProcSum();

        int emin = Integer.MAX_VALUE;
        for (int i = 0; i < order.length && order[i].isInstantiated(); i++) {
            emin = Math.min(emin, starts[order[i].getValue()].getValue());
        }

        int biggestResource = getBiggestResource();
        int LRj = computeLrj(biggestResource, emin);
        int otherLRj = sumOtherResource(biggestResource, emin);
        int pmax = pMax(biggestResource);

        if (resources.length - emptyResources <= m
            || LRj >= (2 * otherLRj / m)
            || LRj >= ((m - 2) * pmax + otherLRj) / (m - 1)
        ) {
            int firstIdxAvailable = 0;
            for (int i = 0; i < order.length && order[i].isInstantiated(); i++) {
                firstIdxAvailable = i + 1;
            }
            while (emptyResources != resources.length && firstIdxAvailable < order.length) { // apply maxLoad to get optimal schedule from here
                biggestResource = getBiggestResource();
                if (resourceProcSum[biggestResource] == 0) {
                    emptyResources++;
                } else {
                    int i = resources[biggestResource].min();
                    resources[biggestResource].remove(i);
                    order[firstIdxAvailable++].instantiateTo(i, this);
                    resourceProcSum[biggestResource] -= processingTime[i];
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }
}
