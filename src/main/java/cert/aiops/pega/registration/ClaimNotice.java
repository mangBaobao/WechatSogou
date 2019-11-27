package cert.aiops.pega.registration;

import java.util.Date;

public class ClaimNotice {
    private String ip;
    private String uuid;
    private Date claimTime;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getClaimTime() {
        return claimTime;
    }

    public void setClaimTime(Date claimTime) {
        this.claimTime = claimTime;
    }

    @Override
    public String toString() {
        return "ClaimNotice{" +
                "ip='" + ip + '\'' +
                ", uuid='" + uuid + '\'' +
                ", claimTime=" + claimTime +
                '}';
    }
}
