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

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.dubbo.common.Constants.GROUP_KEY;
import static org.apache.dubbo.common.Constants.VERSION_KEY;
import static org.apache.dubbo.common.URL.valueOf;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link InMemoryMetadataService} Test
 *
 * @since 2.7.2
 */
public class InMemoryMetadataServiceTest {

    private InMemoryMetadataService inMemoryMetadataService = new InMemoryMetadataService("test");

    private static final String TEST_SERVICE = "org.apache.dubbo.test.TestService";

    private static final URL BASE_URL = valueOf("dubbo://127.0.0.1:20880/" + TEST_SERVICE);
    private static final URL REST_BASE_URL = valueOf("rest://127.0.0.1:20880/" + TEST_SERVICE);
    private static final URL BASE_URL_GROUP = BASE_URL.addParameter(GROUP_KEY, "test");
    private static final URL BASE_URL_GROUP_AND_VERSION = BASE_URL_GROUP.addParameter(VERSION_KEY, "1.0.0");

    @Test
    public void testServiceName() {
        assertEquals("test", inMemoryMetadataService.serviceName());
    }

    @Test
    public void testVersion() {
        assertEquals("1.0.0", DubboMetadataService.VERSION);
        assertEquals("1.0.0", inMemoryMetadataService.version());
    }

    @Test
    public void testGetExportedURLs() {

        assertTrue(inMemoryMetadataService.exportURL(BASE_URL));
        List<String> exportedURLs = inMemoryMetadataService.getExportedURLs(TEST_SERVICE);
        assertEquals(1, exportedURLs.size());
        assertEquals(asList(BASE_URL.toFullString()), exportedURLs);
        assertTrue(inMemoryMetadataService.unexportURL(BASE_URL));

        assertTrue(inMemoryMetadataService.exportURL(BASE_URL));
        assertFalse(inMemoryMetadataService.exportURL(BASE_URL));

        assertTrue(inMemoryMetadataService.exportURL(BASE_URL_GROUP));
        assertTrue(inMemoryMetadataService.exportURL(BASE_URL_GROUP_AND_VERSION));

        exportedURLs = inMemoryMetadataService.getExportedURLs(TEST_SERVICE);
        assertEquals(asList(BASE_URL.toFullString()), exportedURLs);
        assertEquals(asList(
                BASE_URL.toFullString(),
                BASE_URL_GROUP.toFullString(),
                BASE_URL_GROUP_AND_VERSION.toFullString()), inMemoryMetadataService.getExportedURLs());

        assertTrue(inMemoryMetadataService.exportURL(REST_BASE_URL));
        exportedURLs = inMemoryMetadataService.getExportedURLs(TEST_SERVICE);
        assertEquals(asList(BASE_URL.toFullString(), REST_BASE_URL.toFullString()), exportedURLs);
    }

    @Test
    public void testGetSubscribedURLs() {
        assertTrue(inMemoryMetadataService.subscribeServiceURL(BASE_URL));
        assertFalse(inMemoryMetadataService.subscribeServiceURL(BASE_URL));

        assertTrue(inMemoryMetadataService.subscribeServiceURL(BASE_URL_GROUP));
        assertTrue(inMemoryMetadataService.subscribeServiceURL(BASE_URL_GROUP_AND_VERSION));
        assertTrue(inMemoryMetadataService.subscribeServiceURL(REST_BASE_URL));

        List<String> subscribedURLs = inMemoryMetadataService.getSubscribedURLs();
        assertEquals(4, subscribedURLs.size());
        assertEquals(asList(
                BASE_URL.toFullString(),
                REST_BASE_URL.toFullString(),
                BASE_URL_GROUP.toFullString(),
                BASE_URL_GROUP_AND_VERSION.toFullString()), subscribedURLs);

        assertTrue(inMemoryMetadataService.unsubscribeURL(REST_BASE_URL));
        subscribedURLs = inMemoryMetadataService.getSubscribedURLs();
        assertEquals(3, subscribedURLs.size());
        assertEquals(asList(
                BASE_URL.toFullString(),
                BASE_URL_GROUP.toFullString(),
                BASE_URL_GROUP_AND_VERSION.toFullString()), subscribedURLs);

        assertTrue(inMemoryMetadataService.unsubscribeURL(BASE_URL_GROUP));
        subscribedURLs = inMemoryMetadataService.getSubscribedURLs();
        assertEquals(2, subscribedURLs.size());
        assertEquals(asList(
                BASE_URL.toFullString(),
                BASE_URL_GROUP_AND_VERSION.toFullString()), subscribedURLs);

        assertTrue(inMemoryMetadataService.unsubscribeURL(BASE_URL_GROUP_AND_VERSION));
        subscribedURLs = inMemoryMetadataService.getSubscribedURLs();
        assertEquals(1, subscribedURLs.size());
        assertEquals(asList(
                BASE_URL.toFullString()), subscribedURLs);
    }
}