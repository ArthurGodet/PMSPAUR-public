package constraint.cumulative.fahimi2018;

import java.util.Arrays;
import java.util.Comparator;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

/**
 * Data structure for cumulative and disjunctive constraints, described in the following paper :
 * Fahimi, H., Ouellet, Y., Quimper, C.-G.: Linear-Time Filtering Algorithms for the Disjunctive Constraint and a Quadratic Filtering Algorithm for the Cumulative Not-First Not-Last. Constraints 23(3), pages 272–293 (2018). https://doi.org/10.1007/s10601-018-9282-9
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class Timeline {
    private Task[] tasks;
    private IntVar[] heights;
    private int capacity;

    // Useful variables for Timeline data structure (and so, useful for Overload check and Detectable Precedences algorithms)
    private int[] m;
    private int[] t;
    private int[] c;
    private int tSize;
    private int cSize;
    private int e;
    private UnionFindWithGreatest timelineUnion;
    private final int sumP;
    private final int maxiP;
    private final int min;
    private Integer[] indexes;

    public Timeline(Task[] tasks, IntVar[] heights, IntVar capacity) {
        this.tasks = tasks;
        this.heights = heights;
        this.capacity = capacity.getUB();
        t = new int[tasks.length+1];
        c = new int[tasks.length];
        m = new int[tasks.length];
        this.indexes = new Integer[tasks.length];
        int tmp = tasks[0].getStart().getLB();
        int tmpSum = 0;
        int tmpMax = tasks[0].getEnd().getUB()*this.capacity;
        for(int i = 0; i<tasks.length; i++) {
            indexes[i] = i;
            tmp = Math.min(tmp, this.capacity*tasks[i].getStart().getLB());
            tmpSum += tasks[i].getDuration().getLB()*heights[i].getLB();
            tmpMax = Math.max(tmpMax, tasks[i].getEnd().getUB()*this.capacity);
        }
        min = tmp;
        sumP = tmpSum;
        maxiP = tmpMax;
        timelineUnion = new UnionFindWithGreatest(tasks.length);
    }

    public void initializeTimeline() {
        tSize = 0;
        cSize = 0;
        Arrays.sort(indexes, Comparator.comparingInt(i -> tasks[i].getStart().getLB()));
        for(int i : indexes) {
            if(tSize==0 || t[tSize-1]!=capacity*tasks[i].getStart().getLB()) {
                t[tSize++] = capacity*tasks[i].getStart().getLB();
            }
            m[i] = tSize-1;
        }
        t[tSize++] = maxiP+sumP;
        for(int k = 0; k<tSize-1; k++) {
            c[cSize++] = t[k+1]-t[k];
        }

        timelineUnion.reset(tSize);
        e = -1;
    }

    public void scheduleTask(int i) {
        int rho = tasks[i].getDuration().getLB()*heights[i].getLB();
        int k = timelineUnion.findGreatest(m[i]);
        while(rho > 0 && k<cSize) {
            int delta = Math.min(c[k], rho);
            rho -= delta;
            c[k] -= delta;
            if(c[k] == 0) {
                timelineUnion.union(k, k+1);
                k = timelineUnion.findGreatest(k);
            }
        }
        e = Math.max(e, k);
    }

    public int earliestCompletionTime() {
        if(e == -1) {
            return min;
        } else if(e == cSize) {
            return t[e];
        }
        return t[e+1]-c[e];
    }

    public int getSize() {
        return tSize;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
