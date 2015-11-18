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

package net.shibboleth.idp.test.flows.exception;

import javax.annotation.Nonnull;

import net.shibboleth.idp.test.flows.AbstractFlowTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for the profile interceptor flow. */
@ContextConfiguration(locations = {"classpath:/exception/test-webflow-config.xml",})
public class ExceptionFlowTest extends AbstractFlowTest {

    @Nonnull public final static String TEST_EXCEPTION_FLOW_ID = "test-exception-flow";

    @Nonnull public final static String TEST_COMMITTED_FLOW_ID = "test-committed-flow";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ExceptionFlowTest.class);

    @Test public void testExceptionFlow() {

        final FlowExecutionResult result = flowExecutor.launchExecution(TEST_EXCEPTION_FLOW_ID, null, externalContext);

        assertFlowExecutionResult(result, TEST_EXCEPTION_FLOW_ID);
        Assert.assertEquals(result.getOutcome().getId(), "ErrorView");
    }

    @Test public void testExceptionAfterResponseFlow() {

        try {
            flowExecutor.launchExecution(TEST_COMMITTED_FLOW_ID, null, externalContext);
            Assert.fail("Flow should have thrown an exception.");
        } catch (final RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }

}