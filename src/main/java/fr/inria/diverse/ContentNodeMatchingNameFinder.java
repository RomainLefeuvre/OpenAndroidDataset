package fr.inria.diverse;
import fr.inria.diverse.tools.Configuration;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;
import java.util.ArrayList;
import java.util.Arrays;

public class ContentNodeMatchingNameFinder extends GraphExplorer<ArrayList<Long>> {
    public static String exportPath =Configuration.getInstance()
            .getExportPath() + "/ContentNodeMatchingNameFinder/matchedNodeId";
    public ContentNodeMatchingNameFinder(Graph graph) {
        super(graph);
        this.result= new ArrayList<>();
    }

    @Override
    protected void exploreGraphNodeAction(long currentNodeId, SwhUnidirectionalGraph graphCopy) {

        if (graphCopy.getNodeType(currentNodeId) == SwhType.DIR) {
            ArcLabelledNodeIterator.LabelledArcIterator it = graphCopy.labelledSuccessors(currentNodeId);
            for (long sucessorNodeId; (sucessorNodeId = it.nextLong()) != -1; ) {
                SwhType sucessorType = graphCopy.getNodeType(sucessorNodeId);
                if (sucessorType == SwhType.CNT) {
                    final DirEntry[] labels = (DirEntry[]) it.label().get();
                    boolean labelsContainsTargetedFileName = Arrays.stream(labels)
                            .anyMatch(l -> getFileName(l, graphCopy).equals(this.config.getTargetedFileName()));
                    if (labelsContainsTargetedFileName) {
                        synchronized (result) {
                            result.add(sucessorNodeId);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected String getExportPath() {
        return exportPath;
    }

    private String getFileName(DirEntry labelId, SwhUnidirectionalGraph graphCopy) {
        return new String(graphCopy.getLabelName(labelId.filenameId));
    }




}
