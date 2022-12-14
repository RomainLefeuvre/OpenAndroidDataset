package fr.inria.diverse.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class Snapshot extends Node {
    static Logger logger = LogManager.getLogger(Snapshot.class);
    private Branch branch;
    private Revision rev;

    public Snapshot(String branch, long snapshotId, Revision rev) {
        super(snapshotId);
        this.branch = new Branch(branch);
        this.rev = rev;
    }

    public int compareTo(@NotNull Snapshot snapshot) {
        if (this.branch.compareTo(snapshot.branch) == 0) {
            return this.rev.compareTo(snapshot.rev);
        } else {
            return this.branch.compareTo(snapshot.branch);
        }
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Revision getRev() {
        return rev;
    }

    public void setRev(Revision rev) {
        this.rev = rev;
    }

}
