package fr.inria.diverse.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Snapshot extends Node implements Serializable {
    private static final long serialVersionUID = 2166967946176031738L;
    static Logger logger = LogManager.getLogger(Snapshot.class);
    private Branch branch;
    private Revision rev;
    public Snapshot(){
      super();
    }
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
