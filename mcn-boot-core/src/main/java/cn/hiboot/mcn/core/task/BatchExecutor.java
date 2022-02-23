package cn.hiboot.mcn.core.task;

import cn.hiboot.mcn.core.util.McnAssert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * BatchExecutor
 *
 * @author DingHao
 * @since 2022/2/23 11:01
 */
public class BatchExecutor<T> {

    private final TaskThreadPool taskThreadPool;
    private List<T> data;
    private final Consumer<List<T>> consumer;
    private final int batchSize;
    private boolean sync;
    private boolean finish;

    public BatchExecutor(int batchSize,Consumer<List<T>> consumer) {
        this(batchSize,consumer,null);
    }

    public BatchExecutor(int batchSize,Consumer<List<T>> consumer,TaskThreadPool taskThreadPool) {
        McnAssert.state(batchSize >= 0,"batchSize must gt 0");
        McnAssert.notNull(consumer,"consumer must not be null");
        this.batchSize = batchSize;
        this.consumer = consumer;
        this.data = new ArrayList<>(batchSize);
        this.taskThreadPool = taskThreadPool;
    }

    public BatchExecutor<T> sync(){
        this.sync = true;
        return this;
    }

    public void add(T d){
        if(finish){
            return;
        }
        data.add(d);
        if(data.size() % batchSize == 0){
            doExecute();
        }
    }

    private void doExecute(){
        if(taskThreadPool == null){
            consumer.accept(data);
        }else {
            List<T> d = data;
            taskThreadPool.execute(() -> consumer.accept(d));
        }
        if(finish){
            data = Collections.emptyList();
            return;
        }
        data = new ArrayList<>(batchSize);
    }

    public void finish(){
        this.finish = true;
        if(data.isEmpty()){
            return;
        }
        doExecute();
        if(sync && taskThreadPool != null){
            taskThreadPool.closeUntilAllTaskFinish();
        }
    }

}
