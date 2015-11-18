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

package net.shibboleth.idp.profile.support;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletRequest;

/**
 * Exposes the {@link org.opensaml.profile.context.ProfileRequestContext} in a HTTP session attribute to make it
 * accessible outside the Webflow execution pipeline. The PRC is stored in the session under the key
 * {@link ProfileRequestContext#BINDING_KEY}.
 *
 * @author Marvin S. Addison
 */
public class ProfileRequestContextFlowExecutionListener extends FlowExecutionListenerAdapter {

    /** Logger instance. */
    private final Logger log = LoggerFactory.getLogger(ProfileRequestContextFlowExecutionListener.class);


    @Override
    public void stateEntered(
            final RequestContext context, final StateDefinition previousState, final StateDefinition newState) {
        if (previousState != null && previousState.getId().startsWith("Initialize")) {
            final ProfileRequestContext prc = getProfileRequestContext(context);
            final ServletRequest request = getRequest(context);
            if (prc != null && request != null) {
                log.debug("Exposing ProfileRequestContext in servlet request");
                request.setAttribute(ProfileRequestContext.BINDING_KEY, prc);
            }
        }
    }

    @Override
    public void resuming(final RequestContext context) {
        final ProfileRequestContext prc = getProfileRequestContext(context);
        final ServletRequest request = getRequest(context);
        if (prc != null && request != null) {
            log.debug("Updating ProfileRequestContext in servlet request");
            request.setAttribute(ProfileRequestContext.BINDING_KEY, prc);
        }
    }

    /**
     * Get the profile request context bound to conversation scope.
     * 
     * @param context Spring request context
     * 
     * @return the bound profile request context, or null
     */
    @Nullable private ProfileRequestContext getProfileRequestContext(@Nonnull final RequestContext context) {
        final Object prc = context.getConversationScope().get(ProfileRequestContext.BINDING_KEY);
        if (prc instanceof ProfileRequestContext) {
            return (ProfileRequestContext) prc;
        }
        return null;
    }

    /**
     * Get the servlet request.
     * 
     * @param context Spring request context
     * 
     * @return servlet request, or null
     */
    @Nullable private ServletRequest getRequest(@Nonnull final RequestContext context) {
        final Object o = context.getExternalContext().getNativeRequest();
        if (o instanceof ServletRequest) {
            return (ServletRequest) o;
        }
        return null;
    }

}