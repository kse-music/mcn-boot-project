package cn.hiboot.mcn.core.task;

import cn.hiboot.mcn.core.util.McnAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 批量异步执行器
 *
 * @author DingHao
 * @since 2020/11/16 15:46
 */
public class TaskExecutor<T> {

    private final TaskThreadPool taskThreadPool;
    private final Iterable<T> iterable;
    private final int perBatchSize;

    public TaskExecutor(Iterable<T> iterable) {
        this(iterable, 1000);
    }

    public TaskExecutor(Iterable<T> iterable, int perBatchSize) {
        this(iterable, TaskThreadPool.builder().shutdownUntilFinish(true).build(), perBatchSize);
    }

    public TaskExecutor(Iterable<T> iterable, TaskThreadPool taskThreadPool, int perBatchSize) {
        this.iterable = iterable;
        this.perBatchSize = perBatchSize;
        this.taskThreadPool = taskThreadPool;
    }

    public void execute(Consumer<List<T>> consumer) {
        execute(Function.identity(), consumer);
    }

    public <S> void execute(Function<T, S> converter, Consumer<List<S>> consumer) {
        execute(converter, consumer, false);
    }

    public <S> void execute(Function<T, S> converter, Consumer<List<S>> consumer, boolean nullBreak) {
        McnAssert.notNull(converter, "converter must not be null");
        McnAssert.notNull(consumer, "consumer must not be null");
        List<S> data = new ArrayList<>(perBatchSize);
        for (T t : iterable) {
            S apply = converter.apply(t);
            if (apply == null) {
                if (nullBreak) {
                    break;
                }
                continue;
            }
            data.add(apply);
            if (data.size() == perBatchSize) {
                doExecute(data, consumer);
                data = new ArrayList<>();
            }
        }
        if (!data.isEmpty()) {
            doExecute(data, consumer);
        }
        if (taskThreadPool != null) {
            taskThreadPool.shutdown();
        }
    }

    private <S> void doExecute(List<S> data, Consumer<List<S>> consumer) {
        if (taskThreadPool == null) {
            consumer.accept(data);
            return;
        }
        taskThreadPool.execute(() -> consumer.accept(data));
    }

}
