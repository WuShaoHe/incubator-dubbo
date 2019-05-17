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
package org.apache.dubbo.registry.zookeeper.client;

import org.apache.dubbo.common.event.EventDispatcher;
import org.apache.dubbo.common.function.ThrowableConsumer;
import org.apache.dubbo.common.function.ThrowableFunction;
import org.apache.dubbo.registry.client.ServiceDiscovery;
import org.apache.dubbo.registry.client.ServiceInstance;
import org.apache.dubbo.registry.client.ServiceRegistry;
import org.apache.dubbo.registry.client.event.ServiceDiscoveryChangeListener;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.apache.dubbo.common.event.EventDispatcher.getDefaultExtension;
import static org.apache.dubbo.common.function.ThrowableFunction.execute;
import static org.apache.dubbo.registry.zookeeper.client.util.CuratorFrameworkUtils.build;

/**
 * Zookeeper {@link ServiceDiscovery} implementation based on
 * <a href="https://curator.apache.org/curator-x-discovery/index.html">Apache Curator X Discovery</a>
 */
public class ZookeeperServiceDiscovery implements ServiceRegistry, ServiceDiscovery {

    private final org.apache.curator.x.discovery.ServiceDiscovery serviceDiscovery;

    private final EventDispatcher dispatcher = getDefaultExtension();

    public ZookeeperServiceDiscovery(org.apache.curator.x.discovery.ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public void register(ServiceInstance serviceInstance) {
        doInServiceRegistry(serviceDiscovery -> {
            serviceDiscovery.registerService(build(serviceInstance));
        });
    }

    @Override
    public void update(ServiceInstance serviceInstance) {
        doInServiceRegistry(serviceDiscovery -> {
            serviceDiscovery.updateService(build(serviceInstance));
        });
    }

    @Override
    public void unregister(ServiceInstance serviceInstance) {
        doInServiceRegistry(serviceDiscovery -> {
            serviceDiscovery.unregisterService(build(serviceInstance));
        });
    }

    @Override
    public void start() {
        doInServiceRegistry(serviceDiscovery -> {
            serviceDiscovery.start();
        });
    }

    @Override
    public void stop() {
        doInServiceRegistry(serviceDiscovery -> {
            serviceDiscovery.close();
        });
    }

    @Override
    public Set<String> getServices() {
        return doInServiceDiscovery(s -> new LinkedHashSet<>(s.queryForNames()));
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceName) throws NullPointerException {
        return doInServiceDiscovery(s -> build(s.queryForInstances(serviceName)));
    }

    @Override
    public void registerListener(ServiceDiscoveryChangeListener listener) {
        dispatcher.addEventListener(listener);
    }

    private void doInServiceRegistry(ThrowableConsumer<org.apache.curator.x.discovery.ServiceDiscovery> consumer) {
        ThrowableConsumer.execute(serviceDiscovery, s -> {
            consumer.accept(s);
        });
    }

    private <R> R doInServiceDiscovery(ThrowableFunction<org.apache.curator.x.discovery.ServiceDiscovery, R> function) {
        return execute(serviceDiscovery, function);
    }
}
