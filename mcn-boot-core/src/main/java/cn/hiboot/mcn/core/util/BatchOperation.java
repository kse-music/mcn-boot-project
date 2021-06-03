package cn.hiboot.mcn.core.util;

import cn.hiboot.mcn.core.task.TaskThreadPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * 批量操作接口，默认批量大小为 10000
 *
 * @author DingHao
 * @since 2019/8/15 13:21
 */
public interface BatchOperation {

    int DEFAULT_BATCH_SIZE = 10000;

    /**
     * 默认1w执行一次
     * @return 批量大小
     */
    default int getBatchSize(){
        return DEFAULT_BATCH_SIZE;
    }

    default TaskThreadPool getExecutor(){
        return null;
    }

    default <S> void operation(Iterable<S> all, Consumer<Collection<S>> consumer) {
        operation(all,consumer,false);
    }

    /**
     * 多少次执行一次consumer
     *
     * @param all 总输入
     * @param consumer 批量处理函数
     * @param <S> 集合元素
     * @param close 等待所有任务执行完关闭线程池
     */
    default <S> void operation(Iterable<S> all, Consumer<Collection<S>> consumer,boolean close) {
        if (all == null) {
            return;
        }
        TaskThreadPool executor = getExecutor();
        int index = 0;
        Collection<S> tmp = new ArrayList<>();
        for (S next : all) {
            tmp.add(next);
            index++;
            if (index % getBatchSize() == 0) {
                if(executor == null){
                    consumer.accept(tmp);
                }else {
                    Collection<S> finalTmp = tmp;
                    executor.execute(() -> consumer.accept(finalTmp));
                }
                index = 0;
                tmp = new ArrayList<>();
            }
        }
        if(index != 0){
            consumer.accept(tmp);
        }
        if(executor != null && close){
            executor.closeUntilAllTaskFinish();
        }
        tmp.clear();
    }
}
