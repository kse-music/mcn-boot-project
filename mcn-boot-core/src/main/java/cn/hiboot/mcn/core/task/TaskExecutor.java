package cn.hiboot.mcn.core.task;

import cn.hiboot.mcn.core.util.BatchOperation;
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
public class TaskExecutor<T>{

    private final TaskThreadPool taskThreadPool;
    private final Iterable<T> iterable;
    private final int perBatchSize;

    public TaskExecutor(Iterable<T> iterable) {
        this(iterable, BatchOperation.DEFAULT_BATCH_SIZE);
    }

    public TaskExecutor(Iterable<T> iterable, int perBatchSize) {
        this(iterable,new TaskThreadPool(),perBatchSize);
    }

    public TaskExecutor(Iterable<T> iterable, TaskThreadPool taskThreadPool, int perBatchSize) {
        this.iterable = iterable;
        this.perBatchSize = perBatchSize;
        this.taskThreadPool = taskThreadPool;
    }

    public void execute(Consumer<List<T>> opr){
        execute(Function.identity(),opr);
    }

    public <S> void execute(Function<T, S> convert, Consumer<List<S>> opr) {
        execute(convert,opr,false);
    }

    public <S> void execute(Function<T, S> convert, Consumer<List<S>> opr,boolean nullBreak){
        McnAssert.notNull(convert,"convert must not be null");
        List<S> data = new ArrayList<>(perBatchSize);
        for (T t : iterable) {
            S apply = convert.apply(t);
            if(apply == null){
                if(nullBreak){
                    break;
                }
                continue;
            }
            data.add(apply);
            if(data.size() == perBatchSize){
                execute0(data,opr);
                data = new ArrayList<>();
            }
        }
        if(!data.isEmpty()){
            execute0(data,opr);
        }
        taskThreadPool.closeUntilAllTaskFinish();
    }

    private <S> void execute0(List<S> data,Consumer<List<S>> opr){
        taskThreadPool.execute(() -> opr.accept(data));
    }

}
