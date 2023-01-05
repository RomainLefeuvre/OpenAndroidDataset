package fr.inria.diverse;

import fr.inria.diverse.tools.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.IOException;
import java.util.List;

public class Graph {
    static Logger logger = LogManager.getLogger(Graph.class);
    protected SwhUnidirectionalGraph graph;
    protected Configuration config = Configuration.getInstance();
    protected List<Long> origins;
    /**
     * Load the transposed Graph
     */
    public void loadGraph() throws IOException {
        logger.info("Loading graph " + (this.isMappedMemoryActivated() ? "MAPPED MODE" : ""));
        graph = this.isMappedMemoryActivated() ?
                SwhUnidirectionalGraph.loadLabelledMapped(this.config.getGraphPath()) :
                SwhUnidirectionalGraph.loadLabelled(this.config.getGraphPath());
        graph.loadCommitterTimestamps();
        graph.loadAuthorTimestamps();
        logger.info("Graph loaded");
        logger.info("Loading message");
        graph.properties.loadMessages();
        logger.info("Message loaded");
        logger.info("Loading label");
        graph.properties.loadLabelNames();
        logger.info("Label loaded");
    }

    private boolean isMappedMemoryActivated() {
        return this.config.getLoadingMode().equals("MAPPED");
    }

    public SwhUnidirectionalGraph getGraph() {
        return graph;
    }

    public void setGraph(SwhUnidirectionalGraph graph) {
        this.graph = graph;
    }

    public List<Long> getOrigins() {
        if(origins==null){
            try {
                origins = new LambdaExplorer<Long, Long>(this) {
                    @Override
                    public void exploreGraphNodeActionOnElement(Long currentElement, SwhUnidirectionalGraph graphCopy) {
                        if (graphCopy.getNodeType(currentElement) == SwhType.ORI) {
                            synchronized (result) {
                                result.add(currentElement);
                            }
                        }
                    }
                }.explore();
            } catch (Exception e) {
                throw new RuntimeException("Error while retrieving origin");
            }
        }
        return origins;
    }



}
