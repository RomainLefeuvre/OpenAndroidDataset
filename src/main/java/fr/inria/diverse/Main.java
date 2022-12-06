package fr.inria.diverse;

import fr.inria.diverse.tools.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.softwareheritage.graph.SwhUnidirectionalGraph;

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

        Graph g = new Graph() {
            @Override
            public void loadGraph() throws IOException {
                this.graph = SwhUnidirectionalGraph.loadMapped(this.config.getGraphPath());
            }
        };/*
        g.loadGraph();

        OriginFinder originFinder = new OriginFinder(g);
        originFinder.run();
*/
        g = new Graph();
        g.loadGraph();
/*
        logger.info("-----------LastOriginFinder Finder stage-----------");

        GraphExplorer lastOriginFinder = new LastOriginFinder(g);
        lastOriginFinder.run();
*/
        logger.info("-----------File Finder stage-----------");

        GraphExplorer fileFinder = new FileFinder(g);
        fileFinder.run();
/*
        logger.info("-----------Content Node Matching Name stage-----------");

        ContentNodeMatchingNameFinder f = new ContentNodeMatchingNameFinder(g);
        f.run();
*/
    }


}
