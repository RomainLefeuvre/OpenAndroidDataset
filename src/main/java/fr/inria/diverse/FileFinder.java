package fr.inria.diverse;

import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

public class FileFinder extends GraphExplorer {

    public static void main(String[] args) throws InterruptedException, IOException {
        Instant inst1 = Instant.now();
        GraphExplorer fileFinder = new FileFinder();
        fileFinder.run();
        Instant inst2 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());
    }

    public void findTargetedNode(long srcNodeId) {
        LinkedList<DFSNode> matchingFileNode = new LinkedList<>();
        DFSNode currentNode = new DFSNode(srcNodeId);
        Stack<DFSNode> stack = new Stack<>();
        HashSet<Long> visited = new HashSet<>();
        stack.push(currentNode);
        visited.add(srcNodeId);
        long revCount = 0;
        while (!stack.isEmpty()) {
            currentNode = stack.pop();
            if (graph.getNodeType(currentNode.getId()) == SwhType.REV)
                revCount++;
            ArcLabelledNodeIterator.LabelledArcIterator it = graph.labelledSuccessors(currentNode.getId());
            for (long neighborNodeId; (neighborNodeId = it.nextLong()) != -1; ) {
                SwhType neighborType = graph.getNodeType(neighborNodeId);
                //Each revision point to the directory tree and an ordered list of all its parent ...
                //Let's check if it is a CNT or DIR node
                if (neighborType == SwhType.CNT || neighborType == SwhType.DIR) {
                    //Labels is a list since in the same folder you can have files with different names but with the same content.
                    final DirEntry[] labels = (DirEntry[]) it.label().get();
                    boolean labelsContainsTargetedFileName = Arrays.stream(labels)
                            .filter(label -> getFileName(label).equals(this.config.getTargetedFileName()))
                            .count() > 0;
                    //If labelsContainsTargetedFileName we take the targeted label, else we get the first one, it does not matter in our case;
                    final String label = labelsContainsTargetedFileName ? this.config.getTargetedFileName() :
                            (labels.length > 0 ? getFileName(labels[0]) : "");
                    if (!visited.contains(neighborNodeId)) {
                        DFSNode neighborNode = currentNode.createChild(neighborNodeId, label);
                        stack.push(neighborNode);
                        visited.add(neighborNodeId);
                        //Let's check if it's an interesting node ...
                        if (neighborType == SwhType.CNT && labelsContainsTargetedFileName) {
                            logger.info("It's a match, finding file node having requested filename " + neighborNode.getId());
                            matchingFileNode.add(neighborNode);
                        }
                    }
                }
            }
        }
        logger.info("Number of file node matching name found : " + matchingFileNode.size());
    }

    private String getFileName(DirEntry labelId) {
        return new String(graph.getLabelName(labelId.filenameId));
    }

    @Override
    void run() throws InterruptedException, IOException {
        try {
            this.loadGraph();
            this.findTargetedNode(45676258);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error", e);
        }
    }

    public static class DFSNode {
        Path path;
        long id;

        public DFSNode(long id) {
            this.path = Paths.get("/");
            this.id = id;
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

        public void addChild(String childName) {
            this.path = path.resolve(childName);
        }

        public DFSNode createChild(long id, String nodeName) {
            DFSNode res = new DFSNode(id);
            res.path = this.path.resolve(nodeName);
            return res;
        }

        public Path getPath() {
            return path;
        }
    }
}
