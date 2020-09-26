package cn.hiboot.mcn.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
    default <S> void operation(Collection<S> all, Consumer<Collection<S>> consumer) {
        if (all == null || all.isEmpty()) {
            return;
        }
        int index = 0;
        Collection<S> tmp = new ArrayList<>();
        for (S next : all) {
            tmp.add(next);
            index++;
            if (index % getBatchSize() == 0) {
                consumer.accept(tmp);
                index = 0;
                tmp = new ArrayList<>();
            }
        }
        if(index != 0){
            consumer.accept(tmp);
        }
        tmp.clear();
    }
}
