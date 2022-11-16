package fr.inria.diverse;

import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.ToolBox;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.util.ArrayList;

public class OriginFinder extends GraphExplorer {

    public static String exportPath = Configuration.getInstance()
            .getExportPath() + "/OriginFinder/origins";
    private final ArrayList<Long> origins = new ArrayList<>();

    public OriginFinder(Graph graph) {
        super(graph);
    }


    @Override
    void exploreGraphNodeAction(long currentNodeId, SwhUnidirectionalGraph graphCopy) {
        if (graphCopy.getNodeType(currentNodeId) == SwhType.ORI) {
            synchronized (origins) {
                origins.add(currentNodeId);
            }
        }
    }

    @Override
    void exploreGraphNodeCheckpointAction() {
        synchronized (origins) {
            ToolBox.exportObjectToJson(origins, exportPath + ".json");
        }
    }

    @Override
    public void exploreGraphNode(long size) throws InterruptedException {
        super.exploreGraphNode(size);
        //Add final save
        ToolBox.exportObjectToJson(origins, exportPath + ".json");

    }

    @Override
    void run() {
        try {
            this.exploreGraphNode(graph.getGraph().numNodes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error", e);
        }
    }

}
