package jeffaschenk.infra.sbdm.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
public class DeploymentManagerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "serviceName is mandatory")
    private String serviceName;

    @NotNull(message = "eventDate is mandatory")
    private Date eventDate;

    @NotNull(message = "eventMessage is mandatory")
    private String eventMessage;
}
