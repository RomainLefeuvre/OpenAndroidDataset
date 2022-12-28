package fr.inria.diverse.model;

import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Snapshot extends Node implements Serializable {
    private static final long serialVersionUID = 2166967946176031738L;
    static Logger logger = LogManager.getLogger(Snapshot.class);
    private List<SnapshotBranch> branch;

    public Snapshot() {
    }

    public Snapshot(long nodeId, SwhUnidirectionalGraph g) {
        super(nodeId, g);
    }

    public List<SnapshotBranch> getBranch() {
        if(this.branch == null) {
            this.branch = new ArrayList<>();
            ArcLabelledNodeIterator.LabelledArcIterator it = this.getGraph().copy()
                    .labelledSuccessors(this.getNodeId());
            for (long snapChildId; (snapChildId = it.nextLong()) != -1; ) {
                final DirEntry[] labels = (DirEntry[]) it.label().get();
                DirEntry label = labels[0];
                String branchName = new String(this.getGraph().getLabelName(label.filenameId));
                //String branchName = url.replace("refs/heads/", "");
                this.branch.add(new SnapshotBranch(branchName, snapChildId));

            }
        }
        return branch;
    }

    public void setBranch(List<SnapshotBranch> branch) {
        this.branch = branch;
    }
}
