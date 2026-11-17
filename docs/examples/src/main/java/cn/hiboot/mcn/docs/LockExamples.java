package cn.hiboot.mcn.docs;

import cn.hiboot.mcn.autoconfigure.redis.DistributedLocker;
import cn.hiboot.mcn.autoconfigure.redis.annotation.DistributedLock;
import cn.hiboot.mcn.core.exception.ServiceException;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class LockExamples {
    private final DistributedLocker locker;

    public LockExamples(DistributedLocker locker) {
        this.locker = locker;
    }

    public <T> T execute(String key, Supplier<T> operation) {
        if (!locker.tryLock(key, 5, 10)) {
            throw ServiceException.newInstance("暂时无法获取锁");
        }
        try {
            return operation.get();
        } finally {
            locker.unlock(key);
        }
    }

    @DistributedLock("catalog:refresh")
    public String refresh() {
        return "updated";
    }
}
