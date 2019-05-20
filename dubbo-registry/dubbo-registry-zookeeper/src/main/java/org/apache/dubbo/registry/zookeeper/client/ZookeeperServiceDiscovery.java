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

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.event.EventDispatcher;
import org.apache.dubbo.common.function.ThrowableConsumer;
import org.apache.dubbo.common.function.ThrowableFunction;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.registry.client.EventPublishingServiceRegistry;
import org.apache.dubbo.registry.client.ServiceDiscovery;
import org.apache.dubbo.registry.client.ServiceInstance;
import org.apache.dubbo.registry.client.event.ServiceDiscoveryChangeListener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.KeeperException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.dubbo.common.function.ThrowableFunction.execute;
import static org.apache.dubbo.registry.zookeeper.client.util.CuratorFrameworkParams.ROOT_PATH;
import static org.apache.dubbo.registry.zookeeper.client.util.CuratorFrameworkUtils.build;
import static org.apache.dubbo.registry.zookeeper.client.util.CuratorFrameworkUtils.buildCuratorFramework;
import static org.apache.dubbo.registry.zookeeper.client.util.CuratorFrameworkUtils.buildServiceDiscovery;

/**
 * Zookeeper {@link ServiceDiscovery} implementation based on
 * <a href="https://curator.apache.org/curator-x-discovery/index.html">Apache Curator X Discovery</a>
 */
public class ZookeeperServiceDiscovery extends EventPublishingServiceRegistry implements ServiceDiscovery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CuratorFramework curatorFramework;

    private final String rootPath;

    private final org.apache.curator.x.discovery.ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

    private final EventDispatcher dispatcher;

    /**
     * The Key is watched Zookeeper path, the value is an instance of {@link CuratorWatcher}
     */
    private final Map<String, CuratorWatcher> watcherCaches = new ConcurrentHashMap<>();

    public ZookeeperServiceDiscovery(URL registerURL) throws Exception {
        this.curatorFramework = buildCuratorFramework(registerURL);
        this.rootPath = ROOT_PATH.getParameterValue(registerURL);
        this.serviceDiscovery = buildServiceDiscovery(curatorFramework, rootPath);
        this.dispatcher = EventDispatcher.getDefaultExtension();
    }

    @Override
    protected void doRegister(ServiceInstance serviceInstance) throws RuntimeException {
        doInServiceRegistry(serviceDiscovery -> {
            serviceDiscovery.registerService(build(serviceInstance));
        });
    }

    @Override
    protected void doUpdate(ServiceInstance serviceInstance) throws RuntimeException {
        doInServiceRegistry(serviceDiscovery -> {
            serviceDiscovery.updateService(build(serviceInstance));
        });
    }

    @Override
    protected void doUnregister(ServiceInstance serviceInstance) throws RuntimeException {
        doInServiceRegistry(serviceDiscovery -> {
            serviceDiscovery.unregisterService(build(serviceInstance));
        });
    }

    @Override
    protected void doStart() {
        doInServiceRegistry(serviceDiscovery -> {
            serviceDiscovery.start();
        });
    }

    @Override
    protected void doStop() {
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
    public void addServiceDiscoveryChangeListener(String serviceName, ServiceDiscoveryChangeListener listener)
            throws NullPointerException, IllegalArgumentException {
        addServiceWatcherIfAbsent(serviceName);
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

    private void addWatcherIfAbsent(String path, CuratorWatcher watcher) {
        if (!watcherCaches.containsKey(path)) {
            try {
                curatorFramework.getChildren().usingWatcher(watcher).forPath(path);
                watcherCaches.put(path, watcher);
            } catch (KeeperException.NoNodeException e) {
                // ignored
                if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage());
                }
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private void addServiceWatcherIfAbsent(String serviceName) {
        addWatcherIfAbsent(buildServicePath(serviceName),
                new ZookeeperServiceDiscoveryChangeWatcher(this, serviceName, dispatcher));
    }

    private String buildServicePath(String serviceName) {
        return rootPath + "/" + serviceName;
    }
}