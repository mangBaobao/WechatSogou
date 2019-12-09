package cert.aiops.pega.util;

public class PegaEnum {
//    public enum Net{
//        z,v
//    }

    public enum State{
        不在维,在维,交维中,施工中,下线
  //  Main,NotMain,Maining,Cons,Retire
    }

    public enum ObjectState {
        valid, invalid,update
    }

    public enum Avail {
        unavail, avail
    }

    public enum TaskType{
        routine,request
    }

    public enum NodeRole {
        online, worker
    }

    public enum IssueStatus {
        lasting,finish
    }

    public enum ActionType{
        arrival,allocate,extract,local,republish
    }

    public enum UpdaterStateCode{
        AgentRunningFailed(1100),
        DaemondRunningFailed(1101),
        DaemondGuardFailed(1102),
        DoubleKilled(1103),
        UpdaterWorking(1104),
        AgentRunningSuccess(1111);

        private int value;

        UpdaterStateCode(int value){
            this.value=value;
        }
        public UpdaterStateCode valueOf(int value){
            switch (value){
                case 1100:
                    return UpdaterStateCode.AgentRunningFailed;
                case 1101:
                    return UpdaterStateCode.DaemondRunningFailed;
                case 1102:
                    return UpdaterStateCode.DaemondGuardFailed;
                case 1103:
                    return UpdaterStateCode.DoubleKilled;
                case 1104:
                    return UpdaterStateCode.UpdaterWorking;
                case 1111:
                    return UpdaterStateCode.AgentRunningSuccess;
            }
            return null;
        }

        public int getValue(){
            return this.value;
        }
    }

    public enum RegistrationExceptionCode {
        NotFoundUuid (1000),
        NotFoundMatchedIp (1001),
        NameNotMatched (1002),
        UuidNotMatched (1003);

        private int value;

          RegistrationExceptionCode(int value){
            this.value=value;
        }

        public RegistrationExceptionCode valueOf(int value){
            switch (value){
                case 1000:
                    return RegistrationExceptionCode.NotFoundUuid;
                case 1001:
                    return RegistrationExceptionCode.NotFoundMatchedIp;
                case 1002:
                    return RegistrationExceptionCode.NameNotMatched;
                case 1003:
                    return RegistrationExceptionCode.UuidNotMatched;
            }
            return null;
        }

        public int getValue(){
            return value;
        }
    }
}
