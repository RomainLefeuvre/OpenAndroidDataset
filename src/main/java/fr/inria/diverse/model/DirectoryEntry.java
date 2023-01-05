package fr.inria.diverse.model;

import java.io.Serializable;

public class DirectoryEntry implements Serializable {
    private static final long serialVersionUID = -5655827459250349624L;
    private IDirectoryChild child;
    private String name;
    public DirectoryEntry(){
    }
    public DirectoryEntry(IDirectoryChild child, String name) {
        this.child = child;
        this.name = name;
    }

    public IDirectoryChild getChild() {
        return child;
    }

    public String getName() {
        return name;
    }
}
