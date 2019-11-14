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

    public enum ExceptionCode{
        NotFoundUuid (1000),
        NotFoundMatchedIp (1001),
        NameNotMatched (1002),
        UuidNotMatched (1003);

        private int value;

         ExceptionCode(int value){
            this.value=value;
        }

        public ExceptionCode valueOf(int value){
            switch (value){
                case 1000:
                    return ExceptionCode.NotFoundUuid;
                case 1001:
                    return ExceptionCode.NotFoundMatchedIp;
                case 1002:
                    return ExceptionCode.NameNotMatched;
                case 1003:
                    return ExceptionCode.UuidNotMatched;
            }
            return null;
        }

        public int getValue(){
            return value;
        }
    }
}
