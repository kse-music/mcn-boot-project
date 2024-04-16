package cn.hiboot.mcn.core.util;

import cn.hiboot.mcn.core.task.TaskThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 批量操作接口，默认批量大小为 1000
 *
 * @author DingHao
 * @since 2019/8/15 13:21
 */
public interface BatchOperation {

    int DEFAULT_BATCH_SIZE = 1000;

    /**
     * 默认1k执行一次
     * @return 批量大小
     */
    default int getBatchSize(){
        return DEFAULT_BATCH_SIZE;
    }

    default TaskThreadPool getExecutor(){
        return null;
    }

    default <S> void operation(Iterable<S> all, Consumer<List<S>> consumer) {
        operation(all,consumer,true);
    }

    default <S> void asyncOperation(Iterable<S> all, Consumer<List<S>> consumer) {
        operation(all,consumer,false);
    }

    /**
     * 多少次执行一次consumer
     *
     * @param all 总输入
     * @param consumer 批量处理函数
     * @param <S> 集合元素
     * @param closeWaitFinish 等待所有任务执行完关闭线程池须配合TaskThreadPool
     */
    default <S> void operation(Iterable<S> all, Consumer<List<S>> consumer, boolean closeWaitFinish) {
        if (all == null) {
            return;
        }
        TaskThreadPool executor = getExecutor();
        List<S> tmp = new ArrayList<>();
        for (S next : all) {
            tmp.add(next);
            if (tmp.size() % getBatchSize() == 0) {
                if(executor == null){
                    consumer.accept(tmp);
                }else {
                    List<S> finalTmp = tmp;
                    executor.execute(() -> consumer.accept(finalTmp));
                }
                tmp = new ArrayList<>();
            }
        }
        if(!tmp.isEmpty()){
            consumer.accept(tmp);
        }
        tmp.clear();
        if(executor != null && closeWaitFinish){
            executor.closeUntilAllTaskFinish();
        }
    }
}
