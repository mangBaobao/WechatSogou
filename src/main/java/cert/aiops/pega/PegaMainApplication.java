package cert.aiops.pega;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @Author HuihongHe
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableAsync
@EnableCaching
@EnableKafka
public class PegaMainApplication {
//    private static Logger logger = LoggerFactory.getLogger(PegaMainApplication.class);

    public static void main(String[] args) {

        SpringApplication springApplication = new SpringApplication(PegaMainApplication.class);
    //    springApplication.addListeners(new ApplicationStartup());
        springApplication.run(args);
    }
}
