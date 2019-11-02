package jeffaschenk.infra.sbdm.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeffaschenk.infra.sbdm.model.ArtifactServiceProperties;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class DTOParser {

    /**
     * Common Logger
     */
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(DTOParser.class);

    /**
     * getArtifactServicePropertiesObjectFromString
     *
     * @param rawString - String representing a JSON Object.
     * @return Optional<ArtifactServiceProperties> - representing Resolved Optional Object
     */
    public static Optional<ArtifactServiceProperties> getArtifactServicePropertiesObjectFromString(String rawString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ArtifactServiceProperties artifactServiceProperties = objectMapper.readValue(rawString,
                    ArtifactServiceProperties.class);
            return Optional.of(artifactServiceProperties);
        } catch(JsonParseException jpe) {
            LOGGER.error("JSON Exception Parsing Artifact Properties JSON Object: {}", jpe.getMessage());
        } catch(JsonMappingException jme) {
            LOGGER.error("JSON Exception Mapping Artifact Properties JSON Object: {}", jme.getMessage());
        } catch(IOException ioe) {
            LOGGER.error("Exception Parsing Artifact Properties JSON Object: {}", ioe.getMessage());
        }
        return Optional.empty();
    }

    /**
     * getJSONFromArtifactServicePropertiesObject
     *
     * @param artifactServiceProperties - Object to be Serialized
     * @return Optional<String> - Containing contrived JSON Data.
     */
    public static Optional<String> getJSONFromArtifactServicePropertiesObject(ArtifactServiceProperties artifactServiceProperties) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return Optional.of(mapper.writeValueAsString(artifactServiceProperties));
        } catch(JsonProcessingException jpe) {
            LOGGER.error("JSON Exception Processing Artifact Properties Object: {} -- {}", artifactServiceProperties, jpe.getMessage());
        }
        return Optional.empty();
    }

}
