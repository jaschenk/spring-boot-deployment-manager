package jeffaschenk.infra.sbdm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * ArtifactServiceProperties
 *
 * @author jaschenk
 */
@Data
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArtifactServiceProperties implements Serializable {

    @JsonProperty("serviceName")
    private String serviceName;

    @EqualsAndHashCode.Exclude
    @JsonProperty("serviceConfiguration")
    private Map<String,Object> serviceConfiguration = new HashMap<>();  //NOSONAR

    /**
     * Default constructor
     *
     * @param serviceName -- Service Name
     */
    public ArtifactServiceProperties(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * For Serialization capabilities
     */
    public ArtifactServiceProperties() {} //NOSONAR

    /**
     * toMap
     * This Object into an Object ...
     * @return Map<String,Object> - Contrived Map from this Object.
     */
    public Map<String,Object> toMap() {
        Map<String,Object> map = new HashMap<>();
        map.put("serviceName", this.serviceName);
        map.put("serviceConfiguration", this.serviceConfiguration);
        return map;
    }

}
