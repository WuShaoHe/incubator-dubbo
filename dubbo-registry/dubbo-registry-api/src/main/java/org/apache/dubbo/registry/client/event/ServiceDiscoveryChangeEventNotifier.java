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

/**
 * The {@link ServiceDiscoveryChangeEvent} Notifier
 *
 * @since 2.7.2
 */
public interface ServiceDiscoveryChangeEventNotifier {

    /**
     * Adds a {@link ServiceDiscoveryChangeListener listener} to be notified
     *
     * @param serviceName the name of service
     * @param listener    {@link ServiceDiscoveryChangeListener listener}
     */
    void addListener(String serviceName, ServiceDiscoveryChangeListener listener);

    /**
     * Removes a {@link ServiceDiscoveryChangeListener listener} to be notified
     *
     * @param serviceName the name of service
     * @param listener    {@link ServiceDiscoveryChangeListener listener}
     */
    void removeListener(String serviceName, ServiceDiscoveryChangeListener listener);

    /**
     * Removes all {@link ServiceDiscoveryChangeListener listener} to be notified
     *
     * @param serviceName the name of service
     */
    void removeAllListeners(String serviceName);

    /**
     * Notifies the {@link ServiceDiscoveryChangeEvent The Service Discovery Change Event} to the associated listeners
     *
     * @param event the {@link ServiceDiscoveryChangeEvent The Service Discovery Change Event}
     */
    void notify(ServiceDiscoveryChangeEvent event);
}
