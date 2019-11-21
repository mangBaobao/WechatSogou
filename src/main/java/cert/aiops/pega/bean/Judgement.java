package cert.aiops.pega.bean;

import cert.aiops.pega.util.PegaEnum;
import cert.aiops.pega.util.TabSerializable;
import cert.aiops.pega.util.TabSerializer;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.sql.Time;
import java.util.Date;
import java.util.StringJoiner;

public class Judgement implements TabSerializable {

    private String issueId;
    @Enumerated(EnumType.STRING)
    private PegaEnum.RegistrationExceptionCode exceptionCode;
    @Enumerated(EnumType.STRING)
    private PegaEnum.IssueStatus status;
    @Enumerated(EnumType.STRING)
    private PegaEnum.ActionType actionType;
    private String content;
    private Date updateTime;

    @Override
    public String toTabbedString() {
        StringJoiner joiner = new StringJoiner(",");
        TabSerializer.addValue(issueId,joiner);
        TabSerializer.addValue(exceptionCode,joiner);
        TabSerializer.addValue(status,joiner);
        TabSerializer.addValue(actionType,joiner);
        TabSerializer.addValue(content,joiner);
        TabSerializer.addValue(updateTime,joiner);
        return joiner.toString();
    }
    @Override
    public String toString() {
        return "Judgement{" +
                "issueId='" + issueId + '\'' +
                ", exceptionCode=" + exceptionCode +
                ", status=" + status +
                ", actionType=" + actionType +
                ", content='" + content + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public PegaEnum.RegistrationExceptionCode getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(PegaEnum.RegistrationExceptionCode exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public PegaEnum.IssueStatus getStatus() {
        return status;
    }

    public void setStatus(PegaEnum.IssueStatus status) {
        this.status = status;
    }

    public PegaEnum.ActionType getActionType() {
        return actionType;
    }

    public void setActionType(PegaEnum.ActionType actionType) {
        this.actionType = actionType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
