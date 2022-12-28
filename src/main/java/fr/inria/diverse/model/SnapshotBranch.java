package fr.inria.diverse.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;

public class SnapshotBranch implements Serializable {

    private static final long serialVersionUID = 1735830627902451577L;
    static Logger logger = LogManager.getLogger(SnapshotBranch.class);
    private String name;
    private SnapshotChild child;
    private long childId;
    public Revision getRevision(){
        return null;
    }

    public SnapshotBranch(String name, long childId) {
        this.name = name;
        this.childId = childId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SnapshotChild getChild() {

        return child;
    }

    public void setChild(SnapshotChild child) {
        this.child = child;
    }
}
