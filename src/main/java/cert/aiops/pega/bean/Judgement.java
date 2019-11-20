package cert.aiops.pega.bean;

import cert.aiops.pega.util.PegaEnum;

public class Judgement {

    private String exceptionId;
    private PegaEnum.RegistrationExceptionCode exceptionCode;
    private PegaEnum.IssueStatus status;
    private PegaEnum.ActionType actionType;
    private String content;

    @Override
    public String toString() {
        return "Judgement{" +
                "exceptionId='" + exceptionId + '\'' +
                ", exceptionCode=" + exceptionCode +
                ", status=" + status +
                ", actionType=" + actionType +
                ", content='" + content + '\'' +
                '}';
    }

    public String getExceptionId() {
        return exceptionId;
    }

    public void setExceptionId(String exceptionId) {
        this.exceptionId = exceptionId;
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
