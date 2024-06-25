package cn.hiboot.mcn.core.task;

import cn.hiboot.mcn.core.util.McnUtils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TaskThreadPool
 *
 * @author DingHao
 * @since 2020/11/14 16:32
 */
public final class TaskThreadPool extends ThreadPoolExecutor {

    private boolean shutdownUntilFinish = false;

    private TaskThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if (shutdownUntilFinish) {
            McnUtils.loopEnd(this::isTerminated);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final RejectedExecutionHandler defaultHandler = new ThreadPoolExecutor.CallerRunsPolicy();

        private RejectedExecutionHandler handler = defaultHandler;

        private long keepAliveTime = 0L;

        private int corePoolSize = Runtime.getRuntime().availableProcessors();

        private int maximumPoolSize = Runtime.getRuntime().availableProcessors();

        private int blockingQueueSize = 1000;

        private String threadNamePrefix = "BatchTask";

        private boolean shutdownUntilFinish = false;

        public Builder handler(RejectedExecutionHandler handler) {
            this.handler = handler;
            return this;
        }

        public Builder keepAliveTime(long keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
            return this;
        }

        public Builder corePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
            return this;
        }

        public Builder maximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
            return this;
        }

        public Builder blockingQueueSize(int blockingQueueSize) {
            this.blockingQueueSize = blockingQueueSize;
            return this;
        }

        public Builder threadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
            return this;
        }

        public Builder shutdownUntilFinish(boolean shutdownUntilFinish) {
            this.shutdownUntilFinish = shutdownUntilFinish;
            return this;
        }

        public TaskThreadPool build() {
            TaskThreadPool taskThreadPool = new TaskThreadPool(
                    corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(blockingQueueSize),
                    new TaskThreadFactory(threadNamePrefix),
                    handler
            );
            taskThreadPool.shutdownUntilFinish = shutdownUntilFinish;
            return taskThreadPool;
        }

    }

    private static class TaskThreadFactory implements ThreadFactory {

        private final String namePrefix;
        private final AtomicInteger nextId = new AtomicInteger();

        public TaskThreadFactory(String name) {
            this.namePrefix = name + "-Worker-";
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,namePrefix + nextId.getAndIncrement());
        }

    }

}
