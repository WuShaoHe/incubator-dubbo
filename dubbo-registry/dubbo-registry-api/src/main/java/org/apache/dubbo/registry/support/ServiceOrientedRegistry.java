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
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.client.ServiceDiscovery;
import org.apache.dubbo.registry.client.ServiceRegistry;

import java.util.Collections;
import java.util.List;

import static org.apache.dubbo.common.Constants.GROUP_KEY;
import static org.apache.dubbo.common.Constants.PROTOCOL_KEY;
import static org.apache.dubbo.common.Constants.PROVIDER_SIDE;
import static org.apache.dubbo.common.Constants.SIDE_KEY;
import static org.apache.dubbo.common.Constants.VERSION_KEY;

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

    private final ServiceRegistry serviceRegistry;

    private final ServiceDiscovery serviceDiscovery;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public ServiceOrientedRegistry(URL url, ServiceRegistry serviceRegistry, ServiceDiscovery serviceDiscovery) {
        super(url);
        this.serviceRegistry = serviceRegistry;
        this.serviceDiscovery = serviceDiscovery;
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
    }

    @Override
    public void doUnregister(URL url) {
        if (!shouldRegister(url)) {
            return;
        }
    }

    @Override
    public void doSubscribe(URL url, NotifyListener listener) {

        if (isDubboMetadataServiceURL(url)) {
            subscribeDubboMetadataServiceURLs(url, listener);
        }

    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {

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
        String group = url.getParameter(GROUP_KEY);
        String version = url.getParameter(VERSION_KEY);
        String protocol = url.getParameter(PROTOCOL_KEY);
        List<URL> urls = Collections.emptyList();
//                repository.findSubscribedDubboMetadataServiceURLs(serviceInterface, group, version, protocol);
        listener.notify(urls);
    }

}
