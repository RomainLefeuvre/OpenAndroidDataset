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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestSequential {
    final static Logger logger = LoggerFactory.getLogger(TestSequential.class);
    public static boolean startFromCheckpoint = true;
    static Gson gson = new Gson();
    String graphUri;
    SwhUnidirectionalGraph transposedGraph;

    public TestSequential(String graphUri) {
        this.graphUri = graphUri;

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Type listType = new TypeToken<List<Long>>() {
        }.getType();
        Instant inst1 = Instant.now();
        TestSequential test = new TestSequential("/home/rlefeuvr/Workspaces/SAND_BOX/SW_GRAPH/python_data/graph-transposed");
        test.loadTransposedGraph();
        List<Long> filesNodeIdMatchingName = null;
        if (!startFromCheckpoint) {
            filesNodeIdMatchingName = new ArrayList<>(test.getFilesNodeMatchingName("README"));
            try (FileWriter f = new FileWriter("tmp.json", StandardCharsets.UTF_8);
            ) {
                gson.toJson(filesNodeIdMatchingName, listType, f);
            }

        } else {
            filesNodeIdMatchingName = gson.fromJson(Files.newBufferedReader(Paths.get("tmp.json"), StandardCharsets.UTF_8), listType);
        }
        Map<Long, String> results = test.getOriginNodeFromFileNodeIds(filesNodeIdMatchingName);
        try (FileWriter f = new FileWriter("res.json");
        ) {
            gson.toJson(results, f);
        }
        Instant inst2 = Instant.now();

        System.out.println("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());
        System.out.println(results.keySet().iterator().next());

    }


    public void loadTransposedGraph() {
        ProgressLogger pl = new ProgressLogger(logger, 10, TimeUnit.SECONDS);
        try {
            transposedGraph = SwhUnidirectionalGraph.loadLabelled(this.graphUri, pl);
            transposedGraph.properties.loadMessages();
            transposedGraph.properties.loadLabelNames();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Long> getFilesNodeMatchingName(String fileName) throws InterruptedException {
        SwhUnidirectionalGraph localGraph = transposedGraph.copy();
        HashSet<Long> results = new HashSet<>();
        long size = transposedGraph.numNodes();
        System.out.println("Num of nodes: " + size);
        long i = 0;
        ArcLabelledNodeIterator it = localGraph.labelledNodeIterator();
        while (it.hasNext()) {
            long current = it.nextLong();
            i++;
            if (i % 1000000 == 0) {
                System.out.println(results.size());
                System.out.println("Node " + i + " over " + size);
            }
            if (localGraph.getNodeType(current) == SwhType.CNT) {
                ArcLabelledNodeIterator.LabelledArcIterator s = it.successors();
                long dstNode;
                while ((dstNode = s.nextLong()) >= 0) {
                    final DirEntry[] labels = (DirEntry[]) s.label().get();

                    for (DirEntry label : labels) {
                        //If the destination node is a file
                        if (localGraph.getNodeType(dstNode) == SwhType.DIR) {
                            String n = new String(localGraph.getLabelName(label.filenameId));
                            long finalDstNode = dstNode;
                            if (n.equals(fileName)) {
                                results.add(current);
                            }
                        }
                    }
                }
            }
        }


        System.out.println(results.size());
        return results;

    }

    public Map<Long, String> getOriginNodeFromFileNodeIds(List<Long> nodes) throws InterruptedException {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(14);
        ConcurrentHashMap<Long, String> results = new ConcurrentHashMap<>();
        for (Long node : nodes) {
            executor.submit(() -> {
                long originNode = getOriginNodeFromFileNode(node);
                String originUrl = transposedGraph.getUrl(originNode);
                results.put(node, originUrl);

            });
        }
        executor.shutdown();
        //Waiting Tasks
        while (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
            System.out.println("Node traversal completed, waiting for asynchronous tasks. Tasks performed " + executor.getCompletedTaskCount() + " over " + executor.getTaskCount());
        }
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