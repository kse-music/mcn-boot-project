package cn.hiboot.mcn.core.task;

import cn.hiboot.mcn.core.util.McnAssert;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * AccumulateExecutor
 *
 * @author DingHao
 * @since 2024/8/6 10:57
 */
public final class AccumulateExecutor<T> implements Closeable {

    private final TaskThreadPool taskThreadPool;
    private List<T> datum;
    private final Consumer<List<T>> consumer;
    private final int batchSize;
    private final boolean syncExecute;

    public AccumulateExecutor(Consumer<List<T>> consumer) {
        this(1000, consumer);
    }

    public AccumulateExecutor(int batchSize, Consumer<List<T>> consumer) {
        this(batchSize, consumer, false);
    }

    public AccumulateExecutor(int batchSize, Consumer<List<T>> consumer, boolean syncExecute) {
        McnAssert.state(batchSize > 0, "batchSize must gt 0");
        McnAssert.notNull(consumer, "consumer must not be null");
        this.batchSize = batchSize;
        this.syncExecute = syncExecute;
        this.consumer = consumer;
        this.datum = new ArrayList<>(batchSize);
        this.taskThreadPool = TaskThreadPool.builder().build();
    }

    public void add(T data) {
        this.datum.add(data);
        if (this.datum.size() == this.batchSize) {
            execute(this.datum);
            this.datum = new ArrayList<>(this.batchSize);
        }
    }

    private void execute(List<T> data) {
        if (data.isEmpty()) {
            return;
        }
        this.taskThreadPool.execute(() -> consumer.accept(data));
    }

    public void done() {
        execute(this.datum);
    }

    @Override
    public void close() {
        done();
        if (this.syncExecute) {
            this.taskThreadPool.shutdownUntilAllTaskComplete = true;
        }
        this.taskThreadPool.shutdown();
    }

}
