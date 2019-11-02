package jeffaschenk.infra.sbdm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ServiceFile
 *
 * @author jaschenk
 */
@Data
@AllArgsConstructor
public class ServiceFile implements Serializable {

    @JsonProperty("serviceName")
    private String serviceName;

    @JsonProperty("serviceFilePath")
    private String serviceFilePath;

    @JsonProperty("lastModified")
    private LocalDateTime lastModified;

    @JsonProperty("size")
    private Long size;

    @JsonProperty("href")
    private String href;

    @JsonProperty("hrefText")
    private String hrefText;


}
