package cn.hiboot.mcn.core.util;

import java.util.List;
import java.util.function.Consumer;

/**
 * 批量操作接口，默认批量大小为 10W
 *
 * @author DingHao
 * @since 2019/8/15 13:21
 */
public interface BatchOperation {

    int DEFAULT_BATCH_SIZE = 100000;

    default int getBatchSize(){
        return DEFAULT_BATCH_SIZE;
    }

    /**
     * 多少次执行一次,
     * 输入给函数的是list视图，所以不要对其增删
     *
     * @param all
     * @param consumer
     * @param <S>
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
