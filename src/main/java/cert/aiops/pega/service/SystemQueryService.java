package cert.aiops.pega.service;

import cert.aiops.pega.bean.SystemQueryResponse;

import java.util.Date;
import java.util.concurrent.Future;

public interface SystemQueryService {

    public Future<SystemQueryResponse> getSystemState(String SystemName, int pageNumber, int pageSize, Date createdTime);
}
