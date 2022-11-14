package fr.inria.diverse;

import fr.inria.diverse.model.Origin;
import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.ToolBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static fr.inria.diverse.LastOriginFinder.exportPath;

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

        logger.info("-----------LastOriginFinder Finder stage-----------");
        Instant inst1 = Instant.now();

        GraphExplorer lastOriginFinder = new LastOriginFinder(g);
        lastOriginFinder.run();
        Instant inst2 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst1, inst2).toSeconds());
        List<Origin> origins = ToolBox.deserialize(exportPath);

/*
        logger.info("-----------File Finder stage-----------");

        GraphExplorer fileFinder = new FileFinder(g);
        fileFinder.run();
        inst1 = Instant.now();
        logger.debug("Elapsed Time: " + Duration.between(inst2, inst1).toSeconds());
*/
    }
}
