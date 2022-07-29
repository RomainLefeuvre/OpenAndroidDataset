/*
 * Copyright (c) 2020-2022 The Software Heritage developers
 * See the AUTHORS file at the top-level directory of this distribution
 * License: GNU General Public License version 3, or any later version
 * See top-level LICENSE file for more information
 */

package fr.inria.diverse;

import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.logging.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.softwareheritage.graph.SwhBidirectionalGraph;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TestAsync {
    final static Logger logger = LoggerFactory.getLogger(TestAsync.class);
    SwhBidirectionalGraph graph;

    public TestAsync(SwhBidirectionalGraph graph) {
        this.graph = graph;
        try {
            graph.properties.loadLabelNames();
            graph.properties.loadMessages();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    public ConcurrentHashMap<Long, String> getFilesNodeMatchingName(String fileName) throws InterruptedException {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(14);
        ConcurrentHashMap<Long, String> results = new ConcurrentHashMap<>();
        long size = graph.numNodes();
        System.out.println("Num of nodes: " + size);
        long i = 0;
        ArcLabelledNodeIterator it = graph.getForwardGraph().labelledNodeIterator();

        while (it.hasNext()) {
            long current = it.nextLong();
            i++;
            if (i % 1000000 == 0) {
                System.out.println(results.size());
                System.out.println("Node " + i + " over " + size);
            }
            if (graph.getNodeType(current) == SwhType.DIR) {
                ArcLabelledNodeIterator.LabelledArcIterator s = it.successors();
                long dstNode;
                while ((dstNode = s.nextLong()) >= 0) {
                    final DirEntry[] labels = (DirEntry[]) s.label().get();

                    for (DirEntry label : labels) {
                        //If the destination node is a file

                        if (graph.getNodeType(dstNode) == SwhType.CNT) {
                            String n = new String(graph.getLabelName(label.filenameId));
                            long finalDstNode = dstNode;
                            if (n.equals(fileName) ) {
                                executor.submit(() -> {
                                    if(!results.containsKey(finalDstNode)){
                                        long originNode = getOriginNodeFromFileNode(finalDstNode);
                                        String originUrl = graph.getUrl(originNode);
                                        results.put(finalDstNode, originUrl);
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }
        executor.shutdown();
        //Waiting Tasks
        while (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
            System.out.println("Node traversal completed, waiting for asynchronous tasks. Tasks performed "+executor.getCompletedTaskCount()+" over "+ executor.getTaskCount());
        }

        System.out.println(results.size());
        return results;
    }

    /**
     * Retrieve the OriginNodeId from a FileNodeId
     *
     * @param nodeId
     * @return originNodeId
     */
    public long getOriginNodeFromFileNode(long nodeId) {
        SwhBidirectionalGraph graph_copy = graph.copy();
        LazyLongIterator it = graph_copy.predecessors(nodeId);
        Long pred = it.nextLong();
        Long current = nodeId;
        while (pred != -1 && graph_copy.getNodeType(current) != SwhType.ORI) {
            current = pred;
            pred = graph_copy.predecessors(current).nextLong();
        }
        return current;

    }


    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        ProgressLogger pl = new ProgressLogger(logger, 10, TimeUnit.SECONDS);
        TestAsync test = new TestAsync(SwhBidirectionalGraph.loadLabelled("/home/rlefeuvr/Workspaces/SAND_BOX/SW_GRAPH/python_data/graph", pl));
        Instant inst1 = Instant.now();
        test.getFilesNodeMatchingName("README");
        Instant inst2 = Instant.now();

        System.out.println("Elapsed Time: " + Duration.between(inst1, inst2).toMinutes());


    }
}