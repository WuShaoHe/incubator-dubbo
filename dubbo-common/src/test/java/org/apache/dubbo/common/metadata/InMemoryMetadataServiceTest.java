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
import static org.apache.dubbo.common.URL.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link InMemoryMetadataService} Test
 *
 * @since 2.7.2
 */
public class InMemoryMetadataServiceTest {

    private InMemoryMetadataService inMemoryMetadataService = new InMemoryMetadataService("test");

    private final URL BASE_URL = valueOf("dubbo://127.0.0.1:20880/org.apache.dubbo.test.TestService?interface=org.apache.dubbo.test.TestService");

    @Test
    public void testCurrentServiceName() {
        assertEquals("test", inMemoryMetadataService.currentServiceName());
    }

    @Test
    public void testGetExportedURLs() {

        assertTrue(inMemoryMetadataService.exportURL(BASE_URL));
        List<String> exportedURLs = inMemoryMetadataService.getExportedURLs("org.apache.dubbo.test.TestService");
        assertEquals(1, exportedURLs.size());
        assertEquals(asList(BASE_URL.toFullString()), exportedURLs);
        assertTrue(inMemoryMetadataService.unexportURL(BASE_URL));
    }
}
