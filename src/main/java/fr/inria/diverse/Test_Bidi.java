/*
 * Copyright (c) 2020-2022 The Software Heritage developers
 * See the AUTHORS file at the top-level directory of this distribution
 * License: GNU General Public License version 3, or any later version
 * See top-level LICENSE file for more information
 */

package fr.inria.diverse;

import it.unimi.dsi.big.webgraph.LazyLongIterator;
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

    public    HashMap<Long,String> getFilesNodeMatchingName(String fileName){
        Stack<Long> forwardStack = new Stack<>();
        HashSet<Long> forwardVisited = new HashSet<Long>();


        HashMap<Long,String> results = new HashMap<Long, String>();
        ArcLabelledNodeIterator it = graph.getForwardGraph().labelledNodeIterator();
        while (it.hasNext()) {
            long srcNode = it.nextLong();

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

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ProgressLogger pl = new ProgressLogger(logger, 10, TimeUnit.SECONDS);
        Test_Bidi test= new Test_Bidi(SwhBidirectionalGraph.loadLabelled("/home/rlefeuvr/Workspaces/SAND_BOX/SW_GRAPH/python_data/graph", pl));
        Instant inst1 = Instant.now();
        test.getFilesNodeMatchingName("README");
        Instant inst2 = Instant.now();
        System.out.println("Elapsed Time: "+ Duration.between(inst1, inst2).toString());
    }
}