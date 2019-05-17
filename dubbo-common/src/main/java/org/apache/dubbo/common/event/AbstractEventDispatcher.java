/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.common.event;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static org.apache.dubbo.common.event.Listener.findEventType;

/**
 * The abstract {@link EventDispatcher} providers the common implementation.
 *
 * @since 2.7.2
 */
public abstract class AbstractEventDispatcher implements EventDispatcher {

    private final Object mutex = new Object();

    private final ConcurrentMap<Class<? extends Event>, List<Listener>> listenersCache = new ConcurrentHashMap<>();

    @Override
    public void addListener(Listener<?> listener) throws NullPointerException, IllegalArgumentException {
        assertListener(listener);
        doInListener(listener, listeners -> {
            addIfAbsent(listeners, listener);
        });
    }

    @Override
    public void removeListener(Listener<?> listener) throws NullPointerException, IllegalArgumentException {
        assertListener(listener);
        doInListener(listener, listeners -> listeners.remove(listener));
    }

    @Override
    public List<Listener<?>> getAllListeners() {
        List<Listener<?>> listeners = new LinkedList<>();

        listenersCache
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .forEach(listener -> {
                    addIfAbsent(listeners, listener);
                });

        sort((List) listeners);

        return unmodifiableList(listeners);
    }

    private <E> void addIfAbsent(Collection<E> collection, E element) {
        if (!collection.contains(element)) {
            collection.add(element);
        }
    }

    @Override
    public void dispatch(Event event) {
        Executor executor = getExecutor();

        if (executor == null) { // If absent, uses DIRECT_EXECUTOR
            executor = DIRECT_EXECUTOR;
        }

        // execute in sequential or parallel execution model
        executor.execute(() -> {
            listenersCache.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().isAssignableFrom(event.getClass()))
                    .map(Map.Entry::getValue)
                    .flatMap(Collection::stream)
                    .forEach(listener -> {
                        listener.onEvent(event);
                    });
        });
    }

    protected void doInListener(Listener<?> listener, Consumer<Collection<Listener>> consumer) {

        Class<? extends Event> eventType = findEventType(listener);

        if (eventType != null) {
            synchronized (mutex) {
                List<Listener> listeners = listenersCache.computeIfAbsent(eventType, e -> new LinkedList<>());
                // consume
                consumer.accept(listeners);
                // sort
                sort(listeners);
            }
        }
    }

    static void assertListener(Listener<?> listener) throws NullPointerException {
        if (listener == null) {
            throw new NullPointerException("The listener must not be null.");
        }

        Class<?> listenerClass = listener.getClass();

        int modifiers = listenerClass.getModifiers();

        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
            throw new IllegalArgumentException("The listener must be concrete class");
        }
    }
}
