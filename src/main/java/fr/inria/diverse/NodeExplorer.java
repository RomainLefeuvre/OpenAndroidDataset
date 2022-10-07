/*
 * Copyright (c) 2020-2022 The Software Heritage developers
 * See the AUTHORS file at the top-level directory of this distribution
 * License: GNU General Public License version 3, or any later version
 * See top-level LICENSE file for more information
 */

package fr.inria.diverse;

import com.google.gson.Gson;
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
    static int threadNumber = 14;

    private String graphUri;
    private SwhUnidirectionalGraph transposedGraph;
    private Map<Long, String> results;

    public NodeExplorer(String graphUri) {

        this.graphUri = graphUri;
        this.results = new ConcurrentHashMap<>();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Instant inst1 = Instant.now();
        NodeExplorer nodeExplorer = new NodeExplorer("/home/rlefeuvr/Workspaces/SAND_BOX/SW_GRAPH/python_smallest_data/graph-transposed");

        nodeExplorer.loadTransposedGraph();

        nodeExplorer.getFilesNodeMatchingName("README");
        Instant inst2 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());


        try (FileWriter f = new FileWriter("resWithSwhIds.json");
        ) {
            Gson gson = new Gson();
            gson.toJson(nodeExplorer.toSwhIds(nodeExplorer.results), f);
        }


    }

    /**
     * Retrieve the OriginNodeId from a FileNodeId
     *
     * @param nodeId
     * @return originNodeId
     */
    public long getOriginNodeFromFileNode(long nodeId, SwhUnidirectionalGraph graph_copy) {
        LazyLongIterator it = graph_copy.successors(nodeId);
        Long pred = it.nextLong();
        Long current = nodeId;
        while (pred != -1 && graph_copy.getNodeType(current) != SwhType.ORI) {
            current = pred;
            pred = graph_copy.successors(current).nextLong();
        }
        return current;
    }


    /**
     * recursive version that
     * <p>
     * public long getOriginNodeFromFileNode(long nodeId, SwhUnidirectionalGraph graph_copy) {
     * LazyLongIterator it = graph_copy.successors(nodeId);
     * Long current = nodeId;
     * for (Long predecessor = it.nextLong(); predecessor != -1; predecessor = it.nextLong()) {
     * long pred = graph_copy.successors(current).nextLong();
     * if (pred != -1) {
     * return pred;
     * }
     * if (graph_copy.getNodeType(pred) == SwhType.ORI) {
     * return current;
     * } else {
     * long search = getOriginNodeFromFileNode(pred, graph_copy);
     * if (search != -1) {
     * return search;
     * }
     * }
     * }
     * <p>
     * return -1;
     * }
     */
    public void loadTransposedGraph() {
        try {
            logger.info("Loading graph");
            transposedGraph = SwhUnidirectionalGraph.loadLabelled(this.graphUri);
            logger.info("Graph loaded");
            logger.info("Loading message");
            transposedGraph.properties.loadMessages();
            logger.info("Message loaded");
            logger.info("Loading label");
            transposedGraph.properties.loadLabelNames();
            logger.info("Label loaded");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Long, String> getFilesNodeMatchingName(String targetedFileName) throws InterruptedException {
        Executor executor = new Executor(threadNumber);
        ConcurrentHashMap<Long, String> results = new ConcurrentHashMap<>();
        long size = transposedGraph.numNodes();
        logger.debug("Num of nodes: " + size);
        for (int thread = 0; thread < threadNumber; thread++) {
            long finalThread = thread;
            SwhUnidirectionalGraph graphCopy = transposedGraph.copy();
            executor.execute(() -> {
                for (long currentNodeId = finalThread; currentNodeId < size; currentNodeId = currentNodeId + threadNumber) {
                    if ((currentNodeId - finalThread) % 1000000 == 0) {
                        logger.info("Node " + currentNodeId + " over " + size + " thread " + finalThread + "-- Nodes founds :" + results.size());
                    }
                    ArcLabelledNodeIterator.LabelledArcIterator successors = graphCopy.labelledSuccessors(currentNodeId);
                    if (graphCopy.getNodeType(currentNodeId) == SwhType.CNT) {
                        long dstNode;
                        while ((dstNode = successors.nextLong()) >= 0) {
                            final DirEntry[] labels = (DirEntry[]) successors.label().get();
                            for (DirEntry label : labels) {
                                //If the destination node is a Directory
                                if (graphCopy.getNodeType(dstNode) == SwhType.DIR) {
                                    String currentFileName = new String(graphCopy.getLabelName(label.filenameId));
                                    if (currentFileName.equals(targetedFileName) && !results.containsKey(currentNodeId)) {
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
            });

        }
        executor.shutdown();
        //Waiting Tasks
        while (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
            logger.info("Node traversal completed, waiting for asynchronous tasks. Tasks performed " + executor.getCompletedTaskCount() + " over " + executor.getTaskCount());
            logger.info("Partial checkpoint");
            export_raw_results();
        }
        export_raw_results();

        logger.info("Total number of nodes found : " + results.size());
        int errorNb = results.values().stream().reduce(0,
                (subtotal, value) -> subtotal + ((value.equals("")) ? 1 : 0),                 //accumulator
                (subtotal1, subtotal2) -> subtotal1 + subtotal2); //combiner
        logger.info("Total number of error : " + errorNb);
        return results;


    }

    public Map<SWHID, String> toSwhIds(Map<Long, String> map) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> this.transposedGraph.getSWHID(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

    public void export_raw_results() {
        try (FileWriter f = new FileWriter("res.json");
        ) {
            Gson gson = new Gson();
            gson.toJson(results, f);
        } catch (IOException e) {
            throw new RuntimeException("Erro while saving", e);
        }
    }

}