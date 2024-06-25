package cn.hiboot.mcn.core.util;

import cn.hiboot.mcn.core.task.TaskExecutor;

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
     *
     * @return 批量大小
     */
    default int getBatchSize() {
        return DEFAULT_BATCH_SIZE;
    }

    default <S> void operation(Iterable<S> all, Consumer<List<S>> consumer) {
        TaskExecutor<S> taskExecutor = new TaskExecutor<>(all, null, getBatchSize());
        taskExecutor.execute(consumer);
    }

    default <S> void asyncOperation(Iterable<S> all, Consumer<List<S>> consumer) {
        TaskExecutor<S> taskExecutor = new TaskExecutor<>(all);
        taskExecutor.execute(consumer);
    }

}
