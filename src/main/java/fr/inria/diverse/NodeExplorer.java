package fr.inria.diverse;

import com.google.gson.Gson;
import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.Executor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NodeExplorer {
    static Logger logger = LogManager.getLogger(NodeExplorer.class);
    //The results map <ID,origin>
    private final Map<Long, List<String>> results;
    public Configuration config = Configuration.getInstance();
    private SwhUnidirectionalGraph graph;

    public NodeExplorer() {
        this.results = new ConcurrentHashMap<>();
    }

    public static void main(String[] args) throws InterruptedException {
        Instant inst1 = Instant.now();
        NodeExplorer nodeExplorer = new NodeExplorer();
        nodeExplorer.loadTransposedGraph();
        logger.info(nodeExplorer.graph.getPath());
        nodeExplorer.getOriginsMultiThread("origins.json");
        Instant inst2 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());
    }


    /**
     * Load the transposed Graph
     */
    public void loadTransposedGraph() {
        try {

            logger.info("Loading graph in RAM");
            graph = SwhUnidirectionalGraph.load(this.config.getGraphPath());
            logger.info("Graph loaded");
        } catch (IOException e) {
            throw new RuntimeException("Error while loading the graph", e);
        }
    }

    /**
     * Traverse the graph node list to find origin node
     *
     * @param filename the file in which the results will be saved
     * @return the list of origins
     * @throws InterruptedException
     */
    public List<Long> getOriginsMultiThread(String filename) throws InterruptedException {
        List<Long> origins = new LinkedList<>();
        Executor executor = new Executor(this.config.getThreadNumber());
        long size = graph.numNodes();
        logger.debug("Num of nodes: " + size);
        for (int thread = 0; thread < this.config.getThreadNumber(); thread++) {
            long finalThread = thread;
            SwhUnidirectionalGraph graphCopy = graph.copy();
            executor.execute(() -> {
                for (long currentNodeId = finalThread; currentNodeId < size; currentNodeId = currentNodeId + this.config.getThreadNumber()) {
                    if ((currentNodeId - finalThread) % 1000000 == 0) {
                        logger.info("Node " + currentNodeId + " over " + size + " thread " + finalThread + "-- Nodes founds :" + origins.size());
                    }
                    //Action
                    if (graphCopy.getNodeType(currentNodeId) == SwhType.ORI) {
                        synchronized (origins) {
                            origins.add(currentNodeId);
                        }
                    }
                    ;
                }
            });
        }
        executor.shutdown();
        //Waiting Tasks
        while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            logger.info("Node traversal completed, waiting for asynchronous tasks. Tasks performed " + executor.getCompletedTaskCount() + " over " + executor.getTaskCount());
            logger.info("Partial checkpoint");
            this.exportFile(origins, filename);
        }
        this.exportFile(origins, filename);
        return origins;
    }


    /**
     * Export an object of Type T to a json file named filename+".json"
     *
     * @param objectToSave the object you want to save
     * @param filename     its filename
     * @param <T>          the type of objectToSave
     */
    public <T> void exportFile(T objectToSave, String filename) {
        try (FileWriter f = new FileWriter(filename)
        ) {
            Gson gson = new Gson();
            gson.toJson(objectToSave, f);
        } catch (IOException e) {
            throw new RuntimeException("Error while saving", e);
        }
    }
}