package fr.inria.diverse;

import fr.inria.diverse.model.Branch;
import fr.inria.diverse.model.Origin;
import fr.inria.diverse.model.Revision;
import fr.inria.diverse.model.Snapshot;
import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.ToolBox;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.bits.Fast;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static it.unimi.dsi.big.webgraph.labelling.BitStreamArcLabelledImmutableGraph.LABELS_EXTENSION;

public class LastOriginFinder extends GraphExplorer {

    public static String rawExportPath = Configuration.getInstance()
            .getExportPath() + "/LastOriginFinder/origins.json";
    public static String exportPath = Configuration.getInstance()
            .getExportPath() + "/LastOriginFinder/originsFiltered.json";
    private final List<Origin> origins = new LinkedList<>();
    public long size;

    public LastOriginFinder(Graph graph) throws FileNotFoundException {
        super(graph);
        FileInputStream fis = new FileInputStream(this.config.getGraphPath() + "-labelled" + LABELS_EXTENSION);
        try {
            size = fis.getChannel().size();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            long l = Fast.mostSignificantBit((size * Byte.SIZE + 1) / (graphCopy.numNodes() + 1));
            long a = l * currentNodeId;
            logger.info("For node " + currentNodeId + " a is " + a + " l is " + l);
            if (a >>> 6 > Integer.MAX_VALUE) {

                logger.error("Will crash node " + currentNodeId);
            }

            ArcLabelledNodeIterator.LabelledArcIterator it = graphCopy.labelledSuccessors(currentNodeId);
            for (long neighborNodeId; (neighborNodeId = it.nextLong()) != -1; ) {
                if (graphCopy.getNodeType(currentNodeId) == SwhType.SNP) {
                    final DirEntry[] labels = (DirEntry[]) it.label().get();
                    DirEntry label = labels[0];

                    //Getting the first revision node
                    final Long revNode;
                    if (graphCopy.getNodeType(neighborNodeId) == SwhType.REV) {
                        revNode = neighborNodeId;
                    } else {
                        //We probably find a release node, lets get a rev node!
                        LazyLongIterator childIt = (graphCopy.copy())
                                .successors(neighborNodeId);


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
    void exploreGraphNodeAction(long currentNodeId, SwhUnidirectionalGraph graphCopy) {
        if (graphCopy.getNodeType(currentNodeId) == SwhType.ORI) {
            String originUrl = graphCopy.getUrl(currentNodeId);
            originUrl = originUrl != null ? originUrl : "";
            if (originUrl.equals("")) {
                logger.warn("Skipping origin node " + currentNodeId + " because its url is empty");
            } else {
                Origin currentOrigin = new Origin(originUrl, currentNodeId);
                //findLastSnap(currentOrigin, graphCopy.copy());
                findLastSnap(currentOrigin, graphCopy);
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
            ToolBox.exportFile(origins, exportPath);
        }
    }

    @Override
    public void exploreGraphNode(long size) throws InterruptedException {
        super.exploreGraphNode(size);
        //Add final save
        ToolBox.exportFile(origins, rawExportPath);
        ToolBox.exportFile(origins.stream().filter(origin -> origin.getSnapshot() != null)
                .collect(Collectors.toList()), exportPath);
    }

    @Override
    void run() {
        try {
            this.exploreGraphNodeSequentialy(graph.getGraph().numNodes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error", e);
        }
    }

}
