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
package org.apache.dubbo.registry.support;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.metadata.LocalMetadataService;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.configcenter.DynamicConfiguration;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.client.ServiceDiscovery;
import org.apache.dubbo.registry.client.ServiceRegistry;
import org.apache.dubbo.rpc.Protocol;
import org.apache.dubbo.rpc.ProxyFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.apache.dubbo.common.Constants.PROTOCOL_KEY;
import static org.apache.dubbo.common.Constants.PROVIDER_SIDE;
import static org.apache.dubbo.common.Constants.SIDE_KEY;
import static org.apache.dubbo.common.utils.StringUtils.split;

/**
 * Service-Oriented {@link Registry} that is dislike the traditional {@link Registry} will not communicate to
 * registry immediately instead of persisting into the metadata's repository when the Dubbo service exports.
 * The metadata repository will be used as the data source of Dubbo Metadata service that is about to export and be
 * subscribed by the consumers.
 * <p>
 * In subscription phase,
 *
 * @see ServiceRegistry
 * @see ServiceDiscovery
 * @see FailbackRegistry
 * @since 2.7.2
 */
public class ServiceOrientedRegistry extends FailbackRegistry {

    private static final String DUBBO_SERVICES_GROUP = "dubbo-services-mapping";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final DynamicConfiguration dynamicConfiguration = DynamicConfiguration.getDynamicConfiguration();

    private Protocol protocol;

    private ProxyFactory proxyFactory;

    private final ServiceRegistry serviceRegistry;

    private final ServiceDiscovery serviceDiscovery;

    private final LocalMetadataService localMetadataService;

    public ServiceOrientedRegistry(URL url, ServiceRegistry serviceRegistry, ServiceDiscovery serviceDiscovery,
                                   LocalMetadataService localMetadataService) {
        super(url);
        this.serviceRegistry = serviceRegistry;
        this.serviceDiscovery = serviceDiscovery;
        this.localMetadataService = localMetadataService;
    }

    protected boolean shouldRegister(URL url) {
        String side = url.getParameter(SIDE_KEY);

        boolean should = PROVIDER_SIDE.equals(side); // Only register the Provider.

        if (!should) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("The URL[%s] should not be registered.", url.toString()));
            }
        }

        return should;
    }

    @Override
    public void doRegister(URL url) {
        if (!shouldRegister(url)) {
            return;
        }
        if (localMetadataService.exportURL(url)) {
            if (logger.isInfoEnabled()) {
                logger.info(String.format("The URL[%s] registered successfully.", url.toString()));
            }
        } else {
            if (logger.isWarnEnabled()) {
                logger.info(String.format("The URL[%s] has been registered.", url.toString()));
            }
        }
    }

    @Override
    public void doUnregister(URL url) {
        if (!shouldRegister(url)) {
            return;
        }
        if (localMetadataService.unexportURL(url)) {
            if (logger.isInfoEnabled()) {
                logger.info(String.format("The URL[%s] deregistered successfully.", url.toString()));
            }
        } else {
            if (logger.isWarnEnabled()) {
                logger.info(String.format("The URL[%s] has been deregistered.", url.toString()));
            }
        }
    }

    @Override
    public void doSubscribe(URL url, NotifyListener listener) {

        if (isDubboMetadataServiceURL(url)) {
            subscribeDubboMetadataServiceURLs(url, listener);
        } else {  // for general Dubbo Services
            subscribeDubboServiceURLs(url, listener);
        }

        localMetadataService.subscribeURL(url);
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
        localMetadataService.unsubscribeURL(url);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }


    protected boolean isDubboMetadataServiceURL(URL url) {
        // TODO
        return true;
    }

    protected void subscribeDubboMetadataServiceURLs(URL url, NotifyListener listener) {
        String serviceInterface = url.getServiceInterface();
        String group = url.getGroup();
        String version = url.getVersion();
        String protocol = url.getParameter(PROTOCOL_KEY);
        List<String> urls = localMetadataService.getExportedURLs(serviceInterface, group, version, protocol);
        listener.notify(urls.stream().map(URL::valueOf).collect(Collectors.toList()));
    }

    protected void subscribeDubboServiceURLs(URL url, NotifyListener listener) {

        String serviceInterface = url.getServiceInterface();

        Set<String> serviceNames = findServiceNames(serviceInterface);

        serviceNames.stream()
                .map(serviceDiscovery::getInstances)
                .flatMap(Collection::stream)
                .forEach(serviceInstance -> {
                });

    }

    protected Set<String> findServiceNames(String serviceInterface) {
        String serviceNames = dynamicConfiguration.getConfig(serviceInterface, DUBBO_SERVICES_GROUP);
        if (StringUtils.isBlank(serviceNames)) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(asList(split(serviceNames, ',')));
    }
}
