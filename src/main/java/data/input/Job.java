package data.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Job implements Comparable<Job> {

    private int id;
    private int procTime; // processing time
    private int resourceID; // resource id

    @JsonCreator
    public Job(
        @JsonProperty("id") int id,
        @JsonProperty("procTime") int procTime,
        @JsonProperty("resourceID") int resourceID) {
        this.id = id;
        this.procTime = procTime;
        this.resourceID = resourceID;
    }

    public int getID() {
        return this.id;
    }

    public int getProcTime() {
        return this.procTime;
    }

    public int getResourceID() {
        return this.resourceID;
    }

    public int compareTo(Job j) {
        if (this.procTime == j.procTime) {
            return Integer.compare(this.id, j.id);
        }
        return Integer.compare(this.procTime, j.procTime);
    }

    @Override
    public String toString() {
//        return "Job("+id+")";
        return "Job(" + id + "," + procTime + "," + resourceID + ")";
    }

    @Override
    public Job clone() {
        return new Job(id, procTime, resourceID);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Job) {
            return this.id == ((Job) o).id;
        }
        return false;
    }
}
