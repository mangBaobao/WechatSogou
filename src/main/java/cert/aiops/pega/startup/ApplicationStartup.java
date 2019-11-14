package cert.aiops.pega.startup;

import cert.aiops.pega.util.PegaEnum;
import cert.aiops.pega.masterExecutors.Master;
import cert.aiops.pega.util.SpringContextUtil;
import cert.aiops.pega.config.PegaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value=1)
//@PropertySource("classpath:application.properties")ApplicationListener<ContextRefreshedEvent>,
public class ApplicationStartup implements ApplicationRunner {
    private Logger logger = LoggerFactory.getLogger(ApplicationStartup.class);
    @Autowired
    PegaConfiguration pegaConfiguration;
    private String[] argsList;

//    @Value("${pega.role}")
//    private String role;
//
//    @Bean(name = "master")
//    @Conditional(value = {BeingMasterCondition.class})
//    public Master createMaster() {
//        System.out.println("createMaster is executed");
//        return new Master();
//    }
//
//    @Bean(name = "work")
//    @Conditional(value = {BeingWorkerCondition.class})
//    public Worker createWorker() {
//        System.out.println("createWorker is executed");
//        return new Worker();
//    }

    @Override
    public void run(ApplicationArguments args)  throws Exception {

        if (args.getSourceArgs().length != 0) {
           argsList=args.getSourceArgs();
        //    argsList = (ArrayList<String>) Arrays.asList(temp);
            logger.info("ApplicationStartup_run: recieve args={}", argsList.toString());
        }
        Master master = SpringContextUtil.getBean(Master.class);
        master.init(PegaEnum.State.在维, argsList);
        logger.info("ApplicationStartup_onApplicationEvent: finishes  initiating master; master id={}", master.getId());
        logger.info("ApplicationStartup_onApplicationEvent: finishes to initiate. Now start to run.");

//        if (role.equals(PegaEnum.NodeRole.online.name())) {
//            Master master = this.createMaster();
//            master.init(PegaEnum.State.在维, argsList);
//            logger.info("ApplicationStartup_run: finishes  initiating master; master id={}", master.getId());
//        } else {
//            Worker worker =this.createWorker();
//            worker.init();
//            logger.info("ApplicationStartup_run:finishes  initiating worker; worker id={}", worker.getWorkerId());
//        }
//        logger.info("ApplicationStartup_run: finishes to initiate. Now start to run.");
    }

//    @Override
//    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {



//            Worker worker =SpringContextUtil.getBean(Worker.class);
//            worker.init();
//            logger.info("ApplicationStartup_onApplicationEvent:finishes  initiating worker; worker id={}", worker.getWorkerId());


//    }
}
