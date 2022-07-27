/*
 * Copyright (c) 2020-2022 The Software Heritage developers
 * See the AUTHORS file at the top-level directory of this distribution
 * License: GNU General Public License version 3, or any later version
 * See top-level LICENSE file for more information
 */

package fr.inria.diverse;

import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.logging.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.softwareheritage.graph.SwhBidirectionalGraph;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class Test_Bidi {
    final static Logger logger = LoggerFactory.getLogger(Test_Bidi.class);
    SwhBidirectionalGraph graph;

    public Test_Bidi(SwhBidirectionalGraph graph) {
        this.graph = graph;
        try {
            graph.properties.loadLabelNames();
            graph.properties.loadMessages();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public  HashMap<Long,String> getFilesNodeMatchingName(String fileName){
        ArcLabelledNodeIterator it = graph.getForwardGraph().labelledNodeIterator();
        HashMap<Long,String> results = new HashMap<Long, String>();
        results.putAll(this.getFilesNodeMatchingName(fileName,it));
        return results;
    }

    public  HashMap<Long,String> getFilesNodeMatchingName(String fileName,ArcLabelledNodeIterator it){
        HashMap<Long,String> results = new HashMap<>();

        while (it.hasNext()) {
            ArcLabelledNodeIterator.LabelledArcIterator s = it.successors();
            long dstNode;
            while ((dstNode = s.nextLong()) >= 0) {
                DirEntry[] labels = (DirEntry[]) s.label().get();
                if (labels.length > 0) {
                    for (DirEntry label : labels) {
                        //If the destination node is a file
                        if(graph.getNodeType(dstNode) == SwhType.CNT){
                            String filename= new String(graph.getLabelName(label.filenameId));
                            if(filename.equals(fileName)){

                                long originNode=getRepoFromFileNode(dstNode);
                                String originUrl=graph.getUrl(originNode);
                                results.put(dstNode,originUrl);
                            }
                        }
                    }
                }
            }
        }
        System.out.println(results.size());
        return results;
    }

        public long getRepoFromFileNode(long nodeId){
        Instant inst1 = Instant.now();

        SwhBidirectionalGraph graph_copy = graph.copy();
        LazyLongIterator it = graph_copy.predecessors(nodeId);
        Long pred = it.nextLong();
        Long current = nodeId;

        while(pred!=-1 && graph_copy.getNodeType(current)!= SwhType.ORI ){
            current=pred;
            pred= graph_copy.predecessors(current).nextLong();

        }
        Instant inst2 = Instant.now();
        System.out.println("Elapsed Time: "+ Duration.between(inst1, inst2).toString());

        return current;

    }


    public ArcLabelledNodeIterator[] splitArcLabelledNodeIterators(int howMany, SwhUnidirectionalGraph graph ) {
        if (graph.numNodes() == 0L && howMany == 0) {
            return new ArcLabelledNodeIterator[0];
        } else if (howMany < 1) {
            throw new IllegalArgumentException();
        } else {
            ArcLabelledNodeIterator[] result = new ArcLabelledNodeIterator[howMany];
            if (!graph.hasCopiableIterators()) {
                result[0] = graph.labelledNodeIterator();
                return result;
            } else {
                long n = graph.numNodes();
                int m = (int)Math.ceil((double)n / (double)howMany);
                if (graph.randomAccess()) {
                    int i = 0;

                    for(long from = (long)0; from < n; ++i) {
                        result[i] = graph.labelledNodeIterator(from).copy(from + (long)m);
                        from += (long)m;
                    }

                    Arrays.fill(result, i, result.length, NodeIterator.EMPTY);
                    return result;
                } else {
                    ArcLabelledNodeIterator nodeIterator = graph.labelledNodeIterator();
                    int i = 0;

                    for(long nextNode = 0L; i < result.length && nodeIterator.hasNext(); ++nextNode) {
                        if (nextNode % (long)m == 0L) {
                            result[i++] = nodeIterator.copy(nextNode + (long)m);
                        }

                        long node = nodeIterator.nextLong();

                        assert node == nextNode;
                    }

                    Arrays.fill(result, i, result.length, NodeIterator.EMPTY);
                    return result;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ProgressLogger pl = new ProgressLogger(logger, 10, TimeUnit.SECONDS);
        Test_Bidi test= new Test_Bidi(SwhBidirectionalGraph.loadLabelled("/home/rlefeuvr/Workspaces/SAND_BOX/SW_GRAPH/python_data/graph", pl));
        Instant inst1 = Instant.now();
        test.getFilesNodeMatchingName("README");
        Instant inst2 = Instant.now();
        System.out.println("Elapsed Time: "+ Duration.between(inst1, inst2).toString());
    }
}