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

package net.shibboleth.idp.profile.interceptor.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.interceptor.AbstractProfileInterceptorAction;
import net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A profile interceptor action that filters out available interceptor flows if the request requires non-browser support
 * and the flows require a browser.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @pre <pre>ProfileRequestContext.getSubcontext(ProfileInterceptorContext.class) != null</pre>
 * @post ProfileInterceptorContext.getAvailableFlows() is modified as above.
 */
public class FilterFlowsByNonBrowserSupport extends AbstractProfileInterceptorAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(FilterFlowsByNonBrowserSupport.class);

    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        if (profileRequestContext.isBrowserProfile()) {
            log.debug("{} Request does not have non-browser requirement, nothing to do", getLogPrefix());
            return false;
        }

        return super.doPreExecute(profileRequestContext, interceptorContext);
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        final Map<String, ProfileInterceptorFlowDescriptor> availableFlows = interceptorContext.getAvailableFlows();

        final Iterator<Entry<String, ProfileInterceptorFlowDescriptor>> descriptorIterator =
                availableFlows.entrySet().iterator();
        while (descriptorIterator.hasNext()) {
            final ProfileInterceptorFlowDescriptor descriptor = descriptorIterator.next().getValue();
            if (descriptor.isNonBrowserSupported()) {
                log.debug("{} Retaining flow '{}', it supports non-browser authentication", getLogPrefix(),
                        descriptor.getId());
            } else {
                log.debug("{} Removing flow '{}', it does not support non-browser authentication", getLogPrefix(),
                        descriptor.getId());
                descriptorIterator.remove();
            }
        }

        if (availableFlows.size() == 0) {
            log.info("{} No available interceptor flows remain after filtering", getLogPrefix());
        } else {
            log.debug("{} Available interceptor flows after filtering: '{}'", getLogPrefix(), availableFlows);
        }
    }

}