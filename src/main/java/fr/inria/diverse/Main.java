package fr.inria.diverse;

import fr.inria.diverse.tools.Configuration;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.bits.Fast;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import static it.unimi.dsi.big.webgraph.labelling.BitStreamArcLabelledImmutableGraph.LABELS_EXTENSION;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {


        if (args.length > 0) {
            Configuration.init(args[0]);
        } else {
            Configuration.init();
        }
        Graph g = new Graph();
        long currentNodeId = 23000000000L;
        long size = 0L;
        g.loadGraph();
        FileInputStream fis = new FileInputStream(Configuration.getInstance()
                .getGraphPath() + "-labelled" + LABELS_EXTENSION);
        try {
            size = fis.getChannel().size();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long l = Fast.mostSignificantBit((size * Byte.SIZE + 1) / (g.getGraph().numNodes() + 1));
        long a = l * currentNodeId;
        logger.info("For node " + currentNodeId + " a is " + a + " l is " + l);
        if (a >>> 6 > Integer.MAX_VALUE) {

            logger.error("Will crash node " + currentNodeId);
        }
        logger.info(g.getGraph().numNodes());
        ArcLabelledNodeIterator.LabelledArcIterator it = g.getGraph().labelledSuccessors(25000000000L);
        /*GraphExplorer debugExplorer = new DebugExplorer(g);
        debugExplorer.run();
        */
        logger.info("-----------LastOriginFinder Finder stage-----------");
        Instant inst1 = Instant.now();

        GraphExplorer lastOriginFinder = new LastOriginFinder(g);
        lastOriginFinder.run();
        Instant inst2 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());
/*
        logger.info("-----------File Finder stage-----------");

        GraphExplorer fileFinder = new FileFinder(g);
        fileFinder.run();
        inst1 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst2, inst1).toSeconds());
*/
    }
}
