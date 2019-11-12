package cert.aiops.pega.service;

import cert.aiops.pega.bean.FilterResponse;

import java.util.Date;
import java.util.concurrent.Future;

public interface FilterQueryService {
    public Future<FilterResponse> getUnavailBySystems(Date createdTime);
}
