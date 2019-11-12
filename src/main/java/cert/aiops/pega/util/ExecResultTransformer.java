package cert.aiops.pega.util;

import cert.aiops.pega.bean.HostState;
import cert.aiops.pega.bean.PegaEnum;
import cert.aiops.pega.bean.SinglePingState;
import cert.aiops.pega.startup.BeingMasterCondition;
import cert.aiops.pega.startup.BeingWorkerCondition;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.openssl.PEMDecryptor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

@Component
@Conditional(value = {BeingWorkerCondition.class})
public class ExecResultTransformer {

    public static String rewriteExecResult(String value) {
        value=value.replace("#", " ");
        return value;
    }

    public static ArrayList<SinglePingState> execResult2PingState(String execResult) {
        ArrayList<SinglePingState> pingStates = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructParametricType(ArrayList.class, SinglePingState.class);
        try {
            ArrayList<SinglePingState> singlePingStates = mapper.readValue(execResult, type);
            pingStates.addAll(singlePingStates);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pingStates;
    }

    private static PegaEnum .Avail int2enum(int num) {
        switch (num) {
            case 0:
                return PegaEnum.Avail.unavail;
            case 1:
                return PegaEnum.Avail.avail;
            default:
                return null;
        }
    }

    public static ArrayList<HostState> pingState2RequestHostState(ArrayList<SinglePingState> pingStates, String taskId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<HostState> hostStates = new ArrayList<>();
        for (SinglePingState state : pingStates) {
            HostState hostState = new HostState();
            hostState.setIp(state.getIp());
            hostState.setStatus(int2enum(state.getStatus()));
            hostState.setTaskId(taskId);
            try {
                hostState.setUpdate_time(formatter.parse(state.getUpdate_time()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            hostStates.add(hostState);
        }
        return hostStates;
    }

    public static ArrayList<HostState> pingState2RoutineHostState(ArrayList<SinglePingState> pingStates, long epoch, long systemId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<HostState> hostStates = new ArrayList<>();
        for (SinglePingState state : pingStates) {
            HostState hostState = new HostState();
            hostState.setIp(state.getIp());
            hostState.setStatus(int2enum(state.getStatus()));
            hostState.setEpoch(epoch);
            hostState.setSystemId(systemId);
            try {
                hostState.setUpdate_time(formatter.parse(state.getUpdate_time()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            hostStates.add(hostState);
        }
        return hostStates;
    }

    public static ArrayList<HostState> routineExecResulst2HostState(String execString, long epoch, long systemId) {
        ArrayList<SinglePingState> pingStates = ExecResultTransformer.execResult2PingState(execString);
        return ExecResultTransformer.pingState2RoutineHostState(pingStates, epoch, systemId);
    }

    public static ArrayList<SinglePingState> hostState2PingState(ArrayList<HostState> hostStates, String net) {
        ArrayList<SinglePingState> pingStates = new ArrayList<>();
        for (HostState state : hostStates) {
            SinglePingState pingState = new SinglePingState();
            pingState.setIp(state.getIp());
            pingState.setStatus(state.getStatus());
            pingState.setUpdate_time(state.getUpdate_time());
            pingState.setNet(net);
            pingStates.add(pingState);
        }
        return pingStates;
    }
}
