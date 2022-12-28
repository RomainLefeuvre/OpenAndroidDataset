package fr.inria.diverse;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for simple App.
 */
public class AndroidManifestAnalyzerTest {


    /**
     * Rigorous Test :-)
     */
    @Test
    public void getAndroidManifestTest() throws IOException {
        String androidManifest = new String(getClass().getClassLoader().getResourceAsStream("AndroidManifest.xml")
                .readAllBytes());
        //assertEquals("org.mozilla.fenix", AndroidManifestAnalyser.getAndroidManifestPackage(androidManifest));
    }
}
