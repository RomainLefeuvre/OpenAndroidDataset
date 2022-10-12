package fr.inria.diverse;

import com.google.gson.Gson;
import fr.inria.diverse.model.ResolveDto;
import fr.inria.diverse.restClient.ClientEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class AndroidManifestAnalyser {
    static Logger logger = LogManager.getLogger(AndroidManifestAnalyser.class);

    public static void main(String[] args) {
        //Import result
        Map<String, String> source = loadResultsMap();

        Set<String> uris = source.values().stream().collect(Collectors.toSet());

        Map<String, List<String>> res = new HashMap<>();

        Iterator<Map.Entry<String, String>> it = source.entrySet().iterator();
        for (int i = 0; i < 1000 && it.hasNext(); i++) {
            Map.Entry<String, String> entry = it.next();
            String currentId = entry.getKey();
            String manifest = getAndroidManifest(currentId);

            logger.debug("Current manifest : " + manifest);
            String androidManifestPackage = getAndroidManifestPackage(manifest);
            logger.debug("androidManifestPackage found : " + androidManifestPackage);
            logger.debug("========================== \n");
            if (androidManifestPackage != null && !androidManifestPackage.equals("")) {
                if (!res.containsKey(entry.getValue())) {
                    res.put(entry.getValue(), new LinkedList<>());
                }
                res.get(entry.getValue()).add(androidManifestPackage);
            }

        }
        export(res.entrySet().stream().map(entry -> new Result(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()));


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
        String androidManifestPackage = null;
        byte[] byteArray = new byte[0];
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

    public static Map<String, String> loadResultsMap() {
        Gson gson = new Gson();
        Map<String, String> result = null;
        try {
            result = gson.fromJson(new FileReader("resWithSwhIds.json"), Map.class);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error while reading file");
        }
        return result;
    }

    public static void export(List<Result> res) {
        try (FileWriter f = new FileWriter("final.json")
        ) {
            Gson gson = new Gson();
            gson.toJson(res, f);
        } catch (IOException e) {
            throw new RuntimeException("Error while saving", e);
        }

    }

    public static class Result {
        private String uri;
        private List<String> gplayPackages;

        public Result() {
            this.gplayPackages = new LinkedList<>();
        }

        public Result(String uri, List<String> gplayPackages) {
            this.uri = uri;
            this.gplayPackages = gplayPackages;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public List<String> getGplayPackages() {
            return gplayPackages;
        }

        public void setGplayPackages(List<String> gplayPackages) {
            this.gplayPackages = gplayPackages;
        }
    }

}
