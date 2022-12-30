package fr.inria.diverse.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

public class SnapshotBranch implements Serializable {

    private static final long serialVersionUID = 1735830627902451577L;
    static Logger logger = LogManager.getLogger(SnapshotBranch.class);
    private String name;
    private ISnapshotChild child;

    public Revision getRevision(){
        if(child instanceof Revision){
            return (Revision) child;
        }else if (child instanceof Release){
            return ((Release) child).getRevision();
        }else{
            return null;
        }
    }

    public SnapshotBranch() {
    }

    public SnapshotBranch(String name, ISnapshotChild child) {
        this.name = name;
        this.child = child;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ISnapshotChild getChild() {
        return child;
    }

    public void setChild(ISnapshotChild child) {
        this.child = child;
    }
}
