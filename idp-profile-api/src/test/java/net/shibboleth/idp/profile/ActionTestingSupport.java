/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.idp.profile;

import org.springframework.webflow.execution.Event;
import org.testng.Assert;

/**
 * Helper methods for creating/testing objects within profile action tests. When methods herein refer to mock objects
 * they are always objects that have been created via Mockito unless otherwise noted.
 */
public class ActionTestingSupport {

    /** ID of the inbound message. */
    public final static String INBOUND_MSG_ID = "inbound";

    /** Issuer of the inbound message. */
    public final static String INBOUND_MSG_ISSUER = "http://sp.example.org";

    /** ID of the outbound message. */
    public final static String OUTBOUND_MSG_ID = "outbound";

    /** Issuer of the outbound message. */
    public final static String OUTBOUND_MSG_ISSUER = "http://idp.example.org";

    /**
     * Checks that the event is not null, that the event source is not null, and that the event ID is the given id.
     * 
     * @param event the event to check
     */
    public static void assertEvent(final Event event, final String id) {
        Assert.assertNotNull(event);
        Assert.assertNotNull(event.getSource());
        Assert.assertEquals(event.getId(), id);
    }

    /**
     * Checks that the given event is a proceed event.
     * 
     * @param event the event to check
     */
    public static void assertProceedEvent(final Event event) {
        Assert.assertNull(event);
    }
    
}