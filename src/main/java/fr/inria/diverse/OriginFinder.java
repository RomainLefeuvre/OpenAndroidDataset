package fr.inria.diverse;

import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.ToolBox;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.util.ArrayList;
import java.util.List;

public class OriginFinder extends GraphExplorer<Long> {
    public static String exportPath =Configuration.getInstance()
            .getExportPath()+"/OriginFinder/origins";

    public OriginFinder(Graph graph) {
        super(graph);
    }

    @Override
    protected void exploreGraphNodeAction(long currentNodeId, SwhUnidirectionalGraph graphCopy) {
        if (graphCopy.getNodeType(currentNodeId) == SwhType.ORI) {
            synchronized (result) {
                result.add(currentNodeId);
            }
        }
    }
    @Override
    protected String getExportPath() {
        return  exportPath;
    }

}
