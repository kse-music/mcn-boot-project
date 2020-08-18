package cn.hiboot.mcn.core.util;

import java.util.List;
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

    /**
     * 多少次执行一次consumer
     *
     * @param all 总输入
     * @param consumer 批量处理函数
     * @param <S> 集合元素
     */
    default <S> void operation(List<S> all, Consumer<List<S>> consumer) {
        if (all == null || all.isEmpty()) {
            return;
        }
        int count = (all.size() - 1) / getBatchSize() + 1;
        if (count == 1) {
            consumer.accept(all);
            return;
        }
        int toIndex;
        for (int i = 0; i < count; i++) {
            if (i == count - 1) {
                toIndex = all.size();
            } else {
                toIndex = (i + 1) * getBatchSize();
            }
            consumer.accept(all.subList(i * getBatchSize(), toIndex));
        }
        all.clear();
    }
}
