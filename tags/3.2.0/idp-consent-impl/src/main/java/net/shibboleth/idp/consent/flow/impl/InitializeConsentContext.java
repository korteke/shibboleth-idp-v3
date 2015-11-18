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

package net.shibboleth.idp.consent.flow.impl;

import javax.annotation.Nonnull;

import net.shibboleth.idp.consent.context.impl.ConsentContext;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.interceptor.AbstractProfileInterceptorAction;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that creates a {@link ConsentContext} and attaches it to the current {@link ProfileRequestContext}.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @post See above.
 */
public class InitializeConsentContext extends AbstractProfileInterceptorAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(InitializeConsentContext.class);

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        final ConsentContext consentContext = new ConsentContext();

        log.debug("{} Created consent context '{}'", getLogPrefix(), consentContext);

        profileRequestContext.addSubcontext(consentContext, true);

        super.doExecute(profileRequestContext, interceptorContext);
    }
}
