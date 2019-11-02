package jeffaschenk.infra.sbdm.repository;

import jeffaschenk.infra.sbdm.entity.DeploymentManagerEvent;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeploymentManagerEventRepository extends JpaRepository<DeploymentManagerEvent, Long> {

    List<DeploymentManagerEvent> findByServiceName(String serviceName, Sort sort);

}
