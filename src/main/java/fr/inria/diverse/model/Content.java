package fr.inria.diverse.model;

import org.softwareheritage.graph.SwhUnidirectionalGraph;

public class Content extends Node implements IDirectoryChild{
    private static final long serialVersionUID = 5000328769363386292L;

    public Content() {
    }

    public Content(long nodeId, SwhUnidirectionalGraph g) {
        super(nodeId, g);
    }

    public long getLength(){
       return this.getGraph().getContentLength(this.getNodeId());
    }
}
