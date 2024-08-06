package cn.hiboot.mcn.core.task;

import cn.hiboot.mcn.core.util.McnAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * AccumulateExecutor
 *
 * @author DingHao
 * @since 2024/8/6 10:57
 */
public final class AccumulateExecutor<T> {

    private final TaskThreadPool taskThreadPool;
    private List<T> datum;
    private final Consumer<List<T>> consumer;
    private final int batchSize;

    public AccumulateExecutor(Consumer<List<T>> consumer) {
        this(1000, consumer);
    }

    public AccumulateExecutor(int batchSize, Consumer<List<T>> consumer) {
        McnAssert.state(batchSize > 0, "batchSize must gt 0");
        McnAssert.notNull(consumer, "consumer must not be null");
        this.batchSize = batchSize;
        this.consumer = consumer;
        this.datum = new ArrayList<>(batchSize);
        this.taskThreadPool = TaskThreadPool.builder().build();
    }

    public void add(T data) {
        this.datum.add(data);
        if (this.datum.size() == this.batchSize) {
            execute(this.datum, false);
            this.datum = new ArrayList<>(this.batchSize);
        }
    }

    private void execute(List<T> data, boolean waitAllTaskComplete) {
        if (data.isEmpty()) {
            return;
        }
        this.taskThreadPool.execute(() -> consumer.accept(data));
        close(waitAllTaskComplete);
    }

    public void finish() {
        execute(this.datum, false);
    }

    public void finishSync() {
        execute(this.datum, true);
    }

    private void close(boolean shutdownUntilAllTaskComplete) {
        this.taskThreadPool.shutdownUntilAllTaskComplete = shutdownUntilAllTaskComplete;
        this.taskThreadPool.shutdown();
    }

}
