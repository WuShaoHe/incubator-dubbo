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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DefaultServiceDiscoveryChangeEventNotifier} Test
 *
 * @since 2.7.2
 */
public class DefaultServiceDiscoveryChangeEventNotifierTest {

    private DefaultServiceDiscoveryChangeEventNotifier notifier = new DefaultServiceDiscoveryChangeEventNotifier();

    private static class Listener implements ServiceDiscoveryChangeListener {

        private ServiceDiscoveryChangeEvent event;

        @Override
        public void onEvent(ServiceDiscoveryChangeEvent event) {
            this.event = event;
        }
    }

    @Test
    public void testListeners() {

        String serviceName = "A";

        List<Listener> listeners = asList(new Listener(), new Listener(), new Listener());

        for (ServiceDiscoveryChangeListener listener : listeners) {
            notifier.addListener(serviceName, listener);
        }

        assertEquals(listeners, notifier.getListeners(serviceName));

        Listener listener = new Listener();
        Listener listener2 = new Listener();

        notifier.addListener(serviceName, listener);
        notifier.addListener(serviceName, listener);
        notifier.addListener(serviceName, listener2);

        assertEquals(5, notifier.getListeners(serviceName).size());

        for (ServiceDiscoveryChangeListener l : listeners) {
            notifier.removeListener(serviceName, l);
        }

        assertEquals(2, notifier.getListeners(serviceName).size());

        notifier.removeAllListeners(serviceName);
        assertTrue(notifier.getListeners(serviceName).isEmpty());


        notifier.addListener(serviceName, listener);

        ServiceDiscoveryChangeEvent event = new ServiceDiscoveryChangeEvent(serviceName, Collections.emptyList());

        notifier.notify(event);

        assertEquals(listener.event, event);
    }
}
