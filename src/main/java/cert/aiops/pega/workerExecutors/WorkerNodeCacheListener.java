package cert.aiops.pega.workerExecutors;

import cert.aiops.pega.masterExecutors.PegaNodeCacheListener;

@Deprecated
public class WorkerNodeCacheListener extends PegaNodeCacheListener {

    public void nodeChanged() throws Exception {
        super.nodeChanged();

    }
}
