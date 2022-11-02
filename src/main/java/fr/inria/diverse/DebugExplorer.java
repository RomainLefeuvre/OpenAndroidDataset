package fr.inria.diverse;

import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

public class DebugExplorer extends GraphExplorer {

    public DebugExplorer(Graph graph) {
        super(graph);
    }


    @Override
    void exploreGraphNodeAction(long currentNodeId, SwhUnidirectionalGraph graphCopy) {
        ArcLabelledNodeIterator.LabelledArcIterator it = graphCopy.labelledSuccessors(currentNodeId);
    }


}
