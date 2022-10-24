package fr.inria.diverse;

import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileFinderTest {

    @Test
    public void addChildTest() throws IOException {
        FileFinder.DFSNode node = new FileFinder.DFSNode(1);
        node.addChild("ok");
        node.addChild("ko");
        assertEquals("/ok/ko", node.getPath().toString());
    }

    @Test
    public void dfsEqualsTest() throws IOException {
        FileFinder.DFSNode node = new FileFinder.DFSNode(1);
        FileFinder.DFSNode node2 = new FileFinder.DFSNode(1);
        assertEquals(node, node2);

        HashSet<FileFinder.DFSNode> visited = new HashSet<>();
        visited.add(node);
        assertTrue(visited.contains(node2));

    }
}
