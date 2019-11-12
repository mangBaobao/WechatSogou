package cert.aiops.pega.config;

import cert.aiops.pega.masterExecutors.TraceableThreadPoolTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class MasterThreadPoolConfigurator {

    @Autowired
    PegaConfiguration pegaConfiguration;

    Logger logger = LoggerFactory.getLogger(MasterThreadPoolConfigurator.class);

    @Bean
    public Executor hostQueryExecutor(){
        ThreadPoolTaskExecutor executor = new TraceableThreadPoolTaskExecutor();
        executor.setCorePoolSize(pegaConfiguration.getCorePoolSize());
        executor.setMaxPoolSize(pegaConfiguration.getMaxPoolSize());
        executor.setQueueCapacity(pegaConfiguration.getQueueCapacity());
        executor.setThreadNamePrefix(pegaConfiguration.getHostQueryThreadPoolNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        logger.info("HostQueryExecutorThreadPool has been initialized and starts to work");
        return executor;
    }

    @Bean
    public Executor systemQueryExecutor(){
        ThreadPoolTaskExecutor executor = new TraceableThreadPoolTaskExecutor();
        executor.setCorePoolSize(pegaConfiguration.getCorePoolSize());
        executor.setMaxPoolSize(pegaConfiguration.getMaxPoolSize());
        executor.setQueueCapacity(pegaConfiguration.getQueueCapacity());
        executor.setThreadNamePrefix(pegaConfiguration.getSystemQueryThreadPoolNamePrefix());
        executor.initialize();
        logger.info("SystemQueryExecutorThreadPool has been initialized and starts to work");
        return executor;
    }

    @Bean
    public Executor EpochUpdateExecutor(){
        ThreadPoolTaskExecutor executor = new TraceableThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(2);
        executor.setThreadNamePrefix(pegaConfiguration.getEpochUpdateThreadNamePrefix());
        executor.initialize();
        logger.info("EpochUpdateExecutorThreadPool has been initialized and starts to work");
        return executor;
    }


    @Bean
    public Executor RenovationExecutor(){
        ThreadPoolTaskExecutor executor = new TraceableThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(2);
        executor.setThreadNamePrefix(pegaConfiguration.getRenovationThreadNamePrefix());
        executor.initialize();
        logger.info("CollectRoutineResultExecutorThreadPool has been initialized and starts to work");
        return executor;
    }

}
