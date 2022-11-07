package fr.inria.diverse.tools;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ToolBox {
    public static <T> T loadFile(String fileName, Type type) {
        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(Paths.get(fileName))) {
            return gson.fromJson(reader, type);

        } catch (IOException e) {
            throw new RuntimeException("Error while saving", e);
        }
    }

    /**
     * Export an object of Type T to a json file named filename+".json"
     *
     * @param objectToSave the object you want to save
     * @param filename     its filename
     * @param <T>          the type of objectToSave
     */
    public static <T> void exportFile(T objectToSave, String filename) {
        try {
            createParentIfNeeded(filename);
        } catch (IOException e) {
            throw new RuntimeException("Error while creating parent folder", e);
        }
        try (FileWriter f = new FileWriter(filename)
        ) {
            Gson gson = new Gson();
            gson.toJson(objectToSave, f);
        } catch (IOException e) {
            throw new RuntimeException("Error while saving", e);
        }
    }

    public static void createParentIfNeeded(String path) throws IOException {
        Path filePath = Paths.get(path);
        Path parentDir = filePath.getParent();
        if (!Files.exists(parentDir))
            Files.createDirectories(parentDir);
    }
}
