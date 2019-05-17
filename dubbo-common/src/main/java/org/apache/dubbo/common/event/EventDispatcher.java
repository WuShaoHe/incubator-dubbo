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

import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.extension.SPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import static java.util.stream.StreamSupport.stream;

/**
 * {@link Event Dubbo Event} Dispatcher
 *
 * @see Event
 * @see EventListener
 * @see DirectEventDispatcher
 * @since 2.7.2
 */
@SPI("direct")
public interface EventDispatcher {

    /**
     * Direct {@link Executor} uses sequential execution model
     */
    Executor DIRECT_EXECUTOR = Runnable::run;

    /**
     * Add a {@link EventListener Dubbo event listener}
     *
     * @param listener a {@link EventListener Dubbo event listener}
     *                 If current {@link EventListener} is existed, return <code>false</code>
     * @throws NullPointerException     if <code>listener</code> argument is <code>null</code>
     * @throws IllegalArgumentException if <code>listener</code> argument is not concrete instance
     */
    void addEventListener(EventListener<?> listener) throws NullPointerException, IllegalArgumentException;

    /**
     * Add one or more {@link EventListener Dubbo event listeners}
     *
     * @param listener a {@link EventListener Dubbo event listener}
     * @param others   an optional {@link EventListener Dubbo event listeners}
     * @throws NullPointerException     if one of arguments is <code>null</code>
     * @throws IllegalArgumentException if one of arguments argument is not concrete instance
     */
    default void addEventListeners(EventListener<?> listener, EventListener<?>... others) throws NullPointerException,
            IllegalArgumentException {
        List<EventListener<?>> listeners = new ArrayList<>(1 + others.length);
        listeners.add(listener);
        listeners.addAll(Arrays.asList(others));
        addEventListeners(listeners);
    }

    /**
     * Add multiple {@link EventListener Dubbo event listeners}
     *
     * @param listeners the {@link EventListener Dubbo event listeners}
     * @throws NullPointerException     if <code>listeners</code> argument is <code>null</code>
     * @throws IllegalArgumentException if any element of <code>listeners</code> is not concrete instance
     */
    default void addEventListeners(Iterable<EventListener<?>> listeners) throws NullPointerException, IllegalArgumentException {
        stream(listeners.spliterator(), false).forEach(this::addEventListener);
    }

    /**
     * Remove a a {@link EventListener Dubbo event listener}
     *
     * @param listener a {@link EventListener Dubbo event listener}
     * @return If remove successfully, return <code>true</code>.
     * If current {@link EventListener} is existed, return <code>false</code>
     * @throws NullPointerException if <code>listener</code> argument is <code>null</code>
     */
    void removeEventListener(EventListener<?> listener) throws NullPointerException, IllegalArgumentException;

    /**
     * Remove a {@link EventListener Dubbo event listener}
     *
     * @param listeners the {@link EventListener Dubbo event listeners}
     * @return If remove successfully, return <code>true</code>.
     * If current {@link EventListener} is existed, return <code>false</code>
     * @throws NullPointerException     if <code>listener</code> argument is <code>null</code>
     * @throws IllegalArgumentException if any element of <code>listeners</code> is not concrete instance
     */
    default void removeListeners(Iterable<EventListener<?>> listeners) throws NullPointerException, IllegalArgumentException {
        stream(listeners.spliterator(), false).forEach(this::removeEventListener);
    }

    /**
     * Remove all {@link EventListener Dubbo event listeners}
     *
     * @return a amount of removed listeners
     */
    default void removeAllEventListeners() {
        removeListeners(getAllEventListeners());
    }

    /**
     * Get all registered {@link EventListener Dubbo event listeners}
     *
     * @return non-null read-only ordered {@link EventListener Dubbo event listeners}
     * @see EventListener#getPriority()
     */
    List<EventListener<?>> getAllEventListeners();

    /**
     * Dispatch a Dubbo event to the registered {@link EventListener Dubbo event listeners}
     *
     * @param event a {@link Event Dubbo event}
     */
    void dispatch(Event event);

    /**
     * The {@link Executor} to dispatch a {@link Event Dubbo event}
     *
     * @return default implementation directly invoke {@link Runnable#run()} method, rather than multiple-threaded
     * {@link Executor}. If the return value is <code>null</code>, the behavior is same as default.
     * @see #DIRECT_EXECUTOR
     */
    default Executor getExecutor() {
        return DIRECT_EXECUTOR;
    }

    /**
     * The default extension of {@link EventDispatcher} is loaded by {@link ExtensionLoader}
     *
     * @return the default extension of {@link EventDispatcher}
     */
    static EventDispatcher getDefaultExtension() {
        return ExtensionLoader.getExtensionLoader(EventDispatcher.class).getDefaultExtension();
    }
}
