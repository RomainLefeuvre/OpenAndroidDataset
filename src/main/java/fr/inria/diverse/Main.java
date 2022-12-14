package fr.inria.diverse;

import fr.inria.diverse.tools.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 0) {
            Configuration.init(args[0]);
        } else {
            Configuration.init();
        }
        Graph g = new Graph();
        g.loadGraph();
        /*GraphExplorer debugExplorer = new DebugExplorer(g);
        debugExplorer.run();
        */
        logger.info("-----------LastOriginFinder Finder stage-----------");


        Instant inst1 = Instant.now();

        GraphExplorer lastOriginFinder = new LastOriginFinder(g);
        lastOriginFinder.run();
        Instant inst2 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());

        logger.info("-----------File Finder stage-----------");

        GraphExplorer fileFinder = new FileFinder(g);
        fileFinder.run();
        inst1 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst2, inst1).toSeconds());

    }
}
