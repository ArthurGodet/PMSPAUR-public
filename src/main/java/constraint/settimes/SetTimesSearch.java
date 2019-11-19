/*
@author Arthur Godet <arth.godet@gmail.com>
@since 31/10/2019
*/
package constraint.settimes;

import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;

public class SetTimesSearch extends IntStrategy {

    public SetTimesSearch(IntVar[] order, IntVar[] starts) {
        super(order, new InputOrder<>(order[0].getModel()), smallestStartValueSelector(starts));
    }

    private static IntValueSelector smallestStartValueSelector(IntVar[] starts) {
        return var -> {
            int smallest = Integer.MAX_VALUE;
            int idx = -1;
            for (int v : var) {
                if (starts[v].getLB() < smallest) {
                    smallest = starts[v].getLB();
                    idx = v;
                }
            }
            return idx;
        };
    }

}
