package fr.inria.diverse;

import com.google.common.reflect.TypeToken;
import fr.inria.diverse.model.ResolveDto;
import fr.inria.diverse.restClient.ClientEndpoint;
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
    static String backupUri = "export/swh/files/";

    public static void main(String[] args) throws IOException {
        //Import result
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

               /* String androidManifestPackage = getAndroidManifestPackage(manifest);
                logger.debug("androidManifestPackage found : " + androidManifestPackage);

                if (androidManifestPackage != null && !androidManifestPackage.equals("")) {
                    result.addGplayPackage(androidManifestPackage);
                }*/

            }
            if (result.gplayPackages.size() > 0) {
                results.add(result);
            }

        }
        ToolBox.exportFile(results, "AndroidManifestResults.json");

    }

    /**
     * Get the file corresponding to the node by calling swh api
     *
     * @param swhid the swhid of a CNT node
     * @return the file content
     */
    public static String getAndroidManifest(String swhid) {
        ResolveDto dto = ClientEndpoint.swhClient().resolve(swhid);
        return ClientEndpoint.swhClient().getContent(dto.getFullHash());
    }

    public static String getAndroidManifestPackage(String androidManifest) {
        String androidManifestPackage;
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
                        androidManifestPackage = packageAttribute.getValue();
                        return androidManifestPackage;

                    }
                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public static List<FileFinder.Result> loadResultsMap() {
        Type listType = new TypeToken<ArrayList<FileFinder.Result>>() {
        }.getType();
        return ToolBox.loadFile("finalResult.json", listType);
    }

    public static class Result {
        private String uri;
        private Long originId;
        private List<String> gplayPackages = new LinkedList<>();

        public Result(String uri, Long id, List<String> gplayPackages) {
            this.uri = uri;
            this.originId = id;
            this.gplayPackages = gplayPackages;
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

        public List<String> getGplayPackages() {
            return gplayPackages;
        }

        public void setGplayPackages(List<String> gplayPackages) {
            this.gplayPackages = gplayPackages;
        }

        public void addGplayPackage(String gplayPackage) {
            this.gplayPackages.add(gplayPackage);
        }
    }


}
