/*
@author Arthur Godet <arth.godet@gmail.com>
@since 30/10/2019
*/
package constraint.settimes;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorUpBranch;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

public class SetTimesFirst extends Propagator<IntVar> implements IMonitorUpBranch {

    private IntVar[] order;
    private IntVar[] starts;

    public SetTimesFirst(IntVar[] order, IntVar[] starts) {
        super(order, PropagatorPriority.UNARY, false);
        this.order = order;
        this.starts = starts;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int idxCurrentOrder = 0;
        while (idxCurrentOrder < order.length && order[idxCurrentOrder].isInstantiated()) {
            starts[order[idxCurrentOrder].getValue()].instantiateTo(starts[order[idxCurrentOrder].getValue()].getLB(), this);
            idxCurrentOrder++;
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }
}
