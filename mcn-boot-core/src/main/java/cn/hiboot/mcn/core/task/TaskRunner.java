package cn.hiboot.mcn.core.task;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多线程任务启动器
 *
 * @author DingHao
 * @since 2020/5/29 11:42
 */
public class TaskRunner {

    private ExecutorService executorService;

    private List<Runnable> runnableList;

    private boolean async;

    public TaskRunner() {
        this(false);
    }

    public TaskRunner(int coreSize) {
        this(coreSize,false);
    }

    public TaskRunner(boolean async) {
        this(Runtime.getRuntime().availableProcessors(),async);
    }

    public TaskRunner(int coreSize,boolean async) {
        init(coreSize,async);
    }

    private void init(int coreSize,boolean async) {
       this.async = async;
       this.runnableList = new ArrayList<>();
       this.executorService = Executors.newFixedThreadPool(coreSize,new CustomThreadFactory("TaskRunner"));
    }

    public void add(Runnable runnable){
        runnableList.add(runnable);
    }

    public void run(){
        if(runnableList.isEmpty()){
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(runnableList.size());
        for (Runnable runnable : runnableList) {
            executorService.execute(() -> {
                runnable.run();
                countDownLatch.countDown();
            });
        }
        if(async){
            return;
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            //ignore
        }finally {
            runnableList.clear();
        }
    }

    private static class CustomThreadFactory implements ThreadFactory {

        private final String namePrefix;
        private final AtomicInteger nextId = new AtomicInteger(1);

        public CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix + "-Worker-";
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, namePrefix + nextId.getAndDecrement());
        }
    }

}
