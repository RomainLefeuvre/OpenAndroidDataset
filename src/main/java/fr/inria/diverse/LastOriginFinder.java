package fr.inria.diverse;

import fr.inria.diverse.model.Branch;
import fr.inria.diverse.model.Origin;
import fr.inria.diverse.model.Snapshot;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

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
            System.out.println(graphCopy.getNodeType(currentNodeId));

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
                                logger.warn("not a revision as expected");
                            }
                            if (childIt.nextLong() != -1) {
                                logger.warn("iterator not ended as expected");
                            }
                        }

                        String url = new String(graphCopy.getLabelName(label.filenameId));
                        String branchName = url.replace("refs/heads/", "");
                        if (Branch.BranchType.isABranchType(branchName)) {
                            logger.info("Branch Name " + branchName);
                            Long currentTimestamp = graphCopy.getCommitterTimestamp(revNode);
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
                synchronized (origins) {
                    origins.add(currentOrigin);
                }
            }
        }
    }

    @Override
    void nodeListParrallelCheckpointAction() {
        this.exportFile(origins, "origins.json");
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
