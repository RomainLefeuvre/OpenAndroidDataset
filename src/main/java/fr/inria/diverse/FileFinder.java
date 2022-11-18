package fr.inria.diverse;

import fr.inria.diverse.model.Origin;
import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.ToolBox;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.IOException;
import java.util.*;

public class FileFinder extends GraphExplorer {
    public static String exportPath = Configuration.getInstance()
            .getExportPath() + "/FileFinder/result.json";

    final List<Result> results = new ArrayList<>();
    List<Origin> origins;

    public FileFinder(Graph graph) {
        super(graph);
    }


    @Override
    void exploreGraphNodeAction(long currentNodeId, SwhUnidirectionalGraph graphCopy) {
        findTargetedNode(origins.get((int) currentNodeId), graphCopy);
    }

    @Override
    void exploreGraphNodeCheckpointAction() {
        synchronized (results) {
            ToolBox.exportObjectToJson(results, exportPath);
        }
    }

    @Override
    public void exploreGraphNode(long size) throws InterruptedException {
        super.exploreGraphNode(size);
        logger.info("Number of file node matching name found : " + results.size());

        //Add final save
        ToolBox.exportObjectToJson(results, exportPath);
    }

    /**
     * Find revision files/folder
     *
     * @param originNode
     */
    public void findTargetedNode(Origin originNode, SwhUnidirectionalGraph graphCopy) {
        DFSNode currentNode = new DFSNode(originNode.getSnapshot().getRev().getNodeId());
        Stack<DFSNode> stack = new Stack<>();
        HashSet<Long> visited = new HashSet<>();
        stack.push(currentNode);
        visited.add(originNode.getSnapshot().getRev().getNodeId());
        List<DFSNode> res = new LinkedList<>();
        while (!stack.isEmpty()) {
            currentNode = stack.pop();
            ArcLabelledNodeIterator.LabelledArcIterator it = graphCopy.labelledSuccessors(currentNode.getId());
            for (long neighborNodeId; (neighborNodeId = it.nextLong()) != -1; ) {
                SwhType neighborType = graphCopy.getNodeType(neighborNodeId);
                //Each revision point to the directory tree and an ordered list of all its parent ...
                //Let's check if it is a CNT or DIR node
                if (neighborType == SwhType.CNT || neighborType == SwhType.DIR) {
                    String label = "";
                    boolean labelsContainsTargetedFileName = false;
                    if (neighborType == SwhType.CNT) {
                        //Labels is a list since in the same folder you can have files with different names but with the same content.
                        final DirEntry[] labels = (DirEntry[]) it.label().get();
                        labelsContainsTargetedFileName = Arrays.stream(labels)
                                .anyMatch(l -> getFileName(l, graphCopy).equals(this.config.getTargetedFileName()));
                        //If labelsContainsTargetedFileName we take the targeted label, else we get the first one, it does not matter in our case;
                        label = labelsContainsTargetedFileName ? this.config.getTargetedFileName() :
                                (labels.length > 0 ? getFileName(labels[0], graphCopy) : "");
                    }

                    if (!visited.contains(neighborNodeId)) {
                        DFSNode neighborNode = currentNode.createChild(neighborNodeId, label);
                        stack.push(neighborNode);
                        visited.add(neighborNodeId);
                        //Let's check if it's an interesting node ...
                        if (neighborType == SwhType.CNT && labelsContainsTargetedFileName) {
                            logger.debug("It's a match, finding file node having requested filename " + neighborNode.getId());
                            res.add(neighborNode);

                        }
                    }
                }
            }
        }
        if (res.size() > 0) {
            synchronized (results) {
                this.results.add(new Result(originNode.getNodeId(), originNode.getOriginUrl(), res));
            }
        }
    }

    private String getFileName(DirEntry labelId, SwhUnidirectionalGraph graphCopy) {
        return new String(graphCopy.getLabelName(labelId.filenameId));
    }

    @Override
    void run() throws InterruptedException, IOException {
        try {
            logger.info("Loading origins");
            this.origins = ToolBox.deserialize(LastOriginFinder.exportPath);
            this.exploreGraphNode(this.origins.size());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error", e);
        }
    }

    public static class DFSNode {
        private String path;
        private long id;
        private String swhid;

        public DFSNode(long id) {
            this.path = "/";
            this.id = id;
            this.swhid = "";
        }

        public DFSNode(DFSNode node) {
            this.path = node.getPath();
            this.id = node.getId();
            this.swhid = node.getSwhid();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DFSNode dfsNode = (DFSNode) o;

            return id == dfsNode.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public DFSNode createChild(long id, String nodeName) {
            DFSNode res = new DFSNode(id);
            res.path = this.path;
            if (!nodeName.equals(""))
                res.path = res.path + nodeName + "/";
            return res;
        }

        public String getPath() {
            return path;
        }

        public String getSwhid() {
            return swhid;
        }

        public void setSwhid(String swhid) {
            this.swhid = swhid;
        }

    }


    public class Result {
        private long originId;
        private String originUrl;
        private List<DFSNode> fileNodes;

        public Result(long originId, String originUrl, List<DFSNode> fileNodes) {
            this.originId = originId;
            this.originUrl = originUrl;
            this.fileNodes = fileNodes;
            for (DFSNode node : fileNodes) {
                node.setSwhid(graph.getGraph().getSWHID(node.getId()).toString());
            }
        }

        public long getOriginId() {
            return originId;
        }

        public void setOriginId(long originId) {
            this.originId = originId;
        }

        public String getOriginUrl() {
            return originUrl;
        }

        public void setOriginUrl(String originUrl) {
            this.originUrl = originUrl;
        }

        public List<DFSNode> getFileNodes() {
            return fileNodes;
        }

        public void setFileNodes(List<DFSNode> fileNodes) {
            this.fileNodes = fileNodes;
        }
    }


}
