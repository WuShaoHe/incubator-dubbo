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
package org.apache.dubbo.common.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DirectEventDispatcher} Test
 *
 * @since 2.7.2
 */
public class DirectEventDispatcherTest {

    private DirectEventDispatcher dispatcher;

    private EchoEventListener echoEventListener;

    private EchoEventListener2 echoEventListener2;

    @BeforeEach
    public void init() {
        dispatcher = new DirectEventDispatcher();
        echoEventListener = new EchoEventListener();
        echoEventListener2 = new EchoEventListener2();
    }

    @AfterEach
    public void destroy() {
        dispatcher.removeAllListeners();
    }

    @Test
    public void testGetExecutor() {
        assertNotNull(dispatcher.getExecutor());
    }

    @Test
    public void testGetAllListeners() {
        assertTrue(dispatcher.getAllListeners().isEmpty());
    }

    @Test
    public void testSingleListener() {
        // add two listeners
        dispatcher.addListener(echoEventListener);
        dispatcher.addListener(echoEventListener2);
        assertEquals(asList(echoEventListener2, echoEventListener), dispatcher.getAllListeners());

        // add a duplicated listener
        dispatcher.addListener(echoEventListener);
        assertEquals(asList(echoEventListener2, echoEventListener), dispatcher.getAllListeners());

        // remove
        dispatcher.removeListener(echoEventListener);
        assertEquals(asList(echoEventListener2), dispatcher.getAllListeners());

        dispatcher.removeListener(echoEventListener2);
        assertEquals(emptyList(), dispatcher.getAllListeners());
    }

    @Test
    public void testMultipleListeners() {

        // add two listeners
        dispatcher.addListeners(echoEventListener, echoEventListener2);
        assertEquals(asList(echoEventListener2, echoEventListener), dispatcher.getAllListeners());

        // remove all listeners
        dispatcher.removeAllListeners();
        assertEquals(emptyList(), dispatcher.getAllListeners());

        // add the duplicated listeners
        dispatcher.addListeners(echoEventListener, echoEventListener, echoEventListener2);
        assertEquals(asList(echoEventListener2, echoEventListener), dispatcher.getAllListeners());

        // remove all listeners
        dispatcher.removeAllListeners();
        assertEquals(emptyList(), dispatcher.getAllListeners());

        dispatcher.addListeners(asList(echoEventListener, echoEventListener, echoEventListener2));
        assertEquals(asList(echoEventListener2, echoEventListener), dispatcher.getAllListeners());

        dispatcher.removeListeners(asList(echoEventListener, echoEventListener, echoEventListener2));
        assertEquals(emptyList(), dispatcher.getAllListeners());
    }

    @Test
    public void testDispatchEvent() {

        dispatcher.addListener(echoEventListener);

        // dispatch a Event
        dispatcher.dispatch(new Event("Test") {
        });

        // no-op occurs
        assertEquals(0, echoEventListener.getEventOccurs());

        // dispatch a EchoEvent
        dispatcher.dispatch(new EchoEvent("Hello,World"));

        // event has been handled
        assertEquals(1, echoEventListener.getEventOccurs());

        dispatcher.addListener(echoEventListener2);

        // reset the listeners
        init();
        dispatcher.addListeners(echoEventListener, echoEventListener2);

        // dispatch a Event
        dispatcher.dispatch(new Event("Test") {
        });

        // echoEventListener will be not triggered + 0
        // echoEventListener2 will be triggered    + 1
        assertEquals(0, echoEventListener.getEventOccurs());
        assertEquals(1, echoEventListener2.getEventOccurs());

        // dispatch a EchoEvent
        // echoEventListener and echoEventListener2 are triggered both (+1)
        dispatcher.dispatch(new EchoEvent("Hello,World"));
        assertEquals(1, echoEventListener.getEventOccurs());
        assertEquals(2, echoEventListener2.getEventOccurs());

        // both +1
        dispatcher.dispatch(new EchoEvent("2019"));
        assertEquals(2, echoEventListener.getEventOccurs());
        assertEquals(3, echoEventListener2.getEventOccurs());
    }
}
