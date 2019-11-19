package data.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;

public class Instance {

    private String name;
    private List<Job> jobs;
    private int nbMachines; // number of machines
    private Integer L;

    @JsonIgnore
    private LinkedList<Resource> listResources;

    @JsonCreator
    public Instance(
        @JsonProperty("name") String name,
        @JsonProperty("jobs") List<Job> jobs,
        @JsonProperty("nbMachines") int nbMachines) {
        this.name = name;
        this.jobs = jobs;
        this.nbMachines = nbMachines;
        this.listResources = Resource.buildListResources(this);
    }

    private static int[][] example() {
        int[][] jobsR = new int[4][];
        jobsR[0] = new int[]{10, 7, 5, 3, 1};
        jobsR[1] = new int[]{9, 6, 4, 2};
        jobsR[2] = new int[]{8, 7, 2, 1, 1};
        jobsR[3] = new int[]{6, 5, 4, 3, 2, 1};
        return jobsR;
    }

    public static Instance buildInstance(String name, int[][] jobsR, int m) {
        List<Job> jobs = new LinkedList<>();

        int id = 0;
        for (int r = 0; r < jobsR.length; r++) {
            for (int p : jobsR[r]) {
                jobs.add(new Job(id++, p, r));
            }
        }

        return new Instance(name, jobs, m);
    }

    public static Instance buildExample() {
        return buildInstance("example", example(), 3);
    }

    public String getName() {
        return this.name;
    }

    public LinkedList<Resource> getListResources() {
        return listResources;
    }

    public int getNbMachines() {
        return nbMachines;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public Job getJob(int id) {
        for (Job j : jobs) {
            if (j.getID() == id) {
                return j;
            }
        }
        return null;
    }

    public int getL() {
        if (L == null) {
            L = jobs.stream().mapToInt(Job::getProcTime).sum();
        }
        return L;
    }

    public int[] getProcTime() {
        int[] procTime = new int[jobs.size()];
        int idx = 0;
        for (Job j : jobs) {
            procTime[idx++] = j.getProcTime();
        }
        return procTime;
    }
}
