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

package net.shibboleth.idp.profile.context;

import javax.annotation.Nullable;

import org.opensaml.messaging.context.BaseContext;

import org.springframework.webflow.execution.RequestContext;

/**
 * A {@link BaseContext} which holds the Spring WebFlow {@link RequestContext} in which the
 * overall parent context is operating.
 * 
 * <p>Actions that make use of this context type are specific to Spring Web Flow and cannot
 * be orchestrated without that technology.</p>
 */
public class SpringRequestContext extends BaseContext {

    /** The request context represented. */
    @Nullable private RequestContext context;

    /**
     * Get the request context.
     * 
     * @return the event
     */
    @Nullable public RequestContext getRequestContext() {
        return context;
    }

    /**
     * Set the request context.
     * 
     * @param newContext the request context
     */
    public void setRequestContext(@Nullable final RequestContext newContext) {
        context = newContext;
    }

}