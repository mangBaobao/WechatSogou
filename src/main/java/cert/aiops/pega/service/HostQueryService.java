package cert.aiops.pega.service;

import cert.aiops.pega.bean.RequestTaskResponse;

import java.util.Date;
import java.util.concurrent.Future;

public interface HostQueryService {
    public Future<RequestTaskResponse> getHostState(String net, String[] ips, Date createdTime);
}
