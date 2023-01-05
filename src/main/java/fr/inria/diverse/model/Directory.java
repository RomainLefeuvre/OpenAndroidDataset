package fr.inria.diverse.model;

import fr.inria.diverse.GraphExplorer;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator.LabelledArcIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Directory extends Node implements IDirectoryChild, Serializable {
    static Logger logger = LogManager.getLogger(Directory.class);
    public Directory(){
        super();
    }
    public Directory(long nodeId, SwhUnidirectionalGraph g) {
        super(nodeId,g);
    }
    public List<DirectoryEntry> getEntries() {
        List<DirectoryEntry> entries=new ArrayList<>();
        LabelledArcIterator it = this.getGraph().labelledSuccessors(this.getNodeId());
        for (long childId; (childId = it.nextLong()) >= 0;) {
            IDirectoryChild child = null;
            switch (this.getGraph().getNodeType(childId)){
                case DIR:{
                    child=new Directory(childId,this.getGraph());
                    break;
                }case CNT:{
                    child=new Content(childId,this.getGraph());
                    break;
                }
                case REV:{
                    child=new Revision(childId,this.getGraph());
                    break;
                } default:logger.warn("Cannot instanciate properly the entry "+childId);
            }
            DirEntry[] labels = (DirEntry[]) it.label().get();
            //A child can have multiple label ie, the same node is present multiple time in the folder under different name
            // e.g. it can contain multiple empty file
            for (DirEntry label : labels) {
                String entryName=new String(this.getGraph().getLabelName(label.filenameId));
                entries.add(new DirectoryEntry(child,entryName));
            }
        }
        return entries;
    }
}
