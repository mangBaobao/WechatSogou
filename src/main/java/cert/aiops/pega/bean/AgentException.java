package cert.aiops.pega.bean;

import cert.aiops.pega.util.PegaEnum;

public class AgentException {
    private String reporter;
    private String time;
    private String issueId;
    private String topic;
    private PegaEnum.RegistrationExceptionCode code;
    private String reason;

    @Override
    public String toString() {
        return "AgentException{" +
                "reporter='" + reporter + '\'' +
                ", time='" + time + '\'' +
                ", issueId='" + issueId + '\'' +
                ", topic='" + topic + '\'' +
                ", code=" + code +
                ", reason='" + reason + '\'' +
                '}';
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public PegaEnum.RegistrationExceptionCode getCode() {
        return code;
    }

    public void setCode(PegaEnum.RegistrationExceptionCode code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
