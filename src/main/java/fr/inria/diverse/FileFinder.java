package fr.inria.diverse;

import fr.inria.diverse.model.Origin;
import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.ToolBox;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.Serializable;
import java.util.*;

public class FileFinder extends GraphExplorer<ArrayList<FileFinder.Result>> {
    public static String exportPath = Configuration.getInstance()
            .getExportPath() + "/FileFinder/result";

    //Input from LastOriginFinder
    private List<Origin> origins;

    public FileFinder(Graph graph) {
        super(graph);
        this.result=new ArrayList<>();
        logger.info("Loading origins");
        this.origins = ToolBox.deserialize(LastOriginFinder.exportPath);
        if(this.origins==null){
            throw new RuntimeException("No origins");
        }
    }


    @Override
    protected void exploreGraphNodeAction(long currentNodeId, SwhUnidirectionalGraph graphCopy) {
        findTargetedNode(origins.get((int) currentNodeId), graphCopy);
    }

    @Override
    protected String getExportPath() {
        return exportPath;
    }
    @Override
    public void exploreGraphNode(long size) throws InterruptedException {
        super.exploreGraphNode(size);
        logger.info("Number of file node matching name found : " + result.size());
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
                                .anyMatch(l -> ToolBox.getFileName(l, graphCopy).equals(this.config.getTargetedFileName()));
                        //If labelsContainsTargetedFileName we take the targeted label, else we get the first one, it does not matter in our case;
                        label = labelsContainsTargetedFileName ? this.config.getTargetedFileName() :
                                (labels.length > 0 ? ToolBox.getFileName(labels[0], graphCopy) : "");
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
            synchronized (result) {
                this.result.add(new Result(originNode.getNodeId(), originNode.getOriginUrl(), res,this.graph));
            }
        }
    }


    @Override
    void run() throws InterruptedException {
        this.restoreCheckpoint();
        this.exploreGraphNode(this.origins.size());
    }

    public static class DFSNode implements Serializable {
        private String path;
        private Long id;
        private String swhid;
        public DFSNode(){

        }
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

            return id != null ? id.equals(dfsNode.id) : dfsNode.id == null;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
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

        public void setPath(String path) {
            this.path = path;
        }

        public void setId(Long id) {
            this.id = id;
        }

    }


    public static class Result implements Serializable {
        private long originId;
        private String originUrl;
        private List<DFSNode> fileNodes;

        public Result(){

        }
        public Result(long originId, String originUrl, List<DFSNode> fileNodes,Graph graph) {
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
