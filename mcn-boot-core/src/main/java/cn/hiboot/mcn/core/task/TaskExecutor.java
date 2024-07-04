package cn.hiboot.mcn.core.task;

import cn.hiboot.mcn.core.util.McnAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 批量异步任务执行器
 *
 * @author DingHao
 * @since 2020/11/16 15:46
 */
public final class TaskExecutor {

    private TaskThreadPool taskThreadPool;
    private final int batchSize;
    private final boolean autoShutdown;
    private final boolean waitAllTaskComplete;
    private ExecutorCompletionService<Void> completionService;

    private TaskExecutor(TaskThreadPool taskThreadPool, int batchSize, boolean autoShutdown, boolean waitAllTaskComplete) {
        this.batchSize = batchSize;
        this.taskThreadPool = taskThreadPool;
        this.autoShutdown = autoShutdown;
        this.waitAllTaskComplete = waitAllTaskComplete;
        if (taskThreadPool != null) {
            this.completionService = new ExecutorCompletionService<>(this.taskThreadPool);
        }
    }

    public <T> void execute(Iterable<T> datum, Consumer<List<T>> consumer) {
        execute(datum, consumer, null);
    }

    @SuppressWarnings("unchecked")
    public <T, S> void execute(Iterable<T> datum, Consumer<List<S>> consumer, Function<T, S> converter) {
        McnAssert.notNull(datum, "datum must not be null");
        McnAssert.notNull(consumer, "consumer must not be null");
        List<S> data = new ArrayList<>(this.batchSize);
        int taskCount = 0;
        for (T t : datum) {
            if (converter == null) {
                data.add((S) t);
            } else {
                S result = converter.apply(t);
                if (result == null) {
                    continue;
                }
                data.add(result);
            }
            if (data.size() == this.batchSize) {
                submitTask(data, consumer);
                data = new ArrayList<>();
                taskCount++;
            }
        }
        if (!data.isEmpty()) {
            submitTask(data, consumer);
            taskCount++;
        }
        if (this.taskThreadPool != null) {
            if (this.waitAllTaskComplete) {
                waitForAllTasks(taskCount);
            }
            if (this.autoShutdown) {
                this.taskThreadPool.shutdown();
                this.taskThreadPool = null;
            }
        }
    }

    private void waitForAllTasks(int taskCount) {
        try {
            for (int i = 0; i < taskCount; i++) {
                this.completionService.take().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error while waiting for tasks to complete", e);
        }
    }

    private <S> void submitTask(List<S> data, Consumer<List<S>> consumer) {
        if (this.taskThreadPool == null) {
            consumer.accept(data);
        } else {
            this.completionService.submit(() -> {
                consumer.accept(data);
                return null;
            });
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private TaskThreadPool taskThreadPool = TaskThreadPool.builder().build();
        private int batchSize = 1000;
        private boolean autoShutdown = false;
        private boolean waitAllTaskComplete = false;

        public Builder taskThreadPool(TaskThreadPool taskThreadPool) {
            this.taskThreadPool = taskThreadPool;
            return this;
        }

        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder autoShutdown(boolean autoShutdown) {
            this.autoShutdown = autoShutdown;
            return this;
        }

        public Builder waitAllTaskComplete(boolean waitAllTaskComplete) {
            this.waitAllTaskComplete = waitAllTaskComplete;
            return this;
        }

        public TaskExecutor build() {
            return new TaskExecutor(this.taskThreadPool, this.batchSize, this.autoShutdown, this.waitAllTaskComplete);
        }

    }

}
