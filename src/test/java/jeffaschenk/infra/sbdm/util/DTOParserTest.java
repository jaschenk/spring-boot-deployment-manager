package jeffaschenk.infra.sbdm.util;

import jeffaschenk.infra.sbdm.model.ArtifactServiceProperties;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class DTOParserTest {

    private static final String  JSON_DATA_STRING =
            "{\"serviceName\":\"testServiceD\",\"serviceConfiguration\":{\"OneKey\":\"OneValue\",\"TwoKey\": \"TwoValue\"}}";

    private static final String TEST_SERVICE_NAME =
            "testServiceFooBar";

    @Test
    public void constructorTest() {
        ArtifactServiceProperties uploadProperties = new ArtifactServiceProperties(TEST_SERVICE_NAME);
        assertNotNull(uploadProperties);
        assertEquals(TEST_SERVICE_NAME, uploadProperties.getServiceName());
        Map map = uploadProperties.toMap();
        assertNotNull(map);
    }

    @Test
    public void dtoParserTest() {
        Optional<ArtifactServiceProperties> result = DTOParser.getArtifactServicePropertiesObjectFromString(JSON_DATA_STRING);
        assertTrue(result.isPresent());
        ArtifactServiceProperties artifactServiceProperties = result.get();
        assertNotNull(artifactServiceProperties);
        assertEquals("testServiceD", artifactServiceProperties.getServiceName());
        assertEquals("OneValue", artifactServiceProperties.getServiceConfiguration().get("OneKey"));
        assertEquals("TwoValue", artifactServiceProperties.getServiceConfiguration().get("TwoKey"));

        Optional<String> jsonStringOfUploadProperties = DTOParser.getJSONFromArtifactServicePropertiesObject(artifactServiceProperties);
        assertTrue(jsonStringOfUploadProperties.isPresent());
        assertNotNull(jsonStringOfUploadProperties.get());
    }

}
