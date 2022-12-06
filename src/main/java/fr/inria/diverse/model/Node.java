package fr.inria.diverse.model;

import java.io.Serializable;

public class Node implements Serializable {
    private static final long serialVersionUID = -5583517045537897698L;
    long nodeId;

    public Node() {
    }

    public Node(long nodeId) {
        this.nodeId = nodeId;
    }

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return nodeId == node.nodeId;
    }

    @Override
    public int hashCode() {
        return (int) (nodeId ^ (nodeId >>> 32));
    }
}
