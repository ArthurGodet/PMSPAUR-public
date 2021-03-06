package constraint.cumulative.fahimi2018;

import constraint.cumulative.DisjunctiveFilter;
import gnu.trove.list.array.TIntArrayList;
import java.util.Arrays;
import java.util.Comparator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

/**
 * Disjunctive constraint filtering algorithms described in the following paper :
 * Fahimi, H., Ouellet, Y., Quimper, C.-G.: Linear-Time Filtering Algorithms for the Disjunctive Constraint and a Quadratic Filtering Algorithm for the Cumulative Not-First Not-Last. Constraints 23(3), pages 272–293 (2018). https://doi.org/10.1007/s10601-018-9282-9
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class PropDisjunctiveFahimi2018 extends DisjunctiveFilter {
    private Integer[] Ip;

    // Useful variables for TimeTabling algorithm
    private int[] l;
    private int[] u;
    private int[] r;        

    private Timeline overloadTimeline;
    private Timeline detectPrecTimeline;

    private TIntArrayList postponedTasks;
    private int[] lstIndexes;

    public PropDisjunctiveFahimi2018(Task[] tasks, boolean overloadCheck, boolean timeTable, boolean edgeFinding) {
        super(tasks);
        this.overloadCheck = overloadCheck;
        this.timeTable = timeTable;
        this.edgeFinding = edgeFinding;

        l = new int[tasks.length];
        u = new int[tasks.length];
        r = new int[tasks.length];

        postponedTasks = new TIntArrayList();
        lstIndexes = new int[tasks.length];

        Ip = new Integer[tasks.length];
        for(int i = 0; i<Ip.length; i++) {
            Ip[i] = i;
        }
        Arrays.sort(Ip, Comparator.comparingInt(i -> tasks[i].getDuration().getLB()));

        IntVar[] heights = tasks[0].getStart().getModel().intVarArray(tasks.length, 1, 1);
        IntVar capacity = tasks[0].getStart().getModel().intVar(1);
        overloadTimeline = new Timeline(tasks, heights, capacity);
        detectPrecTimeline = new Timeline(tasks, heights, capacity);
    }

    @Override
    public void overloadCheck() throws ContradictionException {
        overloadTimeline.initializeTimeline();
        arraySort.sort(indexes, indexes.length, (i1, i2) -> Integer.compare(tasks[i1].getEnd().getUB(), tasks[i2].getEnd().getUB()));
        for(int i : indexes) {
            overloadTimeline.scheduleTask(i);
            if(overloadTimeline.earliestCompletionTime()>tasks[i].getEnd().getUB()) {
                aCause.fails();
            }
        }
    }

    @Override
    public boolean timeTable() throws ContradictionException {
        boolean hasFiltered = false;
        int m = 0;
        arraySort.sort(indexes, indexes.length, (i1, i2) -> Integer.compare(tasks[i1].getStart().getUB(), tasks[i2].getStart().getUB()));
        for(int i : indexes) {
            if(tasks[i].getStart().getUB() < tasks[i].getEnd().getLB()) {
                if(m > 0 ) {
                    if(u[m-1] > tasks[i].getStart().getUB()) {
                        aCause.fails();
                    } else {
                        hasFiltered |= tasks[i].getStart().updateLowerBound(u[m-1], aCause);
                    }
                }
                l[m] = tasks[i].getStart().getUB();
                u[m] = tasks[i].getStart().getLB()+tasks[i].getDuration().getLB();
                m++;
            }
        }

        if(m == 0) {
            return false;
        }

        int k = 0;
        arraySort.sort(indexes, indexes.length, (i1, i2) -> Integer.compare(tasks[i1].getStart().getLB(), tasks[i2].getStart().getLB()));
        for(int i : indexes) {
            while(k<m && tasks[i].getStart().getLB()>=u[k]) {
                k++;
            }
            r[i] = k;
        }

        UnionFindWithGreatest timeTablingUnion = new UnionFindWithGreatest(m);
        Arrays.sort(Ip, Comparator.comparingInt(i -> tasks[i].getDuration().getLB()));
        for(int i : Ip) {
            if(tasks[i].getEnd().getLB() <= tasks[i].getStart().getUB()) {
                int c = r[i];
                boolean firstUpdate = true;
                while(c<m && tasks[i].getStart().getLB()+tasks[i].getDuration().getLB()>l[c]) {
                    c = timeTablingUnion.findGreatest(c);
                    hasFiltered |= tasks[i].getStart().updateLowerBound(u[c], aCause);
                    if(tasks[i].getStart().getLB()+tasks[i].getDuration().getLB()>tasks[i].getEnd().getUB()) {
                        aCause.fails(); // might be useless because done by Task variable itself
                    }
                    if(!firstUpdate) {
                        timeTablingUnion.union(r[i], c);
                    }
                    firstUpdate = false;
                    c++;
                }
            }
        }
        return hasFiltered;
    }

    // test if k is before i in array
    private static Boolean before(int[] array, int k, int i) {
        for(int a : array) {
            if(a == i) {
                return false;
            } else if(a == k) {
                return true;
            }
        }
        return null;
    }

    @Override
    public boolean edgeFinding() throws ContradictionException {
        boolean hasFiltered = false;
        detectPrecTimeline.initializeTimeline();
        int j = 0;
        arraySort.sort(indexes, indexes.length, (i1, i2) -> Integer.compare(tasks[i1].getStart().getUB(), tasks[i2].getStart().getUB()));
        for(int a = 0; a< lstIndexes.length; a++) {
            lstIndexes[a] = indexes[a];
        }
        int k = lstIndexes[j];
        postponedTasks.clear();
        int blockingTask = -1;

        arraySort.sort(indexes, indexes.length, (i1, i2) -> Integer.compare(tasks[i1].getEnd().getLB(), tasks[i2].getEnd().getLB()));
        for(int i : indexes) {
            while(j<tasks.length && k!=i && tasks[k].getStart().getUB()<tasks[i].getEnd().getLB()) {
                if(tasks[k].getStart().getUB() >= tasks[k].getEnd().getLB()) {
                    detectPrecTimeline.scheduleTask(k);
                } else if(!before(indexes, k, i)) {
                    if(blockingTask != -1) {
                        aCause.fails();
                    }
                    blockingTask = k;
                }
                j++;
                if(j<lstIndexes.length) {
                    k = lstIndexes[j];
                }
            }
            if(blockingTask == -1) {
                hasFiltered |= tasks[i].getStart().updateLowerBound(detectPrecTimeline.earliestCompletionTime(), aCause);
            } else {
                if(blockingTask == i) {
                    hasFiltered |= tasks[i].getStart().updateLowerBound(detectPrecTimeline.earliestCompletionTime(), aCause);
                    detectPrecTimeline.scheduleTask(blockingTask);
                    int ect = detectPrecTimeline.earliestCompletionTime();
                    for(int z = 0; z<postponedTasks.size(); z++) {
                        hasFiltered |= tasks[postponedTasks.getQuick(z)].getStart().updateLowerBound(ect, aCause);
                    }
                    blockingTask = -1;
                    postponedTasks.clear();
                } else {
                    postponedTasks.add(i);
                }
            }
        }

        return hasFiltered;
    }
}
