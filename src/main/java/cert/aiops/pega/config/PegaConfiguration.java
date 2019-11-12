package cert.aiops.pega.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pega")
public class PegaConfiguration {
    private String role;
    private long epoch;
    private String id;
    private int requestHostLimit;
    private int responseTimeout;
    private int routineCycle;
    private int syncCycle;
 //   private String workerShellPath;
    private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity;
    private String hostQueryThreadPoolNamePrefix;
    private String systemQueryThreadPoolNamePrefix;
    private String epochUpdateThreadNamePrefix;
    private String RenovationThreadNamePrefix;
    private int pageNumber;
    private int pageSize;
    private String controllerPath;
//    private String workerInZPath;
//    private String workerInVPath;
    private String routineEpochPath;
    private String systemPath;
//    private String systemInVPath;
//    private String systemInZPath;
    private String workingNet;
    private String workerPath;
    private String mappingPath;
    private int monitorCountThredshold;
    private float giniCoefficient;
    private int requestWaitBase;
    private int requestRetryTimes;


    private int cacheValidation;
    private double fragFactor;


    @Override
    public String toString() {
        return "PegaConfiguration{" +
                "role='" + role + '\'' +
                ", epoch=" + epoch +
                ", id='" + id + '\'' +
                ", requestHostLimit=" + requestHostLimit +
                ", responseTimeout=" + responseTimeout +
                ", routineCycle=" + routineCycle +
                ", syncCycle=" + syncCycle +
                ", corePoolSize=" + corePoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", queueCapacity=" + queueCapacity +
                ", hostQueryThreadPoolNamePrefix='" + hostQueryThreadPoolNamePrefix + '\'' +
                ", systemQueryThreadPoolNamePrefix='" + systemQueryThreadPoolNamePrefix + '\'' +
                ", epochUpdateThreadNamePrefix='" + epochUpdateThreadNamePrefix + '\'' +
                ", RenovationThreadNamePrefix='" + RenovationThreadNamePrefix + '\'' +
                ", pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", controllerPath='" + controllerPath + '\'' +
                ", routineEpochPath='" + routineEpochPath + '\'' +
                ", systemPath='" + systemPath + '\'' +
                ", workingNet='" + workingNet + '\'' +
                ", workerPath='" + workerPath + '\'' +
                ", mappingPath='" + mappingPath + '\'' +
                ", monitorCountThredshold=" + monitorCountThredshold +
                ", giniCoefficient=" + giniCoefficient +
                ", requestWaitBase=" + requestWaitBase +
                ", requestRetryTimes=" + requestRetryTimes +
                ", cacheValidation=" + cacheValidation +
                ", fragFactor=" + fragFactor +
                '}';
    }

    public int getCacheValidation() {
        return cacheValidation;
    }

    public void setCacheValidation(int cacheValidation) {
        this.cacheValidation = cacheValidation;
    }

    public int getRequestWaitBase() {
        return requestWaitBase;
    }

    public void setRequestWaitBase(int requestWaitBase) {
        this.requestWaitBase = requestWaitBase;
    }

    public int getRequestRetryTimes() {
        return requestRetryTimes;
    }

    public void setRequestRetryTimes(int requestRetryTimes) {
        this.requestRetryTimes = requestRetryTimes;
    }

    public String getRenovationThreadNamePrefix() {
        return RenovationThreadNamePrefix;
    }

    public void setRenovationThreadNamePrefix(String renovationThreadNamePrefix) {
        this.RenovationThreadNamePrefix = renovationThreadNamePrefix;
    }

    public String getEpochUpdateThreadNamePrefix() {
        return epochUpdateThreadNamePrefix;
    }

    public void setEpochUpdateThreadNamePrefix(String epochUpdateThreadNamePrefix) {
        this.epochUpdateThreadNamePrefix = epochUpdateThreadNamePrefix;
    }

    public double getFragFactor() {
        return fragFactor;
    }

    public void setFragFactor(double fragFactor) {
        this.fragFactor = fragFactor;
    }
    public float getGiniCoefficient() {
        return giniCoefficient;
    }

    public void setGiniCoefficient(float giniCoefficient) {
        this.giniCoefficient = giniCoefficient;
    }

    public int getMonitorCountThredshold() {
        return monitorCountThredshold;
    }

    public void setMonitorCountThredshold(int monitorCountThredshold) {
        this.monitorCountThredshold = monitorCountThredshold;
    }

    public String getWorkerPath() {
        return workerPath;
    }

    public void setWorkerPath(String workerPath) {
        this.workerPath = workerPath;
    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    public String getMappingPath() {
        return mappingPath;
    }

    public void setMappingPath(String mappingPath) {
        this.mappingPath = mappingPath;
    }

    public String getWorkingNet() {
        return workingNet;
    }

    public void setWorkingNet(String workingNet) {
        this.workingNet = workingNet;
    }

//    public String getSystemInVPath() {
//        return systemInVPath;
//    }
//
//    public void setSystemInVPath(String systemInVPath) {
//        this.systemInVPath = systemInVPath;
//    }
//
//    public String getSystemInZPath() {
//        return systemInZPath;
//    }
//
//    public void setSystemInZPath(String systemInZPath) {
//        this.systemInZPath = systemInZPath;
//    }

    public String getControllerPath() {
        return controllerPath;
    }

    public void setControllerPath(String controllerPath) {
        this.controllerPath = controllerPath;
    }

//    public String getWorkerInZPath() {
//        return workerInZPath;
//    }
//
//    public void setWorkerInZPath(String workerInZPath) {
//        this.workerInZPath = workerInZPath;
//    }
//
//    public String getWorkerInVPath() {
//        return workerInVPath;
//    }
//
//    public void setWorkerInVPath(String workerInVPath) {
//        this.workerInVPath = workerInVPath;
//    }

    public String getRoutineEpochPath() {
        return routineEpochPath;
    }

    public void setRoutineEpochPath(String routineEpochPath) {
        this.routineEpochPath = routineEpochPath;
    }

    public String getSystemPath() {
        return systemPath;
    }

    public void setSystemPath(String systemPath) {
        this.systemPath = systemPath;
    }

//    public String getWorkerShellPath() {
//        return workerShellPath;
//    }
//
//    public void setWorkerShellPath(String workerShellPath) {
//        this.workerShellPath = workerShellPath;
//    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRequestHostLimit() {
        return requestHostLimit;
    }

    public void setRequestHostLimit(int requestHostLimit) {
        this.requestHostLimit = requestHostLimit;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public int getRoutineCycle() {
        return routineCycle;
    }

    public void setRoutineCycle(int routineCycle) {
        this.routineCycle = routineCycle;
    }

    public int getSyncCycle() {
        return syncCycle;
    }

    public void setSyncCycle(int syncCycle) {
        this.syncCycle = syncCycle;
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

    public String getHostQueryThreadPoolNamePrefix() {
        return hostQueryThreadPoolNamePrefix;
    }

    public void setHostQueryThreadPoolNamePrefix(String hostQueryThreadPoolNamePrefix) {
        this.hostQueryThreadPoolNamePrefix = hostQueryThreadPoolNamePrefix;
    }

    public String getSystemQueryThreadPoolNamePrefix() {
        return systemQueryThreadPoolNamePrefix;
    }

    public void setSystemQueryThreadPoolNamePrefix(String systemQueryThreadPoolNamePrefix) {
        this.systemQueryThreadPoolNamePrefix = systemQueryThreadPoolNamePrefix;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
