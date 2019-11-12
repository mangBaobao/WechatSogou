package cert.aiops.pega.bean;

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
}
