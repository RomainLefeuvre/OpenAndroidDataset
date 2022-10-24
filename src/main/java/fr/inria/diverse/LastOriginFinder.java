package fr.inria.diverse;

import fr.inria.diverse.model.Branch;
import fr.inria.diverse.model.Origin;
import fr.inria.diverse.model.Revision;
import fr.inria.diverse.model.Snapshot;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class LastOriginFinder extends GraphExplorer {
    List<Origin> origins = new LinkedList<>();

    public LastOriginFinder() {
        super();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Instant inst1 = Instant.now();
        GraphExplorer lastOriginFinder = new LastOriginFinder();
        lastOriginFinder.run();
        Instant inst2 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());
    }

    void findLastSnap(Origin originNode, SwhUnidirectionalGraph graphCopy) {
        Queue<Long> queue = new ArrayDeque<>();
        HashSet<Long> visited = new HashSet<Long>();
        queue.add(originNode.getNodeId());
        visited.add(originNode.getNodeId());

        while (!queue.isEmpty()) {
            long currentNodeId = queue.poll();
            ArcLabelledNodeIterator.LabelledArcIterator it = graphCopy.labelledSuccessors(currentNodeId);
            for (long neighborNodeId; (neighborNodeId = it.nextLong()) != -1; ) {
                if (graphCopy.getNodeType(currentNodeId) == SwhType.SNP) {

                    final DirEntry[] labels = (DirEntry[]) it.label().get();
                    for (DirEntry label : labels) {

                        //Getting the first revision node
                        final Long revNode;
                        if (graphCopy.getNodeType(neighborNodeId) == SwhType.REV) {
                            revNode = neighborNodeId;
                        } else {
                            //We probably find a release node, lets get a rev node!
                            ArcLabelledNodeIterator.LabelledArcIterator childIt = graphCopy.copy()
                                    .labelledSuccessors(neighborNodeId);
                            revNode = childIt.nextLong();
                            if (graphCopy.getNodeType(revNode) != SwhType.REV) {
                                logger.warn("Not a revision as expected " + graphCopy.getNodeType(revNode) +
                                        " instead for current node " + currentNodeId);
                            }
                            if (childIt.nextLong() != -1) {
                                logger.warn("Iterator not ended as expected at current node " + currentNodeId);
                            }
                        }

                        String url = new String(graphCopy.getLabelName(label.filenameId));
                        String branchName = url.replace("refs/heads/", "");
                        if (Branch.BranchType.isABranchType(branchName)) {
                            logger.debug("Branch Name " + branchName);
                            Long currentTimestamp = graphCopy.getCommitterTimestamp(revNode);
                            if (currentTimestamp != null) {
                                Revision currentRevision = new Revision(revNode, currentTimestamp);
                                Snapshot currentSnap = new Snapshot(branchName, currentNodeId, currentRevision);
                                originNode.checkSnapshotAndUpdate(currentSnap);
                            } else {
                                logger.debug("Impossible to get current revision timestamp for revision " + currentNodeId);
                            }
                        } else {
                            logger.debug("Not a valid branch name " + branchName);
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
     * Not relevant, as each revision points to its parents...
     * Maybe browse the transposed graph? But I'm not even sure it's useful.
     * So let's drop it
     *
     * @param snapshot
     * @param graphCopy
     */
    public void findLastRevision(Snapshot snapshot, SwhUnidirectionalGraph graphCopy) {
        Queue<Long> queue = new ArrayDeque<>();
        HashSet<Long> visited = new HashSet<Long>();
        queue.add(snapshot.getRev().getNodeId());
        visited.add(snapshot.getRev().getNodeId());

        while (!queue.isEmpty()) {
            long currentNodeId = queue.poll();
            SwhType currentNodeType = graphCopy.getNodeType(currentNodeId);
            if (currentNodeType == SwhType.REV) {

                Long currentTimestamp = graphCopy.getCommitterTimestamp(currentNodeId);
                if (currentTimestamp != null) {
                    Revision currentRev = new Revision(currentNodeId, currentTimestamp);
                    if (currentRev.compareTo(snapshot.getRev()) > 1) {
                        logger.info("Updating snapshot rev from " + snapshot.getRev()
                                .getCommitTimestamp() + " to " + currentRev.getCommitTimestamp());
                        snapshot.setRev(currentRev);
                    }
                } else {
                    logger.warn("Something went wrong, the timestamp of node " + currentNodeId + " is null");
                }
            }

            ArcLabelledNodeIterator.LabelledArcIterator it = graphCopy.labelledSuccessors(currentNodeId);
            for (long neighborNodeId; (neighborNodeId = it.nextLong()) != -1; ) {
                if (graphCopy.getNodeType(neighborNodeId) == SwhType.REV && !visited.contains(neighborNodeId)) {
                    queue.add(neighborNodeId);
                    visited.add(neighborNodeId);
                }
            }
        }
    }

    @Override
    void nodeListParrallelTraversalAction(long currentNodeId, SwhUnidirectionalGraph graphCopy) {
        if (graphCopy.getNodeType(currentNodeId) == SwhType.ORI) {
            String originUrl = graphCopy.getUrl(currentNodeId);
            originUrl = originUrl != null ? originUrl : "";
            if (originUrl == "") {
                logger.warn("Skipping origin node " + currentNodeId + " because its url is empty");
            } else {
                Origin currentOrigin = new Origin(originUrl, currentNodeId);
                findLastSnap(currentOrigin, graphCopy);
                if (currentOrigin.getSnapshot() != null)
                    //Not relevant .. see function comment ...
                    // findLastRevision(currentOrigin.getSnapshot(), graphCopy);
                    synchronized (origins) {
                        origins.add(currentOrigin);
                    }
            }
        }
    }

    @Override
    void nodeListParrallelCheckpointAction() {
        synchronized (origins) {
            this.exportFile(origins, "origins.json");
        }
    }

    void nodeListEndCheckpointAction() {
        this.exportFile(origins, "origins.json");
        this.exportFile(origins.stream().filter(origin -> origin.getSnapshot() != null)
                .collect(Collectors.toList()), "originsFiltered.json");
    }


    @Override
    void run() {
        try {
            this.loadGraph();
            this.nodeListParrallelTraversal();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error", e);
        }
    }

}
