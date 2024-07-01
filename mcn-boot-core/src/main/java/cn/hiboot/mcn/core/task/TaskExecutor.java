package cn.hiboot.mcn.core.task;

import cn.hiboot.mcn.core.util.McnAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 批量异步执行器
 * 默认异步执行所有任务,等待所有任务结束并自动关闭线程池
 *
 * @author DingHao
 * @since 2020/11/16 15:46
 */
public final class TaskExecutor<T> {

    private final TaskThreadPool taskThreadPool;
    private final Iterable<T> iterable;
    private final int batchSize;
    private final boolean autoShutdown;

    private TaskExecutor(Iterable<T> iterable, TaskThreadPool taskThreadPool, int batchSize, boolean autoShutdown) {
        this.iterable = iterable;
        this.batchSize = batchSize;
        this.taskThreadPool = taskThreadPool;
        this.autoShutdown = autoShutdown;
    }

    public void execute(Consumer<List<T>> consumer) {
        execute(consumer, null);
    }

    @SuppressWarnings("unchecked")
    public <S> void execute(Consumer<List<S>> consumer, Function<T, S> converter) {
        McnAssert.notNull(consumer, "consumer must not be null");
        List<S> data = new ArrayList<>(this.batchSize);
        for (T t : this.iterable) {
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
                doExecute(data, consumer);
                data = new ArrayList<>();
            }
        }
        if (!data.isEmpty()) {
            doExecute(data, consumer);
        }
        if (this.taskThreadPool != null && autoShutdown) {
            this.taskThreadPool.shutdown();
        }
    }

    private <S> void doExecute(List<S> data, Consumer<List<S>> consumer) {
        if (this.taskThreadPool == null) {
            consumer.accept(data);
            return;
        }
        this.taskThreadPool.execute(() -> consumer.accept(data));
    }

    public static <B> Builder<B> data(Iterable<B> iterable) {
        return new Builder<>(iterable);
    }

    public static class Builder<B> {

        private TaskThreadPool taskThreadPool = TaskThreadPool.builder().build();
        private final Iterable<B> iterable;
        private int batchSize = 1000;
        private boolean autoShutdown = true;
        private boolean shutdownUntilFinish = true;

        private Builder(Iterable<B> iterable) {
            this.iterable = iterable;
        }

        public Builder<B> taskThreadPool(TaskThreadPool taskThreadPool) {
            this.taskThreadPool = taskThreadPool;
            return this;
        }

        public Builder<B> batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder<B> autoShutdown(boolean autoShutdown) {
            this.autoShutdown = autoShutdown;
            return this;
        }

        public Builder<B> shutdownUntilFinish(boolean shutdownUntilFinish) {
            this.shutdownUntilFinish = shutdownUntilFinish;
            return this;
        }

        public TaskExecutor<B> build() {
            if (this.taskThreadPool != null) {
                this.taskThreadPool.shutdownUntilFinish = this.shutdownUntilFinish;
            }
            return new TaskExecutor<>(this.iterable, this.taskThreadPool, this.batchSize, this.autoShutdown);
        }

    }

}
