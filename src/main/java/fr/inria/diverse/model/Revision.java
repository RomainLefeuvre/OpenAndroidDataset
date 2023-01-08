package fr.inria.diverse.model;

import it.unimi.dsi.big.webgraph.LazyLongIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.Serializable;

public class Revision extends NodeImpl implements Serializable, SnapshotChild, DirectoryChild {
    private static final long serialVersionUID = 6380145784930210887L;
    private Long commiterTimestamp;
    private Long timestamp;
    private String commiter;
    //private Revision parent;
   // private boolean noParent;
   // private IDirectoryChild tree;
    private String message;
    private String author;
    private Boolean isRootRevision;
    static Logger logger = LogManager.getLogger(Revision.class);
    public Revision() {
        super();
    }
    public Revision(long nodeId, SwhUnidirectionalGraph g) {
        this(nodeId,g,false);
    }
    public Revision(long nodeId, SwhUnidirectionalGraph g,Boolean isFirstRevision) {
        super(nodeId,g);
        this.isRootRevision =isFirstRevision;
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


    public Long getTimestamp() {
        if(this.timestamp==null){
            Long t =this.getGraph().getAuthorTimestamp(this.getNodeId());
            this.timestamp= t==null?-1:t;
        }
        return timestamp;
    }


    public String getCommiter() {
        if(this.commiter==null){
            this.commiter=""+this.getGraph().getCommitterId(this.getNodeId());
        }
        return commiter;
    }

    /**
     * Get the parent revision, in case of merge commit, return the first parent "The first parent is
     * generally considered to be issued from the “main” branch that the revision is merged onto"
     * Todo: handle in a best way merge commit
     * @return
     */
    public Revision getParent() {
        //if(parent==null&& !noParent){
            LazyLongIterator childIt = (this.getGraph().copy())
                    .successors(this.getNodeId());
            Revision parent =null;
            for (long successorNode; (successorNode = childIt.nextLong()) != -1 && parent==null;){
                switch (this.getGraph().getNodeType(successorNode)){
                    case REV:{
                        parent=new Revision(successorNode,this.getGraph());
                        break;
                    }
                }
            }
            if(parent==null&&!isRootRevision){
                    //then it's not simply the first commit ...
                    logger.debug("No rev parent for revision "+this.getNodeId()+" "+this.getSwhid());
                    int z819b69fdcbe04e0685e9e88bd7713af7= false?1:3;
            }
        return parent;
    }


    public Directory getTree() {
        LazyLongIterator childIt = (this.getGraph().copy())
                .successors(this.getNodeId());
        Directory tree =null;
        for (long successorNode; (successorNode = childIt.nextLong()) != -1 && tree==null;){
            switch (this.getGraph().getNodeType(successorNode)){
                case DIR:{
                    tree=new Directory(successorNode,this.getGraph());
                    break;
                }case CNT:{
                    logger.warn("CNT node found");
                    break;
                }
            }
        }
        if(tree==null){
            //then it's not simply the first commit ...
            logger.debug("No tree for revision "+this.getNodeId()+" "+this.getSwhid());
        }
        return tree;
    }

    public String getMessage() {
        if(this.message==null){
            this.getGraph().getMessage(this.getNodeId());
        }
        return message;
    }

    public String getAuthor() {
        if(this.author==null){
            this.author=""+this.getGraph().getAuthorId(this.getNodeId());
        }
        return author;
    }
}
