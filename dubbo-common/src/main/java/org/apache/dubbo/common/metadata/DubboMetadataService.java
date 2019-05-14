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

import java.util.List;

/**
 * A framework interface of Dubbo Metadata Service defines the contract of Dubbo Services registartion and subscription
 * between Dubbo service providers and its consumers. The implementationwill be exported as a normal Dubbo service that
 * the clients would subscribe, whose version comes from the {@link #getVersion()} method and group gets from
 * {@link #currentServiceName()}, that means, The different Dubbo service(application) will export the different
 * {@link DubboMetadataService} that persists all the exported and subscribed metadata, they are present by
 * {@link #getExportedURLs()} and {@link #getSubscribedURLs()} respectively. What's more, {@link DubboMetadataService}
 * also providers the fine-grain methods for the precise queries.
 *
 * @see InMemoryMetadataService
 * @since 2.7.2
 */
public interface DubboMetadataService {

    /**
     * The value of all service names
     */
    String ALL_SERVICE_NAMES = "*";

    /**
     * The value of All service instances
     */
    String ALL_SERVICE_INTERFACE = "*";

    /**
     * The contract version of {@link DubboMetadataService}, the future update must make sure compatible.
     */
    String VERSION = "1.0.0";

    /**
     * Gets the current Dubbo Service name
     *
     * @return non-null
     */
    String currentServiceName();

    /**
     * The version of {@link DubboMetadataService} that always equals {@link #VERSION}
     *
     * @return non-null
     * @see #VERSION
     */
    default String getVersion() {
        return VERSION;
    }

    /**
     * the list of String that presents all Dubbo subscribed {@link URL urls}
     *
     * @return non-null read-only {@link List}
     */
    List<String> getSubscribedURLs();

    /**
     * Get the list of String that presents all Dubbo exported {@link URL urls}
     *
     * @return non-null read-only {@link List}
     */
    default List<String> getExportedURLs() {
        return getExportedURLs(ALL_SERVICE_INTERFACE);
    }

    /**
     * Get the list of String that presents the specified Dubbo exported {@link URL urls} by the <code>serviceInterface</code>
     *
     * @param serviceInterface The class name of Dubbo service interface
     * @return non-null read-only {@link List}
     * @see URL
     */
    default List<String> getExportedURLs(String serviceInterface) {
        return getExportedURLs(serviceInterface, null);
    }

    /**
     * Get the list of String that presents the specified Dubbo exported {@link URL urls} by the
     * <code>serviceInterface</code> and <code>group</code>
     *
     * @param serviceInterface The class name of Dubbo service interface
     * @param group            the Dubbo Service Group (optional)
     * @return non-null read-only {@link List}
     * @see URL
     */
    default List<String> getExportedURLs(String serviceInterface, String group) {
        return getExportedURLs(serviceInterface, group, null);
    }

    /**
     * * Get the list of String that presents the specified Dubbo exported {@link URL urls} by the
     * * <code>serviceInterface</code>, <code>group</code> and <code>version</code>
     *
     * @param serviceInterface The class name of Dubbo service interface
     * @param group            the Dubbo Service Group (optional)
     * @param version          the Dubbo Service Version (optional)
     * @return non-null read-only {@link List}
     * @see URL
     */
    List<String> getExportedURLs(String serviceInterface, String group, String version);
}
