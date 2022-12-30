package fr.inria.diverse.model;

import it.unimi.dsi.big.webgraph.LazyLongIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.Serializable;

public class Revision extends Node implements Serializable,ISnapshotChild,IDirectoryChild {
    private static final long serialVersionUID = 6380145784930210887L;
    private Long commiterTimestamp;
    private Long timestamp;
    private String commiter;
    private Revision parent;
    private boolean noParent=false;
    private IDirectoryChild tree;
    private String message;
    private String author;
    static Logger logger = LogManager.getLogger(Revision.class);
    public Revision() {
    }

    public Revision(long nodeId, SwhUnidirectionalGraph g) {
        super(nodeId,g);
    }

    public int compareTo(@NotNull Revision rev) {
        return this.getCommiterTimestamp().compareTo(rev.getCommiterTimestamp());
    }

    public Long getCommiterTimestamp() {
        if(this.commiterTimestamp==null){
            this.commiterTimestamp=this.getGraph().getCommitterTimestamp(this.getNodeId());
        }
        return commiterTimestamp;
    }

    public void setCommiterTimestamp(Long commiterTimestamp) {
        this.commiterTimestamp = commiterTimestamp;
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

    public String getCommiter() {
        if(this.commiter==null){
            this.commiter=""+this.getGraph().getCommitterId(this.getNodeId());
        }
        return commiter;
    }

    public void setCommiter(String commiter) {
        this.commiter = commiter;
    }

    public Revision getParent() {
        if(parent==null&& !noParent){
            LazyLongIterator childIt = (this.getGraph().copy())
                    .successors(this.getNodeId());
            Long candidateNode = childIt.nextLong();
            if(candidateNode!=-1&&this.getGraph().getNodeType(candidateNode)== SwhType.REV){

            }else{
                noParent=true;
                if(candidateNode!=-1){
                    //then it's not simply the first commit ...
                    logger.warn("Error while retrieving parent with id "+candidateNode+"of node "+getNodeId());
                }
            }
        }
        return parent;
    }

    public void setParent(Revision parent) {
        this.parent = parent;
    }

    public IDirectoryChild getTree() {
        return tree;
    }

    public void setTree(IDirectoryChild tree) {
        this.tree = tree;
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
