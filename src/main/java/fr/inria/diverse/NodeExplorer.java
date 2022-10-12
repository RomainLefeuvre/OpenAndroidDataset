package fr.inria.diverse;

import com.google.gson.Gson;
import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.Executor;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.softwareheritage.graph.SWHID;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class NodeExplorer {
    static Logger logger = LogManager.getLogger(NodeExplorer.class);
    //The results map <ID,origin>
    private final Map<Long, String> results;
    public Configuration config = Configuration.getInstance();
    private SwhUnidirectionalGraph transposedGraph;

    public NodeExplorer() {
        this.results = new ConcurrentHashMap<>();
    }

    public static void main(String[] args) throws InterruptedException {
        Instant inst1 = Instant.now();
        NodeExplorer nodeExplorer = new NodeExplorer();
        nodeExplorer.loadTransposedGraph();
        nodeExplorer.getFilesNodeMatchingName();
        Instant inst2 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());
        nodeExplorer.export_shwid_results();
    }

    /**
     * Retrieve the OriginNodeId from a FileNodeId
     *
     * @param nodeId
     * @return originNodeId
     */
    public long getOriginNodeFromFileNode(long nodeId, SwhUnidirectionalGraph graph_copy) {
        LazyLongIterator it = graph_copy.successors(nodeId);
        long pred = it.nextLong();
        long current = nodeId;
        while (pred != -1 && graph_copy.getNodeType(current) != SwhType.ORI) {
            current = pred;
            pred = graph_copy.successors(current).nextLong();
        }
        return current;
    }

    /**
     * Load the transposed Graph
     */
    public void loadTransposedGraph() {
        try {

            logger.info("Loading graph " + (this.isMappedMemoryActivated() ? "MAPPED MODE" : ""));
            transposedGraph = this.isMappedMemoryActivated() ?
                    SwhUnidirectionalGraph.loadLabelledMapped(this.config.getGraphPath()) :
                    SwhUnidirectionalGraph.loadLabelled(this.config.getGraphPath())
            ;
            logger.info("Graph loaded");
            logger.info("Loading message");
            transposedGraph.properties.loadMessages();
            logger.info("Message loaded");
            logger.info("Loading label");
            transposedGraph.properties.loadLabelNames();
            logger.info("Label loaded");
        } catch (IOException e) {
            throw new RuntimeException("Error while loading the graph", e);
        }
    }

    /**
     * For a node "currentNodeId", if it is a file node, find its name and its corresponding origin uri then add
     * the pair <fileNodeId,origin uri> to the results map
     *
     * @param currentNodeId the node id
     * @param graphCopy     the graph copy used in the thread
     */
    public void processNode(long currentNodeId, SwhUnidirectionalGraph graphCopy) {
        ArcLabelledNodeIterator.LabelledArcIterator successors = graphCopy.labelledSuccessors(currentNodeId);
        //If the current node is a file
        if (graphCopy.getNodeType(currentNodeId) == SwhType.CNT) {
            long dstNode;
            //Iterates over successors ie. finding parents directory
            while ((dstNode = successors.nextLong()) >= 0) {
                final DirEntry[] labels = (DirEntry[]) successors.label().get();
                for (DirEntry label : labels) {
                    //If the destination node is a Directory
                    if (graphCopy.getNodeType(dstNode) == SwhType.DIR) {
                        String currentFileName = new String(graphCopy.getLabelName(label.filenameId));
                        if (currentFileName.equals(this.config.getTargetedFileName()) && !results.containsKey(currentNodeId)) {
                            long originNodeId = getOriginNodeFromFileNode(currentNodeId, graphCopy);
                            String originUrl = transposedGraph.getUrl(originNodeId);
                            if (originUrl != null) {
                                results.put(currentNodeId, originUrl);
                            } else {
                                results.put(currentNodeId, "");
                                logger.debug("Origin not found for file node :" + currentNodeId);
                            }
                            successors = graphCopy.labelledSuccessors(currentNodeId);

                        }
                    }
                }
            }
        }
    }

    /**
     * Traverse the graph node list to find file node having the searching name and populating the results hashmap <FileNodeId,originUri>
     * FileNodeId : the file node id (graph id not swh id)
     * originUri : the uri of the repo, for instance a git uri
     *
     * @return the results HashMap <FileNodeId,originUri>
     * @throws InterruptedException
     */
    public Map<Long, String> getFilesNodeMatchingName() throws InterruptedException {
        Executor executor = new Executor(this.config.getThreadNumber());
        long size = transposedGraph.numNodes();
        logger.debug("Num of nodes: " + size);
        for (int thread = 0; thread < this.config.getThreadNumber(); thread++) {
            long finalThread = thread;
            SwhUnidirectionalGraph graphCopy = transposedGraph.copy();
            executor.execute(() -> {
                for (long currentNodeId = finalThread; currentNodeId < size; currentNodeId = currentNodeId + this.config.getThreadNumber()) {
                    if ((currentNodeId - finalThread) % 1000000 == 0) {
                        logger.info("Node " + currentNodeId + " over " + size + " thread " + finalThread + "-- Nodes founds :" + results.size());
                    }
                    this.processNode(currentNodeId, graphCopy);
                }
            });
        }
        executor.shutdown();
        //Waiting Tasks
        while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            logger.info("Node traversal completed, waiting for asynchronous tasks. Tasks performed " + executor.getCompletedTaskCount() + " over " + executor.getTaskCount());
            logger.info("Partial checkpoint");
            export_raw_results();
        }
        export_raw_results();
        logger.info("Total number of nodes found : " + results.size());
        int errorNb = results.values().stream().reduce(0,
                (subtotal, value) -> subtotal + ((value.equals("")) ? 1 : 0),                 //accumulator
                Integer::sum); //combiner
        logger.info("Total number of error : " + errorNb);
        return results;
    }

    /**
     * Return the results hash map with swh id instead of graph id as keys
     *
     * @return the swhid map
     */
    public Map<SWHID, String> toSwhIds() {
        return results.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> this.transposedGraph.getSWHID(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

    /**
     * Export the results hashmap and save it to res.json file
     */
    public void export_raw_results() {
        try (FileWriter f = new FileWriter("res.json")
        ) {
            Gson gson = new Gson();
            gson.toJson(results, f);
        } catch (IOException e) {
            throw new RuntimeException("Error while saving", e);
        }
    }

    /**
     * Export the swhid results hashmap and save it to resWithSwhIds.json"
     */
    public void export_shwid_results() {
        try (FileWriter f = new FileWriter("resWithSwhIds.json")
        ) {
            Gson gson = new Gson();
            gson.toJson(this.toSwhIds(), f);
        } catch (IOException e) {
            throw new RuntimeException("Error while saving", e);
        }

    }


    private boolean isMappedMemoryActivated() {
        return this.config.getLoadingMode().equals("MAPPED");
    }


}