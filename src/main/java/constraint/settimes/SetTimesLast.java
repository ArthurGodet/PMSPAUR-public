/*
@author Arthur Godet <arth.godet@gmail.com>
@since 31/10/2019
*/
package constraint.settimes;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorUpBranch;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

public class SetTimesLast extends Propagator<IntVar> implements IMonitorUpBranch {

    private int n;
    private IntVar[] order;
    private IntVar[] starts;

    private IStateInt[] formerStart;
    private IStateInt[] idxOrderLastTry;

    public SetTimesLast(IntVar[] order, IntVar[] starts) {
        super(order, PropagatorPriority.VERY_SLOW, false);
        this.order = order;
        this.starts = starts;
        this.n = order.length;
        getModel().getSolver().plugMonitor(this);

        formerStart = new IStateInt[n];
        idxOrderLastTry = new IStateInt[n];
        for (int i = 0; i < n; i++) {
            formerStart[i] = this.getModel().getEnvironment().makeInt(-1);
            idxOrderLastTry[i] = this.getModel().getEnvironment().makeInt(-1);
        }
    }

    private static <T> int indexOf(T[] array, T element) {
        for (int i = 0; i < array.length; i++) {
            if (element.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public void beforeUpBranch() {
        if (getModel().getSolver().getDecisionPath().size() > 1) {
            IntDecision dec = (IntDecision) getModel().getSolver().getDecisionPath().getLastDecision();
            if (dec.hasNext()) {
                int idx = indexOf(order, dec.getDecisionVariable());
                int var = dec.getDecisionValue();
                idxOrderLastTry[var].set(idx);
                formerStart[var].set(starts[var].getValue());
            }
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int idxCurrentOrder = 0;
        while (idxCurrentOrder < order.length && order[idxCurrentOrder].isInstantiated()) {
            idxCurrentOrder++;
        }

        if (idxCurrentOrder == n) {
            return;
        }

        for (int i = 0; i < idxCurrentOrder; i++) {
            order[idxCurrentOrder].removeValue(order[i].getValue(), this);
        }

        for (int i = 0; i < n; i++) {
            if (!starts[i].isInstantiated() && formerStart[i].get() >= 0 && idxOrderLastTry[i].get() <= idxCurrentOrder) {
                if (formerStart[i].get() == starts[i].getLB()) {
                    order[idxCurrentOrder].removeValue(i, this);
                } else if (starts[i].getLB() > formerStart[i].get()) {
                    formerStart[i].set(-1);
                    idxOrderLastTry[i].set(-1);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }
}
