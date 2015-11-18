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

package net.shibboleth.idp.profile.impl;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit test for {@link RecordResponseComplete}. */
public class RecordResponseCompleteTest {

    RecordResponseComplete action;

    @BeforeMethod public void setUp() throws ComponentInitializationException {
        action = new RecordResponseComplete();
        action.initialize();
    }

    @Test public void testRecordResponseComplete() throws Exception {

        final RequestContext requestContext = new RequestContextBuilder().buildRequestContext();

        Assert.assertFalse(requestContext.getExternalContext().isResponseComplete());

        final Event result = action.execute(requestContext);

        Assert.assertTrue(requestContext.getExternalContext().isResponseComplete());

        ActionTestingSupport.assertProceedEvent(result);
    }

    @Test public void testResponseAlreadyCompleted() throws Exception {

        final RequestContext requestContext = new RequestContextBuilder().buildRequestContext();

        requestContext.getExternalContext().recordResponseComplete();

        Assert.assertTrue(requestContext.getExternalContext().isResponseComplete());

        final Event result = action.execute(requestContext);

        Assert.assertTrue(requestContext.getExternalContext().isResponseComplete());

        ActionTestingSupport.assertProceedEvent(result);
    }
}