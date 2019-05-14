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
package org.apache.dubbo.registry.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DefaultServiceInstance} Test
 *
 * @since 2.7.2
 */
public class DefaultServiceInstanceTest {

    private final DefaultServiceInstance serviceInstance =
            new DefaultServiceInstance("A", "127.0.0.1", 8080);

    @Test
    public void testDefaultValues() {
        assertTrue(serviceInstance.isEnabled());
        assertTrue(serviceInstance.isHealthy());
        assertTrue(serviceInstance.getMetadata().isEmpty());
    }

    @Test
    public void testSetAndGetValues() {
        serviceInstance.setEnabled(false);
        serviceInstance.setHealthy(false);

        assertEquals("A", serviceInstance.getServiceName());
        assertEquals("127.0.0.1", serviceInstance.getHost());
        assertEquals(8080, serviceInstance.getPort());
        assertFalse(serviceInstance.isEnabled());
        assertFalse(serviceInstance.isHealthy());
        assertTrue(serviceInstance.getMetadata().isEmpty());
    }
}