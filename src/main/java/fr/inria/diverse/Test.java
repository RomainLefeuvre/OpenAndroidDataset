/*
 * Copyright (c) 2020-2022 The Software Heritage developers
 * See the AUTHORS file at the top-level directory of this distribution
 * License: GNU General Public License version 3, or any later version
 * See top-level LICENSE file for more information
 */

package fr.inria.diverse;

import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.logging.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.softwareheritage.graph.SWHID;
import org.softwareheritage.graph.SwhType;
import org.softwareheritage.graph.SwhUnidirectionalGraph;
import org.softwareheritage.graph.labels.DirEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Test {
    final static Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws IOException, ClassNotFoundException {


        ProgressLogger pl = new ProgressLogger(logger, 10, TimeUnit.SECONDS);
        SwhUnidirectionalGraph graph = SwhUnidirectionalGraph.loadLabelled("/home/rlefeuvr/Workspaces/SAND_BOX/SW_GRAPH/python_data/graph", pl);


        graph.properties.loadLabelNames();

        getFilesNodeMatchingName("README",graph);


    }

    public static LongArrayList getFilesNodeMatchingName(String fileName, SwhUnidirectionalGraph graph ){
        ArcLabelledNodeIterator it = graph.labelledNodeIterator();
        int totalCount=0;
        LongArrayList results = new LongArrayList();
        while (it.hasNext()) {
            totalCount++;
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
                            if(filename.equals("README")||filename.equals("README.md")){
                                results.add(dstNode);
                            }
                        }
                    }
                }
            }
        }
        System.out.println(totalCount);
        System.out.println(results.size());
        return results;
    }
}