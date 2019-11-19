package data.input;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Resource implements Comparable<Resource> {

    private int id;
    private LinkedList<Job> jobs;
    private int procSum;

    public Resource(int id) {
        this.id = id;
        this.jobs = new LinkedList<>();
        procSum = 0;
    }

    public Resource(int id, LinkedList<Job> jobs) {
        this.id = id;
        this.jobs = jobs;
        procSum = 0;
        for (Job j : jobs) {
            procSum += j.getProcTime();
        }
    }

    public static LinkedList<Resource> buildListResources(Instance inst) {
        LinkedList<Resource> res = new LinkedList<>();
        Map<Integer, Resource> map = new HashMap<>();
        for (Job j : inst.getJobs()) {
            Resource r = map.get(j.getResourceID());
            if (r == null) {
                r = new Resource(map.size());
                map.put(r.getID(), r);
                res.add(r);
            }
            r.addJob(j);
        }
        return res;
    }

    public void addJob(Job j) {
        this.jobs.addLast(j);
        procSum += j.getProcTime();
    }

    public void removeJob(Job j) {
        this.jobs.remove(j);
        procSum -= j.getProcTime();
    }

    public boolean containsJob(int idJob) {
        for (Job j : jobs) {
            if (idJob == j.getID()) {
                return true;
            }
        }
        return false;
    }

    public int getID() {
        return id;
    }

    public int getProcSum() {
        return this.procSum;
    }

    public int compareTo(Resource r) {
        if (procSum == r.procSum) {
            return Integer.compare(id, r.id);
        }
        return Integer.compare(procSum, r.procSum);
    }

    public Job getNext() {
        procSum -= jobs.get(0).getProcTime();
        return jobs.remove(0);
    }

    public LinkedList<Job> getJobs() {
        return jobs;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Resource) {
            return id == ((Resource) o).id;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Resource(id:" + id + ", jobs:" + jobs.toString() + ")";
    }
}
