package cn.hiboot.mcn.core.task;

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

    private static final int DEFAULT_BATCH_SIZE = 1000;
    private final TaskThreadPool myThreadPoolExecutor;
    private final Iterable<T> iterable;
    private final int perBatchSize;

    public TaskExecutor(Iterable<T> iterable) {
        this(iterable,DEFAULT_BATCH_SIZE);
    }

    public TaskExecutor(Iterable<T> iterable, int perBatchSize) {
        this(iterable,new TaskThreadPool(Runtime.getRuntime().availableProcessors(), 10, "BatchTask"),perBatchSize);
    }

    public TaskExecutor(Iterable<T> iterable, TaskThreadPool myThreadPoolExecutor, int perBatchSize) {
        this.iterable = iterable;
        this.perBatchSize = perBatchSize;
        this.myThreadPoolExecutor = myThreadPoolExecutor;
    }

    public void execute(Consumer<List<T>> opr){
        execute(Function.identity(),opr);
    }

    public <S> void execute(Function<T, S> convert, Consumer<List<S>> opr) {
        execute(convert,opr,false);
    }

    public <S> void execute(Function<T, S> convert, Consumer<List<S>> opr,boolean nullBreak){
        assert convert != null;
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
        myThreadPoolExecutor.closeUntilAllTaskFinish();
    }

    private <S> void execute0(List<S> data,Consumer<List<S>> opr){
        myThreadPoolExecutor.execute(() -> opr.accept(data));
    }

}
