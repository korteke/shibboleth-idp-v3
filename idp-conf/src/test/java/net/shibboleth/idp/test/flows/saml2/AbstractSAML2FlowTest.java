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

package net.shibboleth.idp.test.flows.saml2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.test.flows.AbstractFlowTest;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.testng.Assert;

/**
 * Abstract SAML 2 flow test.
 */
public class AbstractSAML2FlowTest extends AbstractFlowTest {

    /**
     * Validate the {@link FlowExecutionResult} using a default {@link SAML2TestResponseValidator}.
     * 
     * @param result the flow execution result
     * @param flowId the flow ID
     */
    public void validateResult(@Nullable final FlowExecutionResult result, @Nonnull final String flowId) {
        final SAML2TestResponseValidator validator = new SAML2TestResponseValidator();
        validator.spCredential = spCredential;
        validateResult(result, flowId, validator);
    }

    /**
     * Validate the {@link FlowExecutionResult} using the given {@link SAML2TestResponseValidator}.
     * 
     * @param result the flow execution result
     * @param flowId the flow ID
     * @param validator the response validator
     */
    public void validateResult(@Nullable final FlowExecutionResult result, @Nonnull final String flowId,
            @Nonnull final SAML2TestResponseValidator validator) {
        assertFlowExecutionResult(result, flowId);
        validator.validateResponse(getResponse(result));
    }

    /**
     * Get the SAML response from the flow result
     * 
     * @param result the flow result
     * @return the SAML response
     */
    public Response getResponse(@Nullable final FlowExecutionResult result) {
        final FlowExecutionOutcome outcome = result.getOutcome();
        assertFlowExecutionOutcome(outcome);

        final ProfileRequestContext prc = (ProfileRequestContext) outcome.getOutput().get(END_STATE_OUTPUT_ATTR_NAME);
        assertProfileRequestContext(prc);
        Assert.assertTrue(prc.getOutboundMessageContext().getMessage() instanceof Response);

        return (Response) prc.getOutboundMessageContext().getMessage();
    }
}
