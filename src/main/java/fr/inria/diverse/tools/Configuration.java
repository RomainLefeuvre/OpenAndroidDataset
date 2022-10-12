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
    private int threadNumber;
    private String graphPath;
    private String targetedFileName;
    private String loadingMode;
    private String swhToken;

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


}
