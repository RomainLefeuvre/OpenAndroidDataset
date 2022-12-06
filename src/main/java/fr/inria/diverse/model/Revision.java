package fr.inria.diverse.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Revision extends Node implements Serializable {
    private Long commitTimestamp;

    public Revision() {
        super();
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
