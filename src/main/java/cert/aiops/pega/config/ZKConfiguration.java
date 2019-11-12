package cert.aiops.pega.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource(value={"classpath:utility.properties"})
@Component
@ConfigurationProperties(prefix = "pega.zkutility" )
public class ZKConfiguration {
    private String connectString;
    private int sessionTimeoutMs;
    private int connectionTimeoutMs;
    private int heartbeatCycleMs;
    private int baseSleepTimeMs;
    private int maxRetries;
    private int maxSleepMs;

    private String chroot;




    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetires) {
        this.maxRetries = maxRetires;
    }

    public int getMaxSleepMs() {
        return maxSleepMs;
    }

    public void setMaxSleepMs(int maxSleepMs) {
        this.maxSleepMs = maxSleepMs;
    }

    public int getHeartbeatCycleMs() {
        return heartbeatCycleMs;
    }

    public void setHeartbeatCycleMs(int heartbeatCycleMs) {
        this.heartbeatCycleMs = heartbeatCycleMs;
    }


    @Override
    public String toString() {
        return "ZKConfiguration{" +
                "connectString='" + connectString + '\'' +
                ", sessionTimeoutMs=" + sessionTimeoutMs +
                ", connectionTimeoutMs=" + connectionTimeoutMs +
                ", heartbeatCycleMs=" + heartbeatCycleMs +
                ", baseSleepTimeMs=" + baseSleepTimeMs +
                ", maxRetries=" + maxRetries +
                ", maxSleepMs=" + maxSleepMs +
                ", chroot='" + chroot + '\'' +
                '}';
    }

    public String getChroot() {
        return chroot;
    }

    public void setChroot(String chroot) {
        this.chroot = chroot;
    }

}
