package cert.aiops.pega;

import cert.aiops.pega.bean.PegaEnum;
import cert.aiops.pega.masterExecutors.Master;
import cert.aiops.pega.startup.ApplicationStartup;
import cert.aiops.pega.util.SpringContextUtil;
import cert.aiops.pega.workerExecutors.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @Author HuihongHe
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableAsync
@EnableCaching
public class PegaMainApplication {
//    private static Logger logger = LoggerFactory.getLogger(PegaMainApplication.class);

    public static void main(String[] args) {

        SpringApplication springApplication = new SpringApplication(PegaMainApplication.class);
    //    springApplication.addListeners(new ApplicationStartup());
        springApplication.run(args);
    }
}
