/*
@author Arthur Godet <arth.godet@gmail.com>
@since 17/04/2019
*/
package constraint.order;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;

public class PropagatorEnqueue extends Propagator<IntVar> {

    private int[] processingTime;
    private int[] idResource;

    private IStateInt[] latestJobID; // per machine
    private IStateInt[] idleTime; // per machine

    private IntVar[] starts;
    private IntVar[] order;

    public PropagatorEnqueue(IntVar[] starts, IntVar[] order, int nbMachines, int[] processingTime, int[] idResource) {
        super(order, PropagatorPriority.UNARY, true); // priority is set on UNARY because PropagatorEnqueue should be the first propagator to be applied at each node
        this.starts = starts;
        this.order = order;

        this.processingTime = processingTime;
        this.idResource = idResource;

        latestJobID = new IStateInt[nbMachines];
        idleTime = new IStateInt[nbMachines];
        initIStateInt();
    }

    private void initIStateInt() {
        for (int j = 0; j < latestJobID.length; j++) {
            latestJobID[j] = this.getModel().getEnvironment().makeInt(-1);
            idleTime[j] = this.getModel().getEnvironment().makeInt(0);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.instantiation();
    }

    private int computeMachineIDProcessingJob(int idJob) { // simulates enqueue algorithm
        int res = idResource[idJob];
        int earliestIdleTime = Integer.MAX_VALUE;
        int idEarliestMachine = -1;
        for (int j = 0; j < latestJobID.length; j++) {
            if (latestJobID[j].get() != -1 && idResource[latestJobID[j].get()] == res) {
                return j;
            } else if (idleTime[j].get() < earliestIdleTime) {
                earliestIdleTime = idleTime[j].get();
                idEarliestMachine = j;
            }
        }
        return idEarliestMachine;
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        int i = order[idxVarInProp].getValue();
        int j = computeMachineIDProcessingJob(i);
        starts[i].instantiateTo(idleTime[j].get(), this);
        idleTime[j].set(starts[i].getValue() + processingTime[i]);
        latestJobID[j].set(i);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            initIStateInt();
            for (int o = 0; o < order.length && order[o].isInstantiated(); o++) {
                int i = order[o].getValue();
                int j = computeMachineIDProcessingJob(i);
                starts[i].instantiateTo(idleTime[j].get(), this);
                idleTime[j].set(starts[i].getValue() + processingTime[i]);
                latestJobID[j].set(i);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }
}
