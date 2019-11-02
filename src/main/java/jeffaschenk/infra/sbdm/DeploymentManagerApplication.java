package jeffaschenk.infra.sbdm;

import jeffaschenk.infra.sbdm.conf.WebConfig;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@PropertySource({"classpath:application.yml"})
@EnableTransactionManagement
@EntityScan(basePackages="jeffaschenk.infra.sbdm.entity")
@EnableEurekaClient
@Import(WebConfig.class)
public class DeploymentManagerApplication {
    public static void main(String args[]){
        SpringApplication.run(DeploymentManagerApplication.class, args);
    }
}
