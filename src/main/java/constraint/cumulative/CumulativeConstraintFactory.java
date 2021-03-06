package constraint.cumulative;

import constraint.cumulative.fahimi2018.PropCumulativeFahimi2018;
import constraint.cumulative.fahimi2018.PropDisjunctiveFahimi2018;
import constraint.cumulative.ouelletQuimper2013.PropCumulativeOuelletQuimper2013;
import constraint.cumulative.vilim2009.PropCumulativeVilim2009;
import constraint.cumulative.vilim2009.PropDisjunctiveVilim2009;
import java.util.ArrayList;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

/**
 * Factory for literature filtering algorithms for the cumulative constraint.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class CumulativeConstraintFactory {
    @FunctionalInterface
    private interface DeclareCumulativeConstraint {
        CumulativeFilter[] call(Task[] tasks, IntVar[] heights);
    }

    @FunctionalInterface
    private interface DeclareDisjunctiveConstraint {
        DisjunctiveFilter[] call(Task[] tasks);
    }

    private static Task[] oppTasks(Task[] tasks) {
        Task[] oppTasks = new Task[tasks.length];
        for(int i = 0; i<tasks.length; i++) {
            oppTasks[i] = new Task(tasks[i].getEnd().neg().intVar(), tasks[i].getDuration(), tasks[i].getStart().neg().intVar());
        }
        return oppTasks;
    }

    private static CumulativeFilter[] checker(Task[] tasks, IntVar[] heights, DeclareCumulativeConstraint dc) {
        ArrayList<Task> taskList = new ArrayList<>(tasks.length);
        ArrayList<IntVar> heightsList = new ArrayList<>(heights.length);

        for(int i = 0; i<tasks.length; i++) {
            if(heights[i].getUB()>0 && tasks[i].getDuration().getUB()>0) {
                taskList.add(tasks[i]);
                heightsList.add(heights[i]);
            }
        }

        Task[] T2 = taskList.toArray(new Task[]{});
        IntVar[] H2 = heightsList.toArray(new IntVar[]{});

        return dc.call(T2, H2);
    }

    private static DisjunctiveFilter[] checker(Task[] tasks, DeclareDisjunctiveConstraint dc) {
        ArrayList<Task> taskList = new ArrayList<>(tasks.length);

        for(Task t : tasks) {
            if(t.getDuration().getUB()>0) {
                taskList.add(t);
            }
        }

        Task[] T2 = taskList.toArray(new Task[]{});

        return dc.call(T2);
    }

    /**
     * Declares filtering algorithms for the cumulative constraint as described in :
     * Fahimi, H., Ouellet, Y., Quimper, C.-G.: Linear-Time Filtering Algorithms for the Disjunctive Constraint and a Quadratic Filtering Algorithm for the Cumulative Not-First Not-Last. Constraints 23(3), pages 272–293 (2018). https://doi.org/10.1007/s10601-018-9282-9
     *
     * @param tasks			task variables (embed start, duration and end variables)
     * @param heights		height variables (represent the consumption of each task on the resource)
     * @param capacity		maximal capacity of the resource (same at each point in time)
     * @param overloadCheck <i>true</i> if the Overload Checking should be applied
     * @param notFirst <i>true</i> if the not-first/not-last rule should be applied
     * @return the filtering algorithms
     */
    public static CumulativeFilter[] fahimi2018(Task[] tasks, IntVar[] heights, IntVar capacity, boolean overloadCheck, boolean notFirst) {
        return checker(tasks, heights,
                (t,h) -> new CumulativeFilter[]{
                        new PropCumulativeFahimi2018(t, h, capacity, overloadCheck, notFirst),
                        new PropCumulativeFahimi2018(oppTasks(t), h, capacity, overloadCheck, notFirst)});
    }

    /**
     * Declares filtering algorithms for the disjunctive constraint as described in :
     * Fahimi, H., Ouellet, Y., Quimper, C.-G.: Linear-Time Filtering Algorithms for the Disjunctive Constraint and a Quadratic Filtering Algorithm for the Cumulative Not-First Not-Last. Constraints 23(3), pages 272–293 (2018). https://doi.org/10.1007/s10601-018-9282-9
     *
     * @param tasks			task variables (embed start, duration and end variables)
     * @param overloadCheck <i>true</i> if the Overload Checking should be applied
     * @param timeTable <i>true</i> if the Timetabling rule should be applied
     * @param edgeFinding <i>true</i> if the Edge-Finding rule should be applied
     * @return the filtering algorithms
     */
    public static DisjunctiveFilter[] fahimi2018(Task[] tasks, boolean overloadCheck, boolean timeTable, boolean edgeFinding) {
        return checker(tasks,
                (t) -> new DisjunctiveFilter[]{
                        new PropDisjunctiveFahimi2018(t, overloadCheck, timeTable, edgeFinding),
                        new PropDisjunctiveFahimi2018(oppTasks(t), overloadCheck, timeTable, edgeFinding)});
    }

    /**
     * Declares filtering algorithms for the cumulative constraint as described in :
     * Vilim, P.: Edge finding filtering algorithm for discrete cumulative resources in O(k n log(n)). In: Proceedings of the 15th International Conference on Principles and Practice of Constraint Programming (CP 2009), pp. 802-816 (2009). https://doi.org/10.1007/978-3-642-04244-7_62
     *
     * @param tasks			task variables (embed start, duration and end variables)
     * @param heights		height variables (represent the consumption of each task on the resource)
     * @param capacity		maximal capacity of the resource (same at each point in time)
     * @param edgeFinding <i>true</i> if the Edge-Finding rule should be applied
     * @return the filtering algorithms
     */
    public static CumulativeFilter[] vilim2009(Task[] tasks, IntVar[] heights, IntVar capacity, boolean edgeFinding) {
        return checker(tasks, heights,
                (t,h) -> new CumulativeFilter[]{
                        new PropCumulativeVilim2009(t, h, capacity, edgeFinding),
                        new PropCumulativeVilim2009(oppTasks(t), h, capacity, edgeFinding)});
    }

    /**
     * Declares filtering algorithms for the disjunctive constraint as described in :
     * Vilim, P.: Global constraints in scheduling. Ph.D. thesis, Charles University in Prague (2007). http://vilim.eu/petr/disertace.pdf
     *
     * @param tasks			task variables (embed start, duration and end variables)
     * @param overloadCheck <i>true</i> if the Overload Checking should be applied
     * @param notFirst <i>true</i> if the not-first/not-last rule should be applied
     * @param edgeFinding <i>true</i> if the Edge-Finding rule should be applied
     * @return the filtering algorithms
     */
    public static DisjunctiveFilter[] vilim2009(Task[] tasks, boolean overloadCheck, boolean notFirst, boolean edgeFinding) {
        return checker(tasks,
                (t) -> new DisjunctiveFilter[]{
                        new PropDisjunctiveVilim2009(t, overloadCheck, notFirst, edgeFinding),
                        new PropDisjunctiveVilim2009(oppTasks(t), overloadCheck, notFirst, edgeFinding)});
    }

    /**
     * Declares filtering algorithms for the cumulative constraint as described in :
     * Ouellet, P., Quimper, C.-G.: Time-table-extended-edge-finding for the cumulative constraint. In: Proceedings of the 19th International Conference on Principles and Practice of Constraint Programming (CP 2013), pp. 562-577 (2013). https://doi.org/10.1007/978-3-642-40627-0_42
     *
     * @param tasks			task variables (embed start, duration and end variables)
     * @param heights		height variables (represent the consumption of each task on the resource)
     * @param capacity		maximal capacity of the resource (same at each point in time)
     * @param timeTable <i>true</i> if the Timetabling rule should be applied
     * @param edgeFinding <i>true</i> if the Edge-Finding rule should be applied
     * @param timetableExtendedEdgeFinding <i>true</i> if the Timetable-Extended-Edge-Finding rule should be applied
     * @return the filtering algorithms
     */
    public static CumulativeFilter[] ouelletQuimper2013(Task[] tasks, IntVar[] heights, IntVar capacity, boolean timeTable, boolean edgeFinding, boolean timetableExtendedEdgeFinding) {
        return checker(tasks, heights,
                (t,h) -> new CumulativeFilter[]{
                        new PropCumulativeOuelletQuimper2013(t, h, capacity, timeTable, edgeFinding, timetableExtendedEdgeFinding),
                        new PropCumulativeOuelletQuimper2013(oppTasks(t), h, capacity, timeTable, edgeFinding, timetableExtendedEdgeFinding)});
    }
}
