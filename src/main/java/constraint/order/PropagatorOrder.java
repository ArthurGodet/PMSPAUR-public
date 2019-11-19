/*
@author Arthur Godet <arth.godet@gmail.com>
@since 17/07/2019
*/
package constraint.order;

import java.util.Arrays;
import java.util.OptionalInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

public class PropagatorOrder extends Propagator<IntVar> {

    private IntVar[] order;
    private IntVar[] starts;

    public PropagatorOrder(IntVar[] starts, IntVar[] order) {
        super(ArrayUtils.append(order, starts), PropagatorPriority.VERY_SLOW,
            false); // priority is set to VERY_SLOW because it should be applied as late as possible during the filtering process as it is mostly based on starts variables' domains
        this.starts = starts;
        this.order = order;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx < order.length) {
            return IntEventType.instantiation();
        } else {
            return IntEventType.INCLOW.getMask();
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int idxCurrentOrder = 0;
        while (idxCurrentOrder < order.length && order[idxCurrentOrder].isInstantiated()
            && starts[order[idxCurrentOrder].getValue()].isInstantiated()) {
            idxCurrentOrder++;
        }

        if (idxCurrentOrder != order.length) {
            OptionalInt optT = Arrays.stream(starts).filter(s -> !s.isInstantiated()).mapToInt(IntVar::getLB).min();
            if (optT.isPresent()) {
                int t = optT.getAsInt();
                for (int i = 0; i < starts.length; i++) {
                    if (!starts[i].isInstantiated() && starts[i].getLB() > t) {
                        order[idxCurrentOrder].removeValue(i, this);
                    }
                }
            }
        }
    }
}
