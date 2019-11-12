package cert.aiops.pega.masterExecutors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;


public class TraceableThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {
    Logger logger = LoggerFactory.getLogger(TraceableThreadPoolTaskExecutor.class);

    private void showThreadPoolInfo(String prefix){
        ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor();
        if(threadPoolExecutor == null)
            return;
//        logger.info("{},{}, taskCount [{}], completedTaskCount [{}], activeCount [{}], queueSize [{}]", this.getThreadNamePrefix(), prefix, threadPoolExecutor.getTaskCount(),
//                threadPoolExecutor.getCompletedTaskCount(),threadPoolExecutor.getActiveCount(),threadPoolExecutor.getQueue().size());
    }

    @Override
    public Future<?> submit(Runnable task){
        showThreadPoolInfo("via submit(Runnable)");
        return super.submit(task);
    }

    @Override
    public <T> Future<T>  submit(Callable<T> task){
        showThreadPoolInfo("via submit(Callable");
        return super.submit(task);
    }
    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        showThreadPoolInfo("via submitListenable(Runnable");
        return super.submitListenable(task);
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task){
        showThreadPoolInfo("via submitListenable(Callable");
        return super.submitListenable(task);
    }
}
