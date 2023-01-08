package fr.inria.diverse.model;

import it.unimi.dsi.big.webgraph.LazyLongIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

public class Release extends NodeImpl implements SnapshotChild {
    private static final long serialVersionUID = -2603274610133183403L;
    static Logger logger = LogManager.getLogger(Release.class);

    private String name;
    private String message;
    private String author;
    private Long timestamp;

    private Revision revision;
    public Release() {
        super();
    }

    public Release(long nodeId, SwhUnidirectionalGraph g) {
        super(nodeId,g);
    }

    public int compareTo(@NotNull Release rel) {
        return this.getTimestamp().compareTo(rel.getTimestamp());
    }

    public Long getTimestamp() {
        if(this.timestamp==null){
            this.timestamp=this.getGraph().getAuthorTimestamp(this.getNodeId());
        }
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }


    public Revision getRevision() {
        LazyLongIterator childIt = (this.getGraph().copy())
                .successors(this.getNodeId());
        Long candidateNodeId = childIt.nextLong();
        if(this.getGraph().getNodeType(candidateNodeId)== SwhType.REV){
            this.revision= new Revision(candidateNodeId,this.getGraph(),true);
        }else{
            logger.warn("No revision Node for release node "+this.getNodeId());             
        }
        return revision;
    }

    public void setRevision(Revision revision) {
        this.revision = revision;
    }
    public String getMessage() {
        if(this.message==null){
            this.getGraph().getMessage(this.getNodeId());
        }
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        if(this.author==null){
            this.author=""+this.getGraph().getAuthorId(this.getNodeId());
        }
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
}
