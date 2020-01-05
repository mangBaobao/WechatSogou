package cert.aiops.pega.bean;

import cert.aiops.pega.util.PegaEnum;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long channelId;
    private String scriptKey;
    private String appendixPath;
    @Enumerated(EnumType.STRING)
    private PegaEnum.CronEnum cron;
    @Enumerated(EnumType.STRING)
    private PegaEnum.WaitEnum waitTime;
    private String metrics;
    private int updateTime;
    private boolean isValid;
    private Date expireTime;
    private String topic;
    private  String parameters;
    @Enumerated(EnumType.STRING)
    private PegaEnum.TriggerEnum trigger;

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", channelId=" + channelId +
                ", scriptKey='" + scriptKey + '\'' +
                ", appendixPath='" + appendixPath + '\'' +
                ", cron=" + cron +
                ", waitTime=" + waitTime +
                ", metrics=" + metrics +
                ", updateTime=" + updateTime +
                ", isValid=" + isValid +
                ", expireTime=" + expireTime +
                ", topic='" + topic + '\'' +
                ", parameters='" + parameters + '\'' +
                ", trigger=" + trigger +
                '}';
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScriptKey() {
        return scriptKey;
    }

    public void setScriptKey(String scriptKey) {
        this.scriptKey = scriptKey;
    }

    public String getAppendixPath() {
        return appendixPath;
    }

    public void setAppendixPath(String appendixPath) {
        this.appendixPath = appendixPath;
    }

    public PegaEnum.CronEnum getCron() {
        return cron;
    }

    public void setCron(PegaEnum.CronEnum cron) {
        this.cron = cron;
    }

    public PegaEnum.WaitEnum getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(PegaEnum.WaitEnum waitTime) {
        this.waitTime = waitTime;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public PegaEnum.TriggerEnum getTrigger() {
        return trigger;
    }

    public void setTrigger(PegaEnum.TriggerEnum trigger) {
        this.trigger = trigger;
    }
}
