package cn.hiboot.mcn.core.eventbus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * EventBus
 *
 * @author DingHao
 * @since 2025/9/24 18:27
 */
public final class EventBus {

    private static final EventBus instance = new EventBus();

    private Executor executor;

    private final Map<Class<?>, List<Subscriber>> subscribers = new ConcurrentHashMap<>();

    private EventBus() {

    }

    public static EventBus defaults() {
        return instance;
    }

    public static EventBus create(Executor executor) {
        EventBus eventBus = new EventBus();
        eventBus.executor = executor;
        return eventBus;
    }

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 1) {
                    throw new IllegalArgumentException("Subscribe method must have exactly 1 parameter");
                }
                Class<?> eventType = params[0];
                method.setAccessible(true);
                subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(new Subscriber(listener, method));
            }
        }
    }

    public void post(Object event) {
        List<Subscriber> subs = subscribers.get(event.getClass());
        if (subs == null) {
            return;
        }
        for (Subscriber sub : subs) {
            try {
                sub.method.invoke(sub.target, event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void postAsync(Object event) {
        executor().execute(() -> post(event));
    }

    private Executor executor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
        return executor;
    }

    private static class Subscriber {

        private final Object target;
        private final Method method;

        Subscriber(Object target, Method method) {
            this.target = target;
            this.method = method;
        }

    }

}

