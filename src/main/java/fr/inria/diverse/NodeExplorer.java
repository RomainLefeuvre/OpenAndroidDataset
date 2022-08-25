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

public class NodeExplorer {
    public static int threadNumber = 14;
    static Gson gson = new Gson();
    private static Logger logger = LogManager.getLogger(NodeExplorer.class);
    String graphUri;
    SwhUnidirectionalGraph transposedGraph;

    public NodeExplorer(String graphUri) {
        this.graphUri = graphUri;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Instant inst1 = Instant.now();
        NodeExplorer nodeExplorer = new NodeExplorer("/home/rlefeuvr/Workspaces/SAND_BOX/SW_GRAPH/python_smallest_data/graph-transposed");
        nodeExplorer.loadTransposedGraph();
        Map<Long, String> results = nodeExplorer.getFilesNodeMatchingName("README");
        Instant inst2 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());
        try (FileWriter f = new FileWriter("res.json");
        ) {
            gson.toJson(results, f);
        }
    }

    /**
     * Retrieve the OriginNodeId from a FileNodeId
     *
     * @param nodeId
     * @return originNodeId
     */
    public static long getOriginNodeFromFileNode(long nodeId, SwhUnidirectionalGraph graph_copy) {
        LazyLongIterator it = graph_copy.successors(nodeId);
        Long pred = it.nextLong();
        Long current = nodeId;
        while (pred != -1 && graph_copy.getNodeType(current) != SwhType.ORI) {
            current = pred;
            pred = graph_copy.successors(current).nextLong();
        }
        return current;
    }

    public void loadTransposedGraph() {
        try {
            transposedGraph = SwhUnidirectionalGraph.loadLabelled(this.graphUri);
            transposedGraph.properties.loadMessages();
            transposedGraph.properties.loadLabelNames();
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
                                    boolean done = false;
                                    while (!done) {
                                        String currentFileName = "";
                                        currentFileName = new String(graphCopy.getLabelName(label.filenameId));
                                        if (currentFileName.equals(targetedFileName) && !results.containsKey(currentNodeId)) {
                                            long originNodeId = getOriginNodeFromFileNode(currentNodeId, graphCopy);
                                            String originUrl = transposedGraph.getUrl(originNodeId);
                                            if (originUrl != null) {
                                                results.put(currentNodeId, originUrl);
                                            } else {
                                                results.put(currentNodeId, "");
                                                logger.warn("Origin not found for file node :" + currentNodeId);
                                            }

                                        }
                                        done = true;
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
        while (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
            logger.info("Node traversal completed, waiting for asynchronous tasks. Tasks performed " + executor.getCompletedTaskCount() + " over " + executor.getTaskCount());
        }
        logger.info("Total number of nodes found : " + results.size());
        return results;
    }


}