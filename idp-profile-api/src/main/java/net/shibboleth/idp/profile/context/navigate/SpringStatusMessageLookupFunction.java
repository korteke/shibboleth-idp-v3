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

package net.shibboleth.idp.profile.context.navigate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.context.SpringRequestContext;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.google.common.base.Function;

/**
 * A function that returns a status message to include, if any, in a SAML response based on the current
 * profile request context state, using Spring's {@link MessageSource} functionality.
 */
public class SpringStatusMessageLookupFunction implements Function<ProfileRequestContext, String>, MessageSourceAware {

    /** MessageSource injected by Spring, typically the parent ApplicationContext itself. */
    @Nonnull private MessageSource messageSource;
    
    /** {@inheritDoc} */
    @Override
    @Nullable public String apply(@Nullable final ProfileRequestContext input) {
        if (input != null && messageSource != null) {
            final SpringRequestContext springContext = input.getSubcontext(SpringRequestContext.class);
            if (springContext != null) {
                final RequestContext springRequestContext = springContext.getRequestContext();
                final Event previousEvent = springRequestContext != null
                        ? springRequestContext.getCurrentEvent() : null;
                if (previousEvent != null) {
                    try {
                        return messageSource.getMessage(previousEvent.getId(), null,
                                springRequestContext.getExternalContext().getLocale());
                    } catch (final NoSuchMessageException e) {
                        return null;
                    }
                }
            }
        }
        
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setMessageSource(MessageSource source) {
        messageSource = source;
    }

}