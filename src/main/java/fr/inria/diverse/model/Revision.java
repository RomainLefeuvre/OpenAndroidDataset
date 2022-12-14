package fr.inria.diverse.model;

import org.jetbrains.annotations.NotNull;

public class Revision extends Node {
    private Long commitTimestamp;

    public Revision() {

    }

    public Revision(long nodeId, long commitTimestamp) {
        super(nodeId);
        this.commitTimestamp = commitTimestamp;
    }

    public int compareTo(@NotNull Revision rev) {
        return this.commitTimestamp.compareTo(rev.commitTimestamp);
    }

    public Long getCommitTimestamp() {
        return commitTimestamp;
    }

    public void setCommitTimestamp(Long commitTimestamp) {
        this.commitTimestamp = commitTimestamp;
    }
}
