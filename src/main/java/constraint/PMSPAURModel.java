/*
@author Arthur Godet <arth.godet@gmail.com>
@since 22/02/2019
*/
package constraint;

import constraint.cumulative.CumulativeConstraintFactory;
import constraint.cumulative.CumulativeFilter;
import constraint.cumulative.DisjunctiveFilter;
import constraint.cumulative.PropagatorCumulative;
import constraint.cumulative.PropagatorDisjunctive;
import constraint.order.MaxLoadSearch;
import constraint.order.PropagatorEnqueue;
import constraint.order.PropagatorOrder;
import constraint.order.PropagatorRules;
import constraint.settimes.SetTimesFirst;
import constraint.settimes.SetTimesLast;
import constraint.settimes.SetTimesSearch;
import data.Factory;
import data.input.Instance;
import data.input.Job;
import data.input.Resource;
import data.output.SolutionPMSPAUR;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.Smallest;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.tools.ArrayUtils;

public class PMSPAURModel {

    public static final int NAIVE = 0;
    public static final int SET_TIMES = 1;
    public static final int MAXLOAD = 2;

    public static final boolean CHOCO_CUMULATIVE = false;

    private Instance instance;
    private Model model;
    private IntVar C;
    private IntVar[] starts;
    private BoolVar[][] matrix;
    private IntVar[] order;

    public PMSPAURModel(Instance inst, boolean withGlobalCumulative, boolean withBooleanVars, boolean[] withOrder, int search) {
        if (withOrder != null) {
            if (withOrder.length != 2) {
                throw new UnsupportedOperationException("withOrder should be of size 2");
            } else if (!withGlobalCumulative && !(withOrder[0] || withOrder[1])) {
                throw new UnsupportedOperationException("withGlobalCumulative should be true if at least one withOrder is true");
            }
        }

        this.instance = inst;
        int L = inst.getL();
        int n = inst.getJobs().size();
        int m = inst.getNbMachines();

        this.model = new Model();
        this.starts = new IntVar[n]; // instant when the job starts
        IntVar[] ends = new IntVar[n];
        for (int i = 0; i < n; i++) { // initializes starts and ends variables
            Job job = inst.getJob(i);
            starts[i] = model.intVar("starts[" + i + "]", 0, L - job.getProcTime());
            ends[i] = model.intOffsetView(starts[i], job.getProcTime());
        }

        LinkedList<Resource> res = Resource.buildListResources(inst);
        int maxR = 0;
        // jobs of the same resource cannot be planned on overlapping time windows
        for (Resource r : res) {
            maxR = Math.max(maxR, r.getProcSum());
            int size = r.getJobs().size();
            Task[] tasks = new Task[size];
            for (int k = 0; k < size; k++) {
                Job job = r.getJobs().get(k);
                tasks[k] = new Task(starts[job.getID()], model.intVar(job.getProcTime()), ends[job.getID()]);
            }
            if (!CHOCO_CUMULATIVE) {
                declareDisjunctives(tasks);
            } else {
                model.cumulative(tasks, model.intVarArray(size, 1, 1), model.intVar(1), true, Cumulative.Filter.DEFAULT.make(n)).post();
            }
        }

        // Objective variable is makespan
        this.C = model.intVar("C", Math.max(maxR, L / m), L);
        model.max(C, ends).post();
        model.setObjective(false, C);

        if (withBooleanVars) {
            this.matrix = model.boolVarMatrix("matrix", n, m);
            for (int i = 0; i < n; i++) {
                model.sum(matrix[i], "=", 1).post(); // each job is done on one machine only
            }

            // Creates Task variables for each job and each machine
            for (int j = 0; j < m; j++) {
                Task[] tasks = new Task[n];
                IntVar[] heights = new IntVar[n];
                for (int i = 0; i < n; i++) {
                    tasks[i] = new Task(starts[i], model.intVar(inst.getJob(i).getProcTime()), ends[i]);
                    heights[i] = matrix[i][j];
                }
                if (!CHOCO_CUMULATIVE) {
                    declareCumulatives(tasks, heights, 1);
                } else {
                    model.cumulative(tasks, heights, model.intVar(1), true, Cumulative.Filter.DEFAULT.make(n)).post();
                }
            }
        } else if (withGlobalCumulative) { // Adding a global cumulative constraint helps for filtering when using boolean variables and is mandatory otherwise
            Task[] tasksGlob = new Task[n];
            IntVar[] heightsGlob = model.intVarArray(n, 1, 1);
            for (int i = 0; i < n; i++) {
                tasksGlob[i] = new Task(starts[i], model.intVar(inst.getJob(i).getProcTime()), ends[i]);
            }
            if (!CHOCO_CUMULATIVE) {
                declareCumulatives(tasksGlob, heightsGlob, m);
            } else {
                model.cumulative(tasksGlob, heightsGlob, model.intVar(m), true, Cumulative.Filter.DEFAULT.make(tasksGlob.length)).post();
            }
        } else {
            throw new UnsupportedOperationException("globalCumulative should be true if boolean variables are not used");
        }
        if (withOrder != null && withOrder[0]) {
            this.order = model.intVarArray("order", n, 0, n - 1);
            model.allDifferent(order).post();
            int[] processingTime = instance.getJobs().stream().mapToInt(Job::getProcTime).toArray();
            int[] idResource = instance.getJobs().stream().mapToInt(Job::getResourceID).toArray();
            model.post(new Constraint("EnqueueCstr", new PropagatorEnqueue(starts, order, m, processingTime, idResource), new PropagatorOrder(starts, order)));
            if (withOrder[1]) {
                model.post(new Constraint("RulesCstr", new PropagatorRules(order, starts, m, instance.getListResources().size(), processingTime, idResource)));
            }

            if (search == NAIVE) {
                model.getSolver().setSearch(Search.inputOrderLBSearch(order));
            } else {
                model.getSolver().setSearch(new MaxLoadSearch(processingTime, idResource, instance.getListResources().size(), order, true));
            }
        } else {
            setSearch(search);
        }
    }

