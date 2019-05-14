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
package org.apache.dubbo.common.metadata;

import org.apache.dubbo.common.URL;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static org.apache.dubbo.common.URL.buildKey;

/**
 * The {@link DubboMetadataService} implementation stores the metadata of Dubbo services in memory when they exported
 *
 * @since 2.7.2
 */
public class InMemoryMetadataService implements DubboMetadataService {

    // =================================== Registration =================================== //

    /**
     * All exported {@link URL urls} {@link Map} whose key is the return value of {@link URL#getServiceKey()} method
     * and value is the {@link List} of String presenting the {@link URL URLs}
     */
    private ConcurrentMap<String, List<String>> exportedServiceURLs = new ConcurrentHashMap<>();

    // ==================================================================================== //

    // =================================== Subscription =================================== //

    /**
     * All subscribed service names
     */
    private Set<String> subscribedServices = new LinkedHashSet<>();

    /**
     * The subscribed {@link URL urls} {@link Map} of {@link DubboMetadataService},
     * whose key is the return value of {@link URL#getServiceKey()} method and value is the {@link List} of
     * String presenting the {@link URL URLs}
     */
    private final ConcurrentMap<String, List<String>> subscribedServiceURLs = new ConcurrentHashMap<>();

    // ==================================================================================== //

    /**
     * Current Dubbo service name
     */
    private final String serviceName;

    public InMemoryMetadataService(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String serviceName() {
        return serviceName;
    }

    @Override
    public List<String> getSubscribedURLs() {
        return getAllServiceURLs(subscribedServiceURLs);
    }

    @Override
    public List<String> getExportedURLs(String serviceInterface, String group, String version) {
        if (ALL_SERVICE_INTERFACES.equals(serviceInterface)) {
            return getAllServiceURLs(exportedServiceURLs);
        }
        String serviceKey = buildKey(serviceInterface, group, version);
        return unmodifiableList(getServiceURLs(exportedServiceURLs, serviceKey));
    }

    public boolean exportURL(URL url) {
        return addURL(exportedServiceURLs, url);
    }

    public boolean unexportURL(URL url) {
        return removeURL(exportedServiceURLs, url);
    }

    public boolean subscribeServiceURL(URL url) {
        return addURL(subscribedServiceURLs, url);
    }

    public boolean unsubscribeURL(URL url) {
        return removeURL(subscribedServiceURLs, url);
    }

    protected boolean addURL(Map<String, List<String>> serviceURLs, URL url) {
        String serviceKey = url.getServiceKey();
        List<String> urls = getServiceURLs(serviceURLs, serviceKey);
        String urlString = url.toFullString();
        if (!urls.contains(urlString)) {
            return urls.add(url.toFullString());
        }
        return false;
    }

    protected boolean removeURL(Map<String, List<String>> serviceURLs, URL url) {
        String serviceKey = url.getServiceKey();
        List<String> urls = getServiceURLs(serviceURLs, serviceKey);
        return urls.remove(url.toFullString());
    }

    protected List<String> getServiceURLs(Map<String, List<String>> serviceURLs, String serviceKey) {
        return serviceURLs.computeIfAbsent(serviceKey, s -> new LinkedList());
    }

    protected List<String> getAllServiceURLs(Map<String, List<String>> serviceURLs) {
        return serviceURLs
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
