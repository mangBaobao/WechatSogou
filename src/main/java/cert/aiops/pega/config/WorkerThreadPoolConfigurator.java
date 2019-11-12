package cert.aiops.pega.config;

import cert.aiops.pega.masterExecutors.TraceableThreadPoolTaskExecutor;
import cert.aiops.pega.startup.BeingWorkerCondition;
//import com.netflix.discovery.converters.Auto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
//@Conditional(value={BeingWorkerCondition.class})
public class WorkerThreadPoolConfigurator {

    Logger logger = LoggerFactory.getLogger(WorkerThreadPoolConfigurator.class);
    @Autowired
    WorkerConfiguration workerConfiguration;

    @Bean
    public Executor RequestHandleExecutor(){
        ThreadPoolTaskExecutor executor = new TraceableThreadPoolTaskExecutor();
        executor.setCorePoolSize(workerConfiguration.getCorePoolSize());
        executor.setMaxPoolSize(workerConfiguration.getMaxPoolSize());
        executor.setQueueCapacity(workerConfiguration.getQueueCapacity());
        executor.setThreadNamePrefix(workerConfiguration.getRequestHandleExecutorNamePrefix());
        executor.initialize();
        logger.info("RequestHandleExecutor has been initialized and starts to work");
        return executor;
    }
}
