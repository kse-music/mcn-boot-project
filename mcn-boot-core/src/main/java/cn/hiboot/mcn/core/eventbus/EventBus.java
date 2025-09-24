package cn.hiboot.mcn.core.eventbus;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
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
        for (Method method : getAllSubscribeMethods(unwrapProxyClass(listener))) {
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1) {
                throw new IllegalArgumentException("Subscribe method must have exactly 1 parameter");
            }
            method.setAccessible(true);
            subscribers.computeIfAbsent(params[0], k -> new ArrayList<>()).add(new Subscriber(listener, method));
        }
    }

    private List<Class<?>> unwrapProxyClass(Object bean) {
        List<Class<?>> result = new ArrayList<>();
        Class<?> clazz = bean.getClass();
        if (Proxy.isProxyClass(clazz)) {
            Collections.addAll(result, clazz.getInterfaces());
        } else if (clazz.getName().contains("$$")) {
            result.add(clazz.getSuperclass());
        } else {
            result.add(clazz);
        }
        return result;
    }

    private List<Method> getAllSubscribeMethods(List<Class<?>> classes) {
        List<Method> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<Class<?>> queue = new LinkedList<>(classes);
        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();
            if (current == null || current == Object.class) {
                continue;
            }
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Subscribe.class) && !method.isSynthetic()) {
                    String sig = method.getName() + Arrays.toString(method.getParameterTypes());
                    if (visited.add(sig)) {
                        result.add(method);
                    }
                }
            }
            queue.add(current.getSuperclass());
            Collections.addAll(queue, current.getInterfaces());
        }
        return result;
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

    private record Subscriber(Object target, Method method) {

    }

}

