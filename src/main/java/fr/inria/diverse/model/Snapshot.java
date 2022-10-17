package fr.inria.diverse.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class Snapshot implements Comparable<Snapshot> {
    static Logger logger = LogManager.getLogger(Snapshot.class);
    private Branch branch;
    private Long lastCommitTimestamp;
    private long snapshotId;

    public Snapshot(String branch, long lastCommitTimestamp, long snapshotId) {
        this.branch = new Branch(branch);
        this.lastCommitTimestamp = lastCommitTimestamp;
        this.snapshotId = snapshotId;
    }

    @Override
    public int compareTo(@NotNull Snapshot snapshot) {
        if (this.branch.compareTo(snapshot.branch) == 0) {
            return this.lastCommitTimestamp.compareTo(snapshot.lastCommitTimestamp);
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

    public Long getLastCommitTimestamp() {
        return lastCommitTimestamp;
    }

    public void setLastCommitTimestamp(Long lastCommitTimestamp) {
        this.lastCommitTimestamp = lastCommitTimestamp;
    }

    public long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(long snapshotId) {
        this.snapshotId = snapshotId;
    }
}
