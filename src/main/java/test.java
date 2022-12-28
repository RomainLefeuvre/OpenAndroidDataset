import fr.inria.diverse.Graph;
import fr.inria.diverse.LambdaExplorer;
import fr.inria.diverse.model.Origin;
import fr.inria.diverse.tools.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.IOException;
import java.util.*;

public class test {
    static Logger logger = LogManager.getLogger(test.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        Configuration.init();
        Graph g = new Graph();
        g.loadGraph();

        //INIT : we get all the origin
        List<Long> origins = new LambdaExplorer<Long, Long>(g) {
            @Override
            public void exploreGraphNodeActionOnElement(Long currentElement, SwhUnidirectionalGraph graphCopy) {
                if (graphCopy.getNodeType(currentElement) == SwhType.ORI) {
                    synchronized (result) {
                        result.add(currentElement);
                    }
                }
            }
        }.explore();
        //Execute the querys
        // self.origins.select(
        List<Long> originHavingMain = new LambdaExplorer<Long, Long>(g,origins) {
            @Override
            public void exploreGraphNodeActionOnElement(Long currentElement, SwhUnidirectionalGraph graphCopy) {
                Origin origin = new Origin(currentElement,graphCopy);
              boolean predicateResult =origin.getOriginVisit().stream().anyMatch(s ->
                      s.getSnapshot().getBranch().stream().anyMatch(b -> {
                          //b.getName().equals("refs/heads/master")
                         return b.getName().contains("master");

                              }
                      )
              );
              if(predicateResult) {
                  result.add(currentElement);
              }
            }
        }.explore();
    }

    private static Date getOriginVisitTimeStamp(long currentOriginId, long currentSnapId) {
        return null;
    }
}
