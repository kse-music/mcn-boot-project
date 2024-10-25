package cn.hiboot.mcn.core.task;

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

    private final TaskExecutor taskExecutor;
    private final Consumer<List<T>> consumer;
    private List<T> datum;
    private final int batchSize;

    public AccumulateExecutor(TaskExecutor taskExecutor, Consumer<List<T>> consumer) {
        this.taskExecutor = taskExecutor;
        this.consumer = consumer;
        this.batchSize = taskExecutor.batchSize;
        this.datum = new ArrayList<>(batchSize);
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
        this.taskExecutor.execute(data, this.consumer);
    }

    public void done() {
        execute(this.datum);
    }

}
