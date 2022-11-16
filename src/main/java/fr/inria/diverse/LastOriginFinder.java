package fr.inria.diverse;

import com.google.common.reflect.TypeToken;
import fr.inria.diverse.model.Branch;
import fr.inria.diverse.model.Origin;
import fr.inria.diverse.model.Revision;
import fr.inria.diverse.model.Snapshot;
import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.ToolBox;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class LastOriginFinder extends GraphExplorer {

    public static String rawExportPath = Configuration.getInstance()
            .getExportPath() + "/LastOriginFinder/origins";
    public static String exportPath = Configuration.getInstance()
            .getExportPath() + "/LastOriginFinder/originsFiltered";
    private final List<Origin> origins = new LinkedList<>();
    private final List<Long> originNodeIds;

    public LastOriginFinder(Graph graph) {
        super(graph);
        Type listType = new TypeToken<ArrayList<Long>>() {
        }.getType();
        this.originNodeIds = ToolBox.loadJsonObject(OriginFinder.exportPath + ".json", listType);
    }

    /**
     * Get the last snapshot for a given origin, the result is saved in the snaphsot attribute of the origin passed in parameter
     *
     * @param originNode the origin node we want to process
     * @param graphCopy  the current graphCopy (thread safe approach)
     */
    private void findLastSnap(Origin originNode, SwhUnidirectionalGraph graphCopy) {
        Queue<Long> queue = new ArrayDeque<>();
        HashSet<Long> visited = new HashSet<>();
        queue.add(originNode.getNodeId());
        visited.add(originNode.getNodeId());
        while (!queue.isEmpty()) {
            long currentNodeId = queue.poll();
            ArcLabelledNodeIterator.LabelledArcIterator it = graphCopy.copy().labelledSuccessors(currentNodeId);
            for (long neighborNodeId; (neighborNodeId = it.nextLong()) != -1; ) {
                if (graphCopy.getNodeType(currentNodeId) == SwhType.SNP) {
                    final DirEntry[] labels = (DirEntry[]) it.label().get();
                    DirEntry label = labels[0];
                    long revisionNodeId = this.getFirstRevisionNode(neighborNodeId, graphCopy);
                    if (revisionNodeId != -1) {
                        String url = new String(graphCopy.getLabelName(label.filenameId));
                        String branchName = url.replace("refs/heads/", "");
                        if (Branch.BranchType.isABranchType(branchName)) {
                            logger.debug("Branch Name " + branchName);
                            Long currentTimestamp = graphCopy.getCommitterTimestamp(revisionNodeId);
                            if (currentTimestamp != null) {
                                Revision currentRevision = new Revision(revisionNodeId, currentTimestamp);
                                Snapshot currentSnap = new Snapshot(branchName, currentNodeId, currentRevision);
                                originNode.checkSnapshotAndUpdate(currentSnap);
                            } else {
                                logger.debug("Impossible to get current revision timestamp for revision " + currentNodeId);
                            }
                        } else {
                            logger.debug("Not a valid branch name " + branchName);
                        }
                    }
                    //For the first iteration , the current node id is an origin
                } else if (graphCopy.getNodeType(currentNodeId) == SwhType.ORI) {
                    if (!visited.contains(neighborNodeId)) {
                        queue.add(neighborNodeId);
                        visited.add(neighborNodeId);
                    }
                } else {
                    logger.error("Error while finding last snapshot, for " + originNode.getNodeId());
                    throw new RuntimeException("Not a snapshot node or an origin node");
                }
            }
        }

    }

    /**
     * Get the first revision Node of a child of a snapshot node
     *
     * @param snapNodeChildId
     * @return the id of the first revision node
     */
    private long getFirstRevisionNode(long snapNodeChildId, SwhUnidirectionalGraph graphCopy) {
        Long revNode = -1L;
        //If the child is a rev, it's over
        if (graphCopy.getNodeType(snapNodeChildId) == SwhType.REV) {
            revNode = snapNodeChildId;
        } else {
            //Else, we probably find a release node, lets get a rev node!
            LazyLongIterator childIt = (graphCopy.copy())
                    .successors(snapNodeChildId);
            Long candidateNode = childIt.nextLong();
            //In some cases a release node can have another release node as child, ToDo understand how it is possible
            while (candidateNode != null && candidateNode != -1 && graphCopy.getNodeType(candidateNode) == SwhType.REL) {
                childIt = (graphCopy.copy())
                        .successors(candidateNode);
                candidateNode = childIt.nextLong();
            }

            if (candidateNode != null && candidateNode != -1 && graphCopy.getNodeType(candidateNode) == SwhType.REV) {
                revNode = candidateNode;
            } else {
                logger.warn("Not a revision as expected " + graphCopy.getNodeType(candidateNode) +
                        " instead for candidate node " + candidateNode + " unable to find rev node for " + snapNodeChildId + " snap child");
            }
            if (childIt.nextLong() != -1) {
                logger.warn("Iterator not ended as expected at candidate node " + candidateNode);
            }
        }
        return revNode;

    }

    @Override
    void exploreGraphNodeAction(long i, SwhUnidirectionalGraph graphCopy) {
        long currentNodeId = this.originNodeIds.get((int) i);
        if (graphCopy.getNodeType(currentNodeId) == SwhType.ORI) {
            String originUrl = graphCopy.getUrl(currentNodeId);
            originUrl = originUrl != null ? originUrl : "";
            if (originUrl.equals("")) {
                logger.warn("Skipping origin node " + currentNodeId + " because its url is empty");
            } else {
                Origin currentOrigin = new Origin(originUrl, currentNodeId);
                findLastSnap(currentOrigin, graphCopy.copy());
                if (currentOrigin.getSnapshot() != null)
                    synchronized (origins) {
                        origins.add(currentOrigin);
                    }
            }
        }
    }

    @Override
    void exploreGraphNodeCheckpointAction() {
        synchronized (origins) {
            ToolBox.serialize(origins, exportPath);
        }
    }

    @Override
    public void exploreGraphNode(long size) throws InterruptedException {
        super.exploreGraphNode(size);
        //Add final save
        ToolBox.serialize(origins, exportPath);

        ToolBox.exportObjectToJson(origins, rawExportPath + ".json");
        ToolBox.exportObjectToJson(origins.stream().filter(origin -> origin.getSnapshot() != null)
                .collect(Collectors.toList()), exportPath + ".json");
    }

    @Override
    void run() {
        try {
            this.exploreGraphNode(this.originNodeIds.size());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error", e);
        }
    }

}
