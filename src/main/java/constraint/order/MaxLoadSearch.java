/*
@author Arthur Godet <arth.godet@gmail.com>
@since 05/05/2019
*/
package constraint.order;

import gnu.trove.list.array.TIntArrayList;
import java.util.Arrays;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;

public class MaxLoadSearch extends IntStrategy {

    public MaxLoadSearch(int[] processingTime, int[] idResource, int nbResources, IntVar[] order, boolean maxMaxLoad) {
        super(order, new InputOrder<>(order[0].getModel()), maxLoadValueSelector(processingTime, idResource, nbResources, order, maxMaxLoad));
    }

    private static IntValueSelector maxLoadValueSelector(int[] processingTime, int[] idResource, int nbResources, IntVar[] order, boolean maxMaxLoad) {
        TIntArrayList list = new TIntArrayList(order.length);
        int[] resourceProcSum = new int[nbResources];
        return (var -> {
            list.clear();
            for (int o = 0; o < order.length && order[o].isInstantiated(); o++) {
                list.add(order[o].getValue());
            }
            Arrays.fill(resourceProcSum, 0);
            for (int i = 0; i < processingTime.length; i++) {
                if (!list.contains(i)) {
                    resourceProcSum[idResource[i]] += processingTime[i];
                }
            }

            int val = var.getLB();
            int procSum = resourceProcSum[idResource[val]];

            for (int v = var.getLB(); v <= var.getUB(); v = var.nextValue(v)) {
                int ps = resourceProcSum[idResource[v]];
                if (ps > procSum || ps == procSum && maxMaxLoad && processingTime[v] > processingTime[val]) {
                    val = v;
                    procSum = ps;
                }
            }
            return val;
        });
    }
}
