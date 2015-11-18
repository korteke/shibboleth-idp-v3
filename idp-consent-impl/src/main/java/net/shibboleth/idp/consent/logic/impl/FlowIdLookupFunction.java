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

package net.shibboleth.idp.consent.logic.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.context.SpringRequestContext;

import org.opensaml.messaging.context.navigate.ContextDataLookupFunction;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.RequestContext;

/**
 * {@link ContextDataLookupFunction} that returns the current flow id.
 */
public class FlowIdLookupFunction implements ContextDataLookupFunction<ProfileRequestContext, String> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(FlowIdLookupFunction.class);

    /** {@inheritDoc} */
    @Override @Nullable public String apply(@Nullable final ProfileRequestContext input) {
        if (input == null) {
            return null;
        }

        final SpringRequestContext springRequestContext = input.getSubcontext(SpringRequestContext.class, false);
        if (springRequestContext == null) {
            return null;
        }

        final RequestContext requestContext = springRequestContext.getRequestContext();
        if (requestContext == null) {
            return null;
        }

        final FlowExecutionContext flowExecutionContext = requestContext.getFlowExecutionContext();
        if (flowExecutionContext == null) {
            return null;
        }

        if (!flowExecutionContext.isActive()) {
            return null;
        }

        final FlowDefinition flowDefinition = requestContext.getActiveFlow();

        final String flowId = flowDefinition.getId();
        log.debug("Current flow id is '{}'", flowId);
        return flowId;
    }

}
