/*
@author Arthur Godet <arth.godet@gmail.com>
@since 03/05/2019
*/
package bench;

import constraint.PMSPAURModel;

public enum Configuration {
    Smallest("results/startsOnly.csv", PMSPAURModel.NAIVE),
    SetTimes("results/setTimes.csv", PMSPAURModel.SET_TIMES),
    Bool("results/bool.csv", PMSPAURModel.NAIVE),
    Order("results/order.csv", PMSPAURModel.NAIVE),
    OrderA("results/orderA.csv", PMSPAURModel.NAIVE),
    OrderAM("results/orderAM.csv", PMSPAURModel.MAXLOAD);

    // format for parameters : new boolean[][]{new boolean[]{withGlobalCumulative, withBooleanVars}, new boolean[]{withOrder, withPropagatorRules}}
    private static final boolean[][] CONFIGS_SMALLEST = new boolean[][]{new boolean[]{true, false}, new boolean[]{false, false}};
    private static final boolean[][] CONFIGS_BOOL = new boolean[][]{new boolean[]{true, true}, new boolean[]{false, false}};
    private static final boolean[][] CONFIGS_ORDER = new boolean[][]{new boolean[]{true, false}, new boolean[]{true, false}};
    private static final boolean[][] CONFIGS_ORDER_A = new boolean[][]{new boolean[]{true, false}, new boolean[]{true, true}};

    static {
        Smallest.parameters = CONFIGS_SMALLEST;
        SetTimes.parameters = CONFIGS_SMALLEST;

        Bool.parameters = CONFIGS_BOOL;

        Order.parameters = CONFIGS_ORDER;
        OrderA.parameters = CONFIGS_ORDER_A;
        OrderAM.parameters = CONFIGS_ORDER_A;
    }

    private final String path;
    private final int search;
    private boolean[][] parameters;

    Configuration(String path, int search) {
        this.path = path;
        this.search = search;
    }

    public String getPath() {
        return this.path;
    }

    public boolean[][] getParameters() {
        return this.parameters;
    }

    public int getSearch() {
        return search;
    }
}
