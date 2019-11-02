package jeffaschenk.infra.sbdm.response;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.json.JsonStruct;
import org.springframework.hateoas.ResourceSupport;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CommonResponseResource extends ResourceSupport {

    @JsonProperty("content")
    private JsonStruct jsonStruct;

    @JsonCreator
    public CommonResponseResource(@JsonProperty("content") JsonStruct jsonStruct) {
        this.jsonStruct = jsonStruct;
    }
    
}
