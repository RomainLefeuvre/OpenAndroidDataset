package fr.inria.diverse;

import com.google.common.reflect.TypeToken;
import fr.inria.diverse.model.ResolveDto;
import fr.inria.diverse.restClient.ClientEndpoint;
import fr.inria.diverse.tools.Configuration;
import fr.inria.diverse.tools.ToolBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AndroidManifestAnalyser {
    static Logger logger = LogManager.getLogger(AndroidManifestAnalyser.class);
    static String backupUri;

    public static void main(String[] args) throws IOException {
        //Import result
        Configuration.init();
        backupUri = Configuration.getInstance()
                .getExportPath() + "/swh/files/";
        List<FileFinder.Result> sources = loadResultsMap();
        List<Result> results = new LinkedList<>();

        for (FileFinder.Result origin : sources) {
            logger.debug("Current Origin : " + origin.getOriginUrl());
            Result result = new Result(origin.getOriginUrl(), origin.getOriginId());
            for (FileFinder.DFSNode fileNode : origin.getFileNodes()) {
                String manifest = getAndroidManifest(fileNode.getSwhid());
                logger.debug("Current manifest : " + manifest);

                Path filePath = Paths.get(backupUri, fileNode.getSwhid());
                Path parentDir = filePath.getParent();
                if (!Files.exists(parentDir))
                    Files.createDirectories(parentDir);
                Files.write(filePath, manifest.getBytes());

                String androidManifestPackage = null;
                androidManifestPackage = getAndroidManifestPackage(manifest);

                logger.debug("androidManifestPackage found : " + androidManifestPackage);

                if (androidManifestPackage != null && !androidManifestPackage.equals("")) {
                    result.addGplayPackage(new AndroidNode(fileNode, androidManifestPackage));
                }

            }
            if (result.androidNodes.size() > 0) {
                results.add(result);
            }

        }
        ToolBox.exportObjectToJson(results, Configuration.getInstance()
                .getExportPath() + "/swh/AndroidManifestResults.json");

    }

    /**
     * Get the file corresponding to the node by calling swh api
     *
     * @param swhid the swhid of a CNT node
     * @return the file content
     */
    public static String getAndroidManifest(String swhid) {
        ResolveDto dto = ClientEndpoint.swhClient()
                .resolve("Bearer " + Configuration.getInstance().getSwhToken(), swhid);
        return ClientEndpoint.swhClient()
                .getContent("Bearer " + Configuration.getInstance().getSwhToken(), dto.getFullHash());
    }

    public static String getAndroidManifestPackage(String androidManifest) {
        byte[] byteArray;
        try {
            byteArray = androidManifest.getBytes("UTF-8");

            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = inputFactory.createXMLEventReader(inputStream);
            while (reader.hasNext()) {
                XMLEvent nextEvent = reader.nextEvent();
                if (nextEvent.isStartElement()) {
                    StartElement startElement = nextEvent.asStartElement();
                    if (startElement.getName().getLocalPart().equals("manifest")) {
                        Attribute packageAttribute = startElement.getAttributeByName(new QName("package"));
                        return packageAttribute.getValue();

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(androidManifest);

        }
        return null;

    }

    public static List<FileFinder.Result> loadResultsMap() {
        Type listType = new TypeToken<ArrayList<FileFinder.Result>>() {
        }.getType();
        return ToolBox.loadJsonObject(FileFinder.exportPath, listType);
    }


    public static class Result {
        private String uri;
        private Long originId;
        private List<AndroidNode> androidNodes = new LinkedList<>();

        public Result(String uri, Long id, List<AndroidNode> androidNodes) {
            this.uri = uri;
            this.originId = id;
            this.androidNodes = androidNodes;
        }

        public Result(String uri, Long id) {
            this.uri = uri;
            this.originId = id;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public Long getOriginId() {
            return originId;
        }

        public void setOriginId(Long originId) {
            this.originId = originId;
        }

        public List<AndroidNode> getAndroidNodes() {
            return androidNodes;
        }

        public void setAndroidNodes(List<AndroidNode> androidNodes) {
            this.androidNodes = androidNodes;
        }

        public void addGplayPackage(AndroidNode androidNode) {
            this.androidNodes.add(androidNode);
        }
    }

    public static class AndroidNode extends FileFinder.DFSNode {
        public String gplayPackage;

        public AndroidNode(FileFinder.DFSNode node, String gplayPackage) {
            super(node);
            this.gplayPackage = gplayPackage;
        }

        public String getGplayPackage() {
            return gplayPackage;
        }

        public void setGplayPackage(String gplayPackage) {
            this.gplayPackage = gplayPackage;
        }
    }


}
