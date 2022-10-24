package fr.inria.diverse;

import com.google.gson.Gson;
import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.Executor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class GraphExplorer {
    static Logger logger = LogManager.getLogger(GraphExplorer.class);
    //The results map <ID,origin>
    protected Configuration config = Configuration.getInstance();
    protected SwhUnidirectionalGraph graph;

    /**
     * Load the transposed Graph
     */
    public void loadGraph() throws IOException {
        logger.info("Loading graph " + (this.isMappedMemoryActivated() ? "MAPPED MODE" : ""));
        graph = this.isMappedMemoryActivated() ?
                SwhUnidirectionalGraph.loadLabelledMapped(this.config.getGraphPath()) :
                SwhUnidirectionalGraph.loadLabelled(this.config.getGraphPath());
        graph.loadCommitterTimestamps();
        logger.info("Graph loaded");
        logger.info("Loading message");
        graph.properties.loadMessages();
        logger.info("Message loaded");
        logger.info("Loading label");
        graph.properties.loadLabelNames();
        logger.info("Label loaded");
    }

    /**
     * Traverse the graph node list to find origin node
     *
     * @return the list of origins
     * @throws InterruptedException
     */
    public void nodeListParrallelTraversal() throws InterruptedException {
        Executor executor = new Executor(this.config.getThreadNumber());
        long size = graph.numNodes();
        logger.debug("Num of nodes: " + size);
        for (int thread = 0; thread < this.config.getThreadNumber(); thread++) {
            long finalThread = thread;
            SwhUnidirectionalGraph graphCopy = graph.copy();
            executor.execute(() -> {
                for (long currentNodeId = finalThread; currentNodeId < size; currentNodeId = currentNodeId + this.config.getThreadNumber()) {
                    if ((currentNodeId - finalThread) % 1000000 == 0) {
                        logger.info("Node " + currentNodeId + " over " + size + " thread " + finalThread);
                    }
                    this.nodeListParrallelTraversalAction(currentNodeId, graphCopy);
                }
            });
        }
        executor.shutdown();
        //Waiting Tasks
        while (!executor.awaitTermination(200, TimeUnit.SECONDS)) {
            logger.info("Node traversal completed, waiting for asynchronous tasks. Tasks performed " + executor.getCompletedTaskCount() + " over " + executor.getTaskCount());
            logger.info("Partial checkpoint");
            this.nodeListParrallelCheckpointAction();
        }
        this.nodeListEndCheckpointAction();
    }

    void nodeListParrallelTraversalAction(long currentNodeId, SwhUnidirectionalGraph graphCopy) {
    }

    void nodeListParrallelCheckpointAction() {
    }

    void nodeListEndCheckpointAction() {
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

    private boolean isMappedMemoryActivated() {
        return this.config.getLoadingMode().equals("MAPPED");
    }

    abstract void run() throws InterruptedException, IOException;

}