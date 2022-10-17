package fr.inria.diverse;

import com.google.gson.Gson;
import fr.inria.diverse.model.Branch;
import fr.inria.diverse.model.Origin;
import fr.inria.diverse.model.Snapshot;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class NodeExplorerV3 {
    static Logger logger = LogManager.getLogger(NodeExplorerV3.class);
    //The results map <ID,origin>
    private final Map<Long, List<String>> results;
    public Configuration config = Configuration.getInstance();
    private SwhUnidirectionalGraph graph;

    public NodeExplorerV3() {
        this.results = new ConcurrentHashMap<>();
    }

    public static void main(String[] args) throws InterruptedException {
        Instant inst1 = Instant.now();
        NodeExplorerV3 nodeExplorer = new NodeExplorerV3();
        nodeExplorer.loadTransposedGraph();
        logger.info(nodeExplorer.graph.getPath());

        nodeExplorer.traverseNodeList();
        //nodeExplorer.getFilesNodeMatchingName();
        Instant inst2 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());
        // nodeExplorer.export_shwid_results();
    }


    public void getAllOriginNode() {

    }

    /**
     * Load the transposed Graph
     */
    public void loadTransposedGraph() {
        try {

            logger.info("Loading graph " + (this.isMappedMemoryActivated() ? "MAPPED MODE" : ""));
            graph = SwhUnidirectionalGraph.loadLabelled(this.config.getGraphPath());
            graph.loadCommitterTimestamps();

            logger.info("Graph loaded");
            logger.info("Loading message");
            graph.properties.loadMessages();
            logger.info("Message loaded");
            logger.info("Loading label");
            graph.properties.loadLabelNames();
            logger.info("Label loaded");
        } catch (IOException e) {
            throw new RuntimeException("Error while loading the graph", e);
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
        List<Origin> origins = new LinkedList<>();
        Set<Long> originsId = new HashSet<>();
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
                    if (graphCopy.getNodeType(currentNodeId) == SwhType.ORI) {
                        String originUrl = graphCopy.getUrl(currentNodeId);
                        originUrl = originUrl != null ? originUrl : "";
                        if (originUrl == "") {
                            logger.warn("Skipping origin node " + currentNodeId + " because its url is empty");
                        } else {
                            Origin currentOrigin = new Origin(originUrl, currentNodeId);
                            //  findLastSnap(currentOrigin);
                            synchronized (originsId) {
                                originsId.add(currentNodeId);
                            }
                        }
                    }
                }
            });
        }
        executor.shutdown();
        //Waiting Tasks
        while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            logger.info("Node traversal completed, waiting for asynchronous tasks. Tasks performed " + executor.getCompletedTaskCount() + " over " + executor.getTaskCount());
            logger.info("Partial checkpoint");
            //  export_raw_results();
        }

        logger.info("Total origin  found : " + originsId.size());
        // logger.info("Total of unique origin found : " + origins.stream().collect(Collectors.toSet()).size());

        //logger.info("Total number of error : " + errorNb);
        //return origins;
    }

    public void findLastSnap(Origin originNode) {
        Queue<Long> queue = new ArrayDeque<>();
        HashSet<Long> visited = new HashSet<Long>();
        queue.add(originNode.getNodeId());
        visited.add(originNode.getNodeId());

        while (!queue.isEmpty()) {
            long currentNodeId = queue.poll();
            System.out.println(graph.getNodeType(currentNodeId));

            ArcLabelledNodeIterator.LabelledArcIterator it = graph.copy().labelledSuccessors(currentNodeId);
            for (long neighborNodeId; (neighborNodeId = it.nextLong()) != -1; ) {
                if (graph.getNodeType(currentNodeId) == SwhType.SNP) {

                    final DirEntry[] labels = (DirEntry[]) it.label().get();
                    for (DirEntry label : labels) {

                        //Getting the first revision node
                        final Long revNode;
                        if (graph.getNodeType(neighborNodeId) == SwhType.REV) {
                            revNode = neighborNodeId;
                        } else {
                            //We probably find a release node, lets get a rev node!
                            ArcLabelledNodeIterator.LabelledArcIterator childIt = graph.copy()
                                    .labelledSuccessors(neighborNodeId);
                            revNode = childIt.nextLong();
                            if (graph.getNodeType(revNode) != SwhType.REV) {
                                logger.warn("not a revision as expected");
                            }
                            if (childIt.nextLong() != -1) {
                                logger.warn("iterator not ended as expected");
                            }
                        }

                        String url = new String(graph.getLabelName(label.filenameId));
                        String branchName = url.replace("refs/heads/", "");
                        if (Branch.BranchType.isABranchType(branchName)) {
                            logger.info("Branch Name " + branchName);
                            Long currentTimestamp = graph.getCommitterTimestamp(revNode);
                            if (currentTimestamp != null) {
                                Snapshot currentSnap = new Snapshot(branchName, currentTimestamp, currentNodeId);
                                originNode.checkSnapshotAndUpdate(currentSnap);
                            } else {
                                logger.warn("Impossible to get current revision timestamp for revision " + currentNodeId);
                            }
                        } else {
                            logger.warn("Not a valid branch name " + branchName);
                        }


                    }
                } else {
                    if (!visited.contains(neighborNodeId)) {
                        queue.add(neighborNodeId);
                        visited.add(neighborNodeId);
                    }
                }
            }
        }

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

    public void visitNodesDFS(long srcNodeId) {
        Stack<Long> stack = new Stack<>();
        HashSet<Long> visited = new HashSet<Long>();
        stack.push(srcNodeId);
        visited.add(srcNodeId);

        while (!stack.isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long currentNodeId = stack.pop();
            System.out.println(graph.getNodeType(currentNodeId));

            LazyLongIterator it = graph.successors(currentNodeId);
            for (long neighborNodeId; (neighborNodeId = it.nextLong()) != -1; ) {
                if (!visited.contains(neighborNodeId)) {
                    stack.push(neighborNodeId);
                    visited.add(neighborNodeId);
                }
            }
        }
    }

    /**
     * Return the results hash map with swh id instead of graph id as keys
     *
     * @return the swhid map
     */
    public Map<SWHID, List<String>> toSwhIds() {
        return results.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> this.graph.getSWHID(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

}