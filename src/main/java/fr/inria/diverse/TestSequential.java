/*
 * Copyright (c) 2020-2022 The Software Heritage developers
 * See the AUTHORS file at the top-level directory of this distribution
 * License: GNU General Public License version 3, or any later version
 * See top-level LICENSE file for more information
 */

package fr.inria.diverse;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.logging.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TestSequential {
    final static Logger logger = LoggerFactory.getLogger(TestSequential.class);
    static Gson gson = new Gson();
    String graphUri;
    String transposedGraphUri;
    SwhUnidirectionalGraph graph;
    SwhUnidirectionalGraph transposedGraph;

    public TestSequential(String graphUri, String transposedGraphUri) {
        this.graphUri = graphUri;
        this.transposedGraphUri = transposedGraphUri;

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Type listType = new TypeToken<List<Long>>() {
        }.getType();
        Instant inst1 = Instant.now();
        TestSequential test = new TestSequential("/home/rlefeuvr/Workspaces/SAND_BOX/SW_GRAPH/python_data/graph", "/home/rlefeuvr/Workspaces/SAND_BOX/SW_GRAPH/python_data/graph-transposed");

        test.loadForwardGraph();


        List<Long> filesNodeIdMatchingName = new ArrayList<>(test.getFilesNodeMatchingName("README"));
        try (FileWriter f = new FileWriter("tmp.json");
        ) {
            gson.toJson(filesNodeIdMatchingName, listType, f);
        }

        test.wipeForwardGraph();

        test.loadTransposedGraph();
        filesNodeIdMatchingName = gson.fromJson(Files.newBufferedReader(Paths.get("tmp.json")), listType);
        Map<Long, String> results = test.getOriginNodeFromFileNodeIds(filesNodeIdMatchingName);

        Instant inst2 = Instant.now();

        System.out.println("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());
        System.out.println(results.keySet().iterator().next());

    }

    public void loadForwardGraph() {
        ProgressLogger pl = new ProgressLogger(logger, 10, TimeUnit.SECONDS);
        try {
            graph = SwhUnidirectionalGraph.loadLabelled(this.graphUri, pl);
            graph.properties.loadLabelNames();
            graph.properties.loadMessages();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void wipeForwardGraph() {
        this.graph = null;
        System.gc();
    }

    public void loadTransposedGraph() {
        ProgressLogger pl = new ProgressLogger(logger, 10, TimeUnit.SECONDS);
        try {
            transposedGraph = SwhUnidirectionalGraph.load(this.transposedGraphUri, pl);
            transposedGraph.properties.loadMessages();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Long> getFilesNodeMatchingName(String fileName) throws InterruptedException {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(14);
        HashSet<Long> results = new HashSet<>();
        long size = graph.numNodes();
        System.out.println("Num of nodes: " + size);
        long i = 0;
        ArcLabelledNodeIterator it = graph.labelledNodeIterator();

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
                            if (n.equals(fileName)) {
                                results.add(dstNode);
                            }
                        }
                    }
                }
            }
        }
        executor.shutdown();
        //Waiting Tasks
        while (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
            System.out.println("Node traversal completed, waiting for asynchronous tasks. Tasks performed " + executor.getCompletedTaskCount() + " over " + executor.getTaskCount());
        }

        System.out.println(results.size());
        return results;

    }

    public Map<Long, String> getOriginNodeFromFileNodeIds(List<Long> nodes) {
        Map<Long, String> results = nodes.parallelStream().map((id) -> {
            long originNode = getOriginNodeFromFileNode(id);
            String originUrl = transposedGraph.getUrl(originNode);
            return new AbstractMap.SimpleEntry<Long, String>(id, originUrl == null ? "" : originUrl);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return results;
    }


    /**
     * Retrieve the OriginNodeId from a FileNodeId
     *
     * @param nodeId
     * @return originNodeId
     */
    public long getOriginNodeFromFileNode(long nodeId) {
        SwhUnidirectionalGraph graph_copy = transposedGraph.copy();
        LazyLongIterator it = graph_copy.successors(nodeId);
        Long pred = it.nextLong();
        Long current = nodeId;
        while (pred != -1 && graph_copy.getNodeType(current) != SwhType.ORI) {
            current = pred;
            pred = graph_copy.successors(current).nextLong();
        }
        return current;
    }


}