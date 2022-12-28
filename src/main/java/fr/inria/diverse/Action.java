package fr.inria.diverse;

import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.Serializable;
import java.util.ArrayList;

public interface Action<T extends Serializable > {
    void exploreGraphNodeAction(long index, SwhUnidirectionalGraph graphCopy, ArrayList<T> result);
}
