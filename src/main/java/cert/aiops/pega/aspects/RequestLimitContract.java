package cert.aiops.pega.aspects;
import cert.aiops.pega.controller.BaseController;
import cert.aiops.pega.controller.HostController;
import cert.aiops.pega.util.SpringContextUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
     * @author Administrator
     * @time：
     * @Discription：
     */
    @Aspect
    @Component
    public class RequestLimitContract {
        private static final Logger logger = LoggerFactory.getLogger(RequestLimitContract.class);
        private Map<String , Integer> requestMapping = new HashMap<>();

//        @Before("within(@org.springframework.stereotype.Controller *) && @annotation(limit)")
@Before("within(@org.springframework.web.bind.annotation.RestController *) && @annotation(limit)")
        public void requestLimit(final JoinPoint joinPoint , RequestLimit limit) throws RequestLimitException {
            try {
               Object[] args = joinPoint.getArgs();
//                if (args.length==0) {
//                    throw new RequestLimitException("请求缺少参数");
//                }
                BaseController controller= (BaseController) joinPoint.getTarget();

//                String controllerName=joinPoint.getKind();
//                BaseController controller= SpringContextUtil.getBean(controllerName);

                String requestParams = "";
                if(!(controller instanceof HostController)) {
                    for (int i = 0; i < args.length; i++) {
//                    if (args[i] instanceof HttpServletRequest) {
//                        request = (HttpServletRequest) args[i];
//                        break;
//                    }
                        if (args[i] instanceof HashMap) {
                            for (Object param : ((HashMap) args[i]).entrySet()) {
                                Map.Entry entry = (Map.Entry) param;
                                String key = (String) entry.getKey();
                                String value = (String) entry.getValue();
                                requestParams += key + "_" + value + ",";

                            }
                        }
                        if (args[i] instanceof String)
                            requestParams.concat((String) args[i]);
                    }
                }
                HttpServletRequest request = controller.getRequestServlet();
                String ip = controller.getRequesterIp();
                String url = request.getRequestURL().toString();
                String key = "req_limit_".concat(url).concat(ip).concat(requestParams);
                if (requestMapping.get(key) == null || requestMapping.get(key) == 0) {
                    requestMapping.put(key, 1);
                } else {
                    requestMapping.put(key, requestMapping.get(key) + 1);
                }
                int count = requestMapping.get(key);
                if (count > 0) {
                    //创建一个定时器
                    Timer timer = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            requestMapping.remove(key);
                        }
                    };
                    //这个定时器设定在time规定的时间之后会执行上面的remove方法，也就是说在这个时间后它可以重新访问
                    timer.schedule(timerTask, limit.time());
                }
                if (count > limit.count()) {
                    logger.info("RequestLimitContract:用户IP={} 访问地址={} 超过了限定的次数={}",ip,url,limit.count());
                    throw new RequestLimitException("请求超过了限定的次数.请稍后再试");
                }
            }catch (RequestLimitException e){
                throw e;
            }catch (Exception e){
                logger.error("发生异常",e);
            }
        }
}
