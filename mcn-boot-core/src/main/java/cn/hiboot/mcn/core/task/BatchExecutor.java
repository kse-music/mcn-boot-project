package cn.hiboot.mcn.core.task;

import cn.hiboot.mcn.core.util.McnAssert;

import java.util.ArrayList;
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

    public void add(T d){
        if(data == null){
            return;
        }
        data.add(d);
        if(data.size() % batchSize == 0){
            doExecute(data);
            this.data = new ArrayList<>(batchSize);
        }
    }

    private void doExecute(List<T> data){
        if(taskThreadPool == null){
            consumer.accept(data);
        }else {
            taskThreadPool.execute(() -> consumer.accept(data));
        }
    }

    public void finish(){
        finish(false);
    }

    public void finish(boolean sync){
        if(!data.isEmpty()){
            doExecute(data);
        }
        this.data = null;
        if(sync && taskThreadPool != null){
            taskThreadPool.closeUntilAllTaskFinish();
        }
    }

}
