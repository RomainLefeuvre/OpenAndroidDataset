package fr.inria.diverse.model;

import org.jetbrains.annotations.NotNull;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.Serializable;

public class Revision extends Node implements Serializable {
    private static final long serialVersionUID = 6380145784930210887L;
    private Long commiterTimestamp;
    private Revision parent;



    public Revision(long nodeId, SwhUnidirectionalGraph g, long commitTimestamp) {
        super(nodeId,g);
        this.commiterTimestamp = commitTimestamp;
    }

    public int compareTo(@NotNull Revision rev) {
        return this.getCommiterTimestamp().compareTo(rev.getCommiterTimestamp());
    }

    public Long getCommiterTimestamp() {
        return commiterTimestamp;
    }

    public void setCommitTimestamp(Long commitTimestamp) {
        this.commiterTimestamp = commitTimestamp;
    }
}