    private void declareDisjunctives(Task[] tasks) {
        ArrayList<DisjunctiveFilter[]> list = new ArrayList<>();
        list.add(CumulativeConstraintFactory.fahimi2018(tasks, true, true, true));

        model.post(new Constraint(ConstraintsName.CUMULATIVE,
            new PropagatorDisjunctive(tasks, ArrayUtils.flatten(list.toArray(new DisjunctiveFilter[][]{})))));
    }

    private void declareCumulatives(Task[] tasks, IntVar[] heights, int cap) {
        boolean[][] selectors = new boolean[][]{
            new boolean[]{true}, // Vilim
            new boolean[]{true, false, false}, // OuelletQuimper
            new boolean[]{true, true}, // Fahimi
        };
        declareCumulatives(tasks, heights, cap, selectors);
    }

    private void declareCumulatives(Task[] tasks, IntVar[] heights, int cap, boolean[][] selectors) {
        ArrayList<CumulativeFilter[]> list = new ArrayList<>();
        if (selectors[0][0]) {
            list.add(CumulativeConstraintFactory.vilim2009(tasks, heights, model.intVar(cap), true));
        }
        if (selectors[1][0] || selectors[1][1] || selectors[1][2]) {
            list.add(CumulativeConstraintFactory.ouelletQuimper2013(tasks, heights, model.intVar(cap), selectors[1][0], selectors[1][1], selectors[1][2]));
        }
        if (selectors[2][0] || selectors[2][1]) {
            list.add(CumulativeConstraintFactory.fahimi2018(tasks, heights, model.intVar(cap), selectors[2][0], selectors[2][1]));
        }

        if (!list.isEmpty()) {
            model.post(new Constraint(ConstraintsName.CUMULATIVE,
                new PropagatorCumulative(tasks, heights, model.intVar(cap), ArrayUtils.flatten(list.toArray(new CumulativeFilter[][]{})))));
        }
    }

    public Instance getInstance() {
        return this.instance;
    }

    public Model getModel() {
        return this.model;
    }

    public IntVar getC() {
        return this.C;
    }

    public IntVar[] getStarts() {
        return this.starts;
    }

    public BoolVar[][] getMatrix() {
        return this.matrix;
    }

    public IntVar[] getOrder() {
        return this.order;
    }

    private void setSearch(int search) {
        if (matrix != null) {
            model.getSolver().setSearch(
                Search.inputOrderLBSearch(ArrayUtils.flatten(matrix)),
                Search.intVarSearch(new Smallest(), new IntDomainMin(), starts)
            );
        } else if (search == NAIVE) {
            model.getSolver().setSearch(Search.intVarSearch(new Smallest(), new IntDomainMin(), starts));
        } else if (search == SET_TIMES) {
            IntVar[] order = model.intVarArray("order", starts.length, 0, starts.length - 1);
            model.post(new Constraint("SetTimes", new SetTimesFirst(order, starts), new SetTimesLast(order, starts)));
            model.getSolver().setSearch(new SetTimesSearch(order, starts));
        }
    }

    public Object[] solve(int nbMinutes) {
        return solve(nbMinutes, false, false);
    }

    public Object[] solve(int nbMinutes, boolean showStatistics, boolean showSolutions) {
        if (showStatistics) {
            model.getSolver().showStatistics();
        }
        if (showSolutions) {
            model.getSolver().showSolutions();
        }
        model.getSolver().limitTime(nbMinutes + "m");

        Solution sol = new Solution(model);
        long timeToBestSolution = 0;
        boolean foundSolution = false;

        while (model.getSolver().solve()) {
            sol.record();
            foundSolution = true;
            timeToBestSolution = model.getSolver().getTimeCountInNanoSeconds();
            System.out.println(timeToBestSolution / 1000000 + ";"
                + sol.getIntVal(getC()) + ";"
                + model.getSolver().getNodeCount() + ";"
                + model.getSolver().getBackTrackCount() + ";"
                + model.getSolver().getFailCount() + ";");
        }
        return new Object[]{(foundSolution ? sol : null), timeToBestSolution, model.getSolver().getTimeCountInNanoSeconds()};
    }

    public SolutionPMSPAUR buildSolution(Solution chocoSol) {
        if (matrix != null) {
            SolutionPMSPAUR sol = new SolutionPMSPAUR(instance);
            for (int j = 0; j < instance.getNbMachines(); j++) {
                List<Integer> list = new ArrayList<>();
                for (int id = 0; id < instance.getJobs().size(); id++) {
                    if (chocoSol.getIntVal(matrix[id][j]) == 1) { // if job id is done on machine n°j
                        list.add(id);
                    }
                }
                list.sort(Comparator.comparingInt(id -> chocoSol.getIntVal(starts[id])));
                for (int id : list) {
                    sol.add(j, instance.getJob(id));
                }
            }
            return sol;
        } else {
            // TODO see how to build a PMSPAURSolution from the model without boolean variables
            return null;
        }
    }

    public static void main(String[] args) {
        int nbMinutes = 30; // 30 min

        Instance inst = Factory.fromFile("data/2_3/2_3_RANDOM_20_100.json", Instance.class);
        boolean[] withOrder = new boolean[]{false, false};
        PMSPAURModel pmspaurModel = new PMSPAURModel(inst, true, false, withOrder, SET_TIMES);
        Object[] res = pmspaurModel.solve(nbMinutes, false, false);
        SolutionPMSPAUR sol = pmspaurModel.buildSolution((Solution) res[0]);
    }

}
