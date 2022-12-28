package fr.inria.diverse;

import fr.inria.diverse.model.Origin;
import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class LambdaExplorer extends GraphExplorer<Origin>{
    protected Action<Origin> exploreGraphNodeAction;
    protected List<Origin> inputs;
    public LambdaExplorer(Graph graph,Action<Origin> a) {
        super(graph);
        this.exploreGraphNodeAction = a;
    }

    public LambdaExplorer(Graph graph, Action<Origin> a , List<Origin> inputs){
        this(graph,a);
        this.inputs=inputs;
    }

    protected  void exploreGraphNodeAction(long index, SwhUnidirectionalGraph graphCopy){
        this.exploreGraphNodeAction.exploreGraphNodeAction(index,graphCopy,this.result);
    }

    @Override
    protected String getExportPath() {
        return "test";
    }

    public List<Origin> explore() throws InterruptedException, IOException {
        this.run();
        return result;
    }

    public static void visitNodesBFS(ImmutableGraph graph, long srcNodeId) {
        Queue<Long> queue = new ArrayDeque<>();
        HashSet<Long> visited = new HashSet<Long>();
        queue.add(srcNodeId);
        visited.add(srcNodeId);

        while (!queue.isEmpty()) {
            long currentNodeId = queue.poll();
            System.out.println(currentNodeId);

            LazyLongIterator it = graph.successors(currentNodeId);
            for (long neighborNodeId; (neighborNodeId = it.nextLong()) != -1;) {
                if (!visited.contains(neighborNodeId)) {
                    queue.add(neighborNodeId);
                    visited.add(neighborNodeId);
                }
            }
        }
    }

    ;


}
