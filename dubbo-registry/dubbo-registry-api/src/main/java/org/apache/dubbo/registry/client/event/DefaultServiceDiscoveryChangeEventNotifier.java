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
package org.apache.dubbo.registry.client.event;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * The default {@link ServiceDiscoveryChangeEventNotifier}
 *
 * @since 2.7.2
 */
public class DefaultServiceDiscoveryChangeEventNotifier implements ServiceDiscoveryChangeEventNotifier {

    private ConcurrentMap<String, List<ServiceDiscoveryChangeListener>> serviceListeners = new ConcurrentHashMap<>();

    @Override
    public void addListener(String serviceName, ServiceDiscoveryChangeListener listener) {
        executeListeners(serviceName, listeners -> {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        });
    }

    @Override
    public void removeListener(String serviceName, ServiceDiscoveryChangeListener listener) {
        executeListeners(serviceName, listeners -> {
            listeners.remove(listener);
        });
    }

    @Override
    public void removeAllListeners(String serviceName) {
        executeListeners(serviceName, listeners -> {
            listeners.clear();
        });
    }

    @Override
    public void notify(ServiceDiscoveryChangeEvent event) {
        executeListeners(event.getServiceName(), listeners -> {
            listeners.forEach(listener -> {
                listener.onEvent(event);
            });
        });
    }

    protected List<ServiceDiscoveryChangeListener> getListeners(String serviceName) {
        return serviceListeners.computeIfAbsent(serviceName, s -> new LinkedList<>());
    }

    private void executeListeners(String serviceName, Consumer<List<ServiceDiscoveryChangeListener>> listeners) {
        listeners.accept(getListeners(serviceName));
    }
}
