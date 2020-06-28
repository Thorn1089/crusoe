package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
public interface Component {

    static Component wrap(final Object instance) {
        /*
         * Cache the matching Methods so we only have to look them up once
         */
        final Map<Class<?>, Method> handlers = Stream.of(instance.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(Handler.class))
                .collect(Collectors.toMap(m -> m.getAnnotation(Handler.class).value(), m -> m));

        return (Component) Proxy.newProxyInstance(ClassLoader.getPlatformClassLoader(), new Class<?>[]{Component.class}, (proxy, method, args) -> {
            final List<Event<?>> batch = (List<Event<?>>)args[0];
            for(final Event<?> event : batch) {
                final Object payload = event.payload();
                final Class<?> target = payload.getClass();

                if(handlers.containsKey(target)) {
                    handlers.get(target).invoke(instance, payload);
                }
            }
            return null;
        });
    }

    void process(final List<Event<?>> batch);

}
