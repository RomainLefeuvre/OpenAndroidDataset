package fr.inria.diverse;

import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.Executor;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.NodeIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TEST {
    static Logger logger = LogManager.getLogger(TEST.class);
    //The results map <ID,origin>
    private final Map<Long, List<String>> results;
    public Configuration config = Configuration.getInstance();
    private SwhUnidirectionalGraph graph;

    public TEST() {
        this.results = new ConcurrentHashMap<>();
    }

    public static void main(String[] args) throws InterruptedException {
        Instant inst1 = Instant.now();
        TEST nodeExplorer = new TEST();

        nodeExplorer.loadGraph();
        logger.info(nodeExplorer.graph.getPath());

        nodeExplorer.simpleTraversal();
        Instant inst2 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());
    }


    /**
     * Load the transposed Graph
     */
    public void loadGraph() {
        try {

            logger.info("Loading graph ");
            graph = SwhUnidirectionalGraph.load(this.config.getGraphPath());
            logger.info("Graph loaded");

        } catch (IOException e) {
            throw new RuntimeException("Error while loading the graph", e);
        }
    }

    public void simpleTraversal() throws InterruptedException {
        NodeIterator it = graph.nodeIterator();
        long node;
        while ((node = it.nextLong()) != -1) {
            // if (graph.getNodeType(node) == SwhType.ORI) {

            logger.info(node + "  " + graph.getNodeType(node));
            Thread.sleep(1000);

            // graph.get

//            }

        }
        // graph.
    }

    public void visitNodesBFS() {
        NodeIterator it1 = graph.nodeIterator();
        it1.nextLong();
        long srcNodeId = it1.nextLong();
        Queue<Long> queue = new ArrayDeque<>();
        HashSet<Long> visited = new HashSet<Long>();
        queue.add(srcNodeId);
        visited.add(srcNodeId);

        while (!queue.isEmpty()) {
            long currentNodeId = queue.poll();
            System.out.println(currentNodeId);

            LazyLongIterator it = graph.successors(currentNodeId);
            for (long neighborNodeId; (neighborNodeId = it.nextLong()) != -1; ) {
                if (!visited.contains(neighborNodeId)) {
                    queue.add(neighborNodeId);
                    visited.add(neighborNodeId);
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
    public void traverseNodeList() throws InterruptedException {
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
                        logger.info("Node " + currentNodeId + " over " + size + " thread " + finalThread + "-- Nodes founds :" + results.size());
                    }
                    //Action
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.info(currentNodeId + "  " + graphCopy.getNodeType(currentNodeId));
                    if (graphCopy.getNodeType(currentNodeId) == SwhType.ORI) {
                        synchronized (origins) {
                            origins.add(currentNodeId);
                        }
                    } else {
                        //break;
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
        }

        logger.info("Total origin  found : " + origins.size());
        logger.info("Total of unique origin found : " + origins.stream().collect(Collectors.toSet()).size());


    }


}