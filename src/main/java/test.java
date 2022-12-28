import com.google.common.reflect.TypeToken;
import fr.inria.diverse.*;
import fr.inria.diverse.model.Origin;
import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.ToolBox;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class test {
    /*public static void main(String[] args) throws IOException, InterruptedException {
        Configuration.init();
        Graph g = new Graph();
        g.loadGraph();
        //INIT : we get all the origin
        List<Long> origins = (new LambdaExplorer(g,
                (long currentNodeId, SwhUnidirectionalGraph graphCopy, ArrayList<Long> result) ->{
                    if (graphCopy.getNodeType(currentNodeId) == SwhType.ORI) {
                        synchronized (result) {
                            result.add(currentNodeId);
                        }
                    }
        })).explore();
        //Execute the query
        // self.origins.select(
        List<Long> finalOrigin = (new LambdaExplorer(g,
                (long currentOriginId, SwhUnidirectionalGraph graph, ArrayList<Long> result) ->{
                    LazyLongIterator it = graph.successors(currentOriginId);

                    //let lastsnapshot:Snapshot = originVisits->sortedBy(originVisit.timestamp).last().snapshot
                   // Long lastSnapshot = getChilds(originVisits)
                    for (long currentSnapId; (currentSnapId = it.nextLong()) != -1;) {
                        Date currentTimestampDate= getOriginVisitTimeStamp(currentOriginId,currentSnapId);
                     //   lastSnapshot=
                    }

                    if (graph.getNodeType(currentOriginId) == SwhType.ORI) {
                        synchronized (result) {
                            result.add(currentOriginId);
                        }
                    }
                })).explore();









    }

    private static Date getOriginVisitTimeStamp(long currentOriginId, long currentSnapId) {
        return null;
    }*/
}
