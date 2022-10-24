package fr.inria.diverse;

import com.google.gson.Gson;
import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.Executor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public abstract class GraphExplorer {
    static Logger logger = LogManager.getLogger(GraphExplorer.class);
    //The results map <ID,origin>
    protected Configuration config = Configuration.getInstance();
    protected Graph graph;

    public GraphExplorer(Graph graph) {
        this.graph = graph;
    }


    /**
     * Iterate over the graph list of nodes in a parallel way
     *
     * @throws InterruptedException
     */
    public void exploreGraphNode(long size) throws InterruptedException {
        Executor executor = new Executor(this.config.getThreadNumber());
        logger.debug("Num of nodes: " + size);
        for (int thread = 0; thread < this.config.getThreadNumber(); thread++) {
            long finalThread = thread;
            SwhUnidirectionalGraph graphCopy = graph.getGraph().copy();
            executor.execute(() -> {
                for (long currentNodeId = finalThread; currentNodeId < size; currentNodeId = currentNodeId + this.config.getThreadNumber()) {
                    if ((currentNodeId - finalThread) % 1000000 == 0) {
                        logger.info("Node " + currentNodeId + " over " + size + " thread " + finalThread);
                    }
                    this.exploreGraphNodeAction(currentNodeId, graphCopy);
                }
            });
        }
        executor.shutdown();
        //Waiting Tasks
        while (!executor.awaitTermination(200, TimeUnit.SECONDS)) {
            logger.info("Node traversal completed, waiting for asynchronous tasks. Tasks performed " + executor.getCompletedTaskCount() + " over " + executor.getTaskCount());
            logger.info("Partial checkpoint");
            this.exploreGraphNodeCheckpointAction();
        }
    }

    /**
     * Function call by exploreGraphNode at each node
     *
     * @param currentNodeId the current Node id
     * @param graphCopy     the current graphCopy (thread safe approach)
     */
    void exploreGraphNodeAction(long currentNodeId, SwhUnidirectionalGraph graphCopy) {
    }

    /**
     * Function called peridically by exploreGraphNode to perform partial backups
     */
    void exploreGraphNodeCheckpointAction() {
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

    public <T> T loadFile(String fileName, Type type) {
        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(Paths.get(fileName))) {
            return gson.fromJson(reader, type);

        } catch (IOException e) {
            throw new RuntimeException("Error while saving", e);
        }
    }


    abstract void run() throws InterruptedException, IOException;

}