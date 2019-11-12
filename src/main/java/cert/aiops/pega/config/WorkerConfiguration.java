package cert.aiops.pega.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:worker.properties")
@ConfigurationProperties(prefix = "worker")
public class WorkerConfiguration {
    private int concurrency;
    private int maxCountPerFile;
    private int retryTimes;
    private int requestWaitBase;
    private String routineFilePath;
    private String requestFilePath;
    private String pingScriptPath;
    private String queueName;
    private String queueRoutingKey;
    private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity;
    private String requestHandleExecutorNamePrefix;
    private int routineWaitBase;
    private int cacheValidation;
    private int requestWaitThreshold;
    private int routineWaitThreshold;

    @Override
    public String toString() {
        return "WorkerConfiguration{" +
                "concurrency=" + concurrency +
                ", maxCountPerFile=" + maxCountPerFile +
                ", retryTimes=" + retryTimes +
                ", requestWaitBase=" + requestWaitBase +
                ", routineFilePath='" + routineFilePath + '\'' +
                ", requestFilePath='" + requestFilePath + '\'' +
                ", pingScriptPath='" + pingScriptPath + '\'' +
                ", queueName='" + queueName + '\'' +
                ", queueRoutingKey='" + queueRoutingKey + '\'' +
                ", corePoolSize=" + corePoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", queueCapacity=" + queueCapacity +
                ", requestHandleExecutorNamePrefix='" + requestHandleExecutorNamePrefix + '\'' +
                ", routineWaitBase=" + routineWaitBase +
                ", cacheValidation=" + cacheValidation +
                ", requestWaitThreshold=" + requestWaitThreshold +
                ", routineWaitThreshold=" + routineWaitThreshold +
                '}';
    }

    public int getRequestWaitThreshold() {
        return requestWaitThreshold;
    }

    public void setRequestWaitThreshold(int requestWaitThreshold) {
        this.requestWaitThreshold = requestWaitThreshold;
    }

    public int getRoutineWaitThreshold() {
        return routineWaitThreshold;
    }

    public void setRoutineWaitThreshold(int routineWaitThreshold) {
        this.routineWaitThreshold = routineWaitThreshold;
    }

    public int getCacheValidation() {
        return cacheValidation;
    }

    public void setCacheValidation(int cacheValidation) {
        this.cacheValidation = cacheValidation;
    }

    public int getRoutineWaitBase() {
        return routineWaitBase;
    }

    public void setRoutineWaitBase(int routineWaitBase) {
        this.routineWaitBase = routineWaitBase;
    }

    public String getRequestHandleExecutorNamePrefix() {
        return requestHandleExecutorNamePrefix;
    }

    public void setRequestHandleExecutorNamePrefix(String requestHandleExecutorNamePrefix) {
        this.requestHandleExecutorNamePrefix = requestHandleExecutorNamePrefix;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public int getRequestWaitBase() {
        return requestWaitBase;
    }

    public void setRequestWaitBase(int requestWaitBase) {
        this.requestWaitBase = requestWaitBase;
    }

    public String getPingScriptPath() {
        return pingScriptPath;
    }

    public void setPingScriptPath(String pingScriptPath) {
        this.pingScriptPath = pingScriptPath;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public int getMaxCountPerFile() {
        return maxCountPerFile;
    }

    public void setMaxCountPerFile(int maxCountPerFile) {
        this.maxCountPerFile = maxCountPerFile;
    }

    public String getRoutineFilePath() {
        return routineFilePath;
    }

    public void setRoutineFilePath(String routineFilePath) {
        this.routineFilePath = routineFilePath;
    }

    public String getRequestFilePath() {
        return requestFilePath;
    }

    public void setRequestFilePath(String requestFilePath) {
        this.requestFilePath = requestFilePath;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueRoutingKey() {
        return queueRoutingKey;
    }

    public void setQueueRoutingKey(String queueRoutingKey) {
        this.queueRoutingKey = queueRoutingKey;
    }
}
