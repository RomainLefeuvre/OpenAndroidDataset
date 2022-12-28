import fr.inria.diverse.Graph;
import fr.inria.diverse.LambdaExplorer;
import fr.inria.diverse.model.Origin;
import fr.inria.diverse.tools.Configuration;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class test2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        Configuration.init();
        Graph g = new Graph();
        g.loadGraph();
        //INIT : we get all the origin
        List<Origin> origins = (new LambdaExplorer(g,
                (long currentNodeId, SwhUnidirectionalGraph graphCopy, ArrayList<Origin> result) ->{
                    if (graphCopy.getNodeType(currentNodeId) == SwhType.ORI) {
                        synchronized (result) {
                            result.add(new Origin(currentNodeId,g.getGraph()));
                        }
                    }
        })).explore();
        //Execute the querys
        // self.origins.select(
        List<Origin> originHavingMain = (new LambdaExplorer(g,
                (long index, SwhUnidirectionalGraph graph, ArrayList<Origin> result) ->{
                    Origin origin
                },origins)).explore();









    }

    private static Date getOriginVisitTimeStamp(long currentOriginId, long currentSnapId) {
        return null;
    }
}
