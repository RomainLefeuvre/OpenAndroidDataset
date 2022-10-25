package fr.inria.diverse.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    /**
     * FILENAME is the file location of the configuration JSON file
     *
     * @todo Potentially make this configurable
     */
    public static final String FILENAME = "config.properties";
    /**
     * The instance of Configuration that this Class is storing
     */
    private static Configuration instance = null;

    //Config Attributes
    private final int threadNumber;
    private final String graphPath;
    private final String targetedFileName;
    private final String loadingMode;
    private final String swhToken;
    private final String exportPath;

    private Configuration() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(FILENAME)) {
            if (input == null) {
                throw new RuntimeException("unable to find config.properties");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading config file", e);
        }
        this.threadNumber = Integer.parseInt(props.getProperty("threadNumber"));
        this.graphPath = props.getProperty("graphPath");
        this.targetedFileName = props.getProperty("targetedFileName");
        this.loadingMode = props.getProperty("loadingMode");
        this.swhToken = props.getProperty("swhToken");
        this.exportPath = props.getProperty("exportPath");
    }

    public static Configuration getInstance() {
        if (Configuration.instance == null) {
            Configuration.instance = new Configuration();
        }
        return Configuration.instance;
    }

    //Getters
    public int getThreadNumber() {
        return threadNumber;
    }

    public String getGraphPath() {
        return graphPath;
    }

    public String getTargetedFileName() {
        return targetedFileName;
    }

    public String getLoadingMode() {
        return loadingMode;
    }

    public String getSwhToken() {
        return swhToken;
    }

    public String getExportPath() {
        return exportPath;
    }

}
