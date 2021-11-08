package cn.hiboot.mcn.core.task;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description about this class
 *
 * @author DingHao
 * @since 2020/11/14 16:32
 */
public class TaskThreadPool extends ThreadPoolExecutor {

    private final int workQueueSize;


    public TaskThreadPool(int corePoolSize, int workQueueSize, String namePrefix){
        this(corePoolSize,corePoolSize,workQueueSize,namePrefix);
    }

    public TaskThreadPool(int corePoolSize, int maximumPoolSize, int workQueueSize, String threadNamePrefix){
        super(corePoolSize,maximumPoolSize,0,TimeUnit.MICROSECONDS,new LinkedBlockingDeque<>(workQueueSize),new CustomThreadFactory(threadNamePrefix));
        this.workQueueSize = workQueueSize;
    }

    @Override
    public void execute(Runnable runnable){
        //防止队列数过多OOM
        while (blocking()){

        }
        super.execute(runnable);
    }

    private boolean blocking() {
        return getPoolSize() == getMaximumPoolSize() && getQueue().size() == workQueueSize;
    }

    public void closeUntilAllTaskFinish(){
        shutdown();
        while (!isTerminated()){

        }
    }

    private static class CustomThreadFactory implements ThreadFactory {

        private final String namePrefix;
        private final AtomicInteger nextId = new AtomicInteger();

        public CustomThreadFactory(String name) {
            this.namePrefix = name + "-Worker";
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,namePrefix + nextId.getAndIncrement());
        }

    }

}
