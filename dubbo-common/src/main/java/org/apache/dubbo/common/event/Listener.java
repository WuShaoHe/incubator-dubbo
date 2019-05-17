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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EventListener;
import java.util.Objects;

import static java.lang.Integer.compare;
import static org.apache.dubbo.common.utils.ReflectUtils.findParameterizedTypes;

/**
 * The {@link Event Dubbo Event} Listener that is based on Java standard {@link EventListener} interface supports
 * the generic {@link Event}.
 * <p>
 * The {@link #onEvent(Event) handle method} will be notified when the matched-type {@link Event Dubbo Event} is
 * published, whose priority could be changed by {@link #getPriority()} method.
 *
 * @param <E> the concrete class of {@link Event Dubbo Event}
 * @see Event
 * @see EventListener
 * @since 2.7.2
 */
@FunctionalInterface
public interface Listener<E extends Event> extends EventListener, Comparable<Listener<E>> {

    /**
     * Handle a {@link Event Dubbo Event} when it's be published
     *
     * @param event a {@link Event Dubbo Event}
     */
    void onEvent(E event);

    /**
     * The priority of {@link Listener current listener}.
     *
     * @return the value is more greater, the priority is more lower.
     * {@link Integer#MIN_VALUE} indicates the highest priority. The default value is {@link Integer#MAX_VALUE}.
     * The comparison rule , refer to {@link #compareTo(Listener)}.
     * @see #compareTo(Listener)
     * @see Integer#MAX_VALUE
     * @see Integer#MIN_VALUE
     */
    default int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    default int compareTo(Listener<E> another) {
        return compare(this.getPriority(), another.getPriority());
    }

    /**
     * Find the {@link Class type} {@link Event Dubbo event} from the specified {@link Listener Dubbo event listener}
     *
     * @param listener the {@link Class class} of {@link Listener Dubbo event listener}
     * @return <code>null</code> if not found
     */
    static Class<? extends Event> findEventType(Listener<?> listener) {
        return findEventType(listener.getClass());
    }

    /**
     * Find the {@link Class type} {@link Event Dubbo event} from the specified {@link Listener Dubbo event listener}
     *
     * @param listenerClass the {@link Class class} of {@link Listener Dubbo event listener}
     * @return <code>null</code> if not found
     */
    static Class<? extends Event> findEventType(Class<?> listenerClass) {
        Class<? extends Event> eventType = null;

        if (listenerClass != null && Listener.class.isAssignableFrom(listenerClass)) {
            eventType = findParameterizedTypes(listenerClass)
                    .stream()
                    .map(Listener::findEventType)
                    .filter(Objects::nonNull)
                    .findAny()
                    .get();
        }

        return eventType;
    }

    /**
     * Find the type {@link Event Dubbo event} from the specified {@link ParameterizedType} presents
     * a class of {@link Listener Dubbo event listener}
     *
     * @param parameterizedType the {@link ParameterizedType} presents a class of {@link Listener Dubbo event listener}
     * @return <code>null</code> if not found
     */
    static Class<? extends Event> findEventType(ParameterizedType parameterizedType) {
        Class<? extends Event> eventType = null;

        Type rawType = parameterizedType.getRawType();
        if ((rawType instanceof Class) && Listener.class.isAssignableFrom((Class) rawType)) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            for (Type typeArgument : typeArguments) {
                if (typeArgument instanceof Class) {
                    Class argumentClass = (Class) typeArgument;
                    if (Event.class.isAssignableFrom(argumentClass)) {
                        eventType = argumentClass;
                        break;
                    }
                }
            }
        }

        return eventType;
    }
}