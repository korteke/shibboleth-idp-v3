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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.SpringRequestContext;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action that records the "Response Complete" status on the external context if not done so already.
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * 
 * @pre <pre>
 * ProfileRequestContext.getSubcontext(SpringRequestContext.class, false) != null
 * </pre>
 * @pre <pre>
 * SpringRequestContext.getRequestContext() != null
 * </pre>
 * @pre <pre>
 * RequestContext.getExternalContext() != null
 * </pre>
 */
public class RecordResponseComplete extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private Logger log = LoggerFactory.getLogger(RecordResponseComplete.class);

    /** ExternalContext to operate on. */
    @Nullable private ExternalContext externalContext;
    
    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        final SpringRequestContext springRequestContext =
                profileRequestContext.getSubcontext(SpringRequestContext.class);
        if (springRequestContext == null) {
            log.debug("{} Spring request context not found in profile request context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }

        final RequestContext requestContext = springRequestContext.getRequestContext();
        if (requestContext == null) {
            log.debug("{} Web Flow request context not found in Spring request context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        externalContext = requestContext.getExternalContext();
        if (externalContext == null) {
            log.debug("{} External context not found in Web Flow request context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }

        return super.doPreExecute(profileRequestContext);
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!externalContext.isResponseComplete()) {
            log.debug("{} Record response complete", getLogPrefix());
            externalContext.recordResponseComplete();
        }
    }
    
}