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

package net.shibboleth.idp.session.impl;

import com.google.common.base.Function;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.ActionSupport;
import net.shibboleth.idp.session.context.LogoutContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;

/**
 * Stores the {@link LogoutContext} in the servlet session to facilitate lookup by logout propagation flows.
 *
 * @author Marvin S. Addison
 */
public class SaveLogoutContext extends AbstractProfileAction {

    /** Key name under which LogoutContext is stored in Session. */
    @Nonnull @NotEmpty public static final String LOGOUT_CONTEXT_KEY = "net.shibboleth.idp.session.impl.LogoutContext";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SaveLogoutContext.class);

    /** Looks up a LogoutContext from PRC. */
    @Nonnull private Function<ProfileRequestContext, LogoutContext> logoutContextLookup;
            
    /** Constructor. */
    public SaveLogoutContext() {
        logoutContextLookup = new ChildContextLookup<>(LogoutContext.class);
    }
    
    /**
     * Set the lookup strategy for the {@link LogoutContext}.
     * 
     * @param strategy lookup strategy
     */
    public void setLogoutContextLookupStrategy(@Nonnull final Function<ProfileRequestContext,LogoutContext> strategy) {
        logoutContextLookup = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull protected Event doExecute(@Nonnull final RequestContext springRequestContext,
            @Nonnull final ProfileRequestContext profileRequestContext) {

        final LogoutContext logoutContext = logoutContextLookup.apply(profileRequestContext);
        if (logoutContext == null) {
            log.debug("{} LogoutContext not found in ProfileRequestContext", getLogPrefix());
            return ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
        }
        log.debug("{} Saving LogoutContext in HTTP session", getLogPrefix());
        springRequestContext.getExternalContext().getSessionMap().put(LOGOUT_CONTEXT_KEY, logoutContext);
        return ActionSupport.buildProceedEvent(profileRequestContext);
    }
    
}