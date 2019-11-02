package jeffaschenk.infra.sbdm.metrics;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Application Metric
 *
 * @author jaschenk
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "metric", "lastMetric" })
public class AppMetric implements Serializable {

    private final String name;
    private Long metric = 0L;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="US/Pacific")
    private Date lastMetric;

    public AppMetric(String name) {
        this.name = name;
    }

    public void increment() {
        metric++;
        lastMetric=new Date();
    }
}
