package fr.inria.diverse;

import com.google.gson.Gson;
import fr.inria.diverse.restClient.ClientEndpoint;
import fr.inria.diverse.restClient.ResolveDto;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

public class AndroidManifestAnalyser {

    public static void main(String[] args) {
        //Import result
        Map<String, String> res = loadResultsMap();

        String id = res.entrySet().stream().findAny().get().getKey();
        System.out.println(getAndroidManifest(id));

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
}
