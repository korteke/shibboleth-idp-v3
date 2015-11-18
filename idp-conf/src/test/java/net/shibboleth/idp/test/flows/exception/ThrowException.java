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

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.SpringRequestContext;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletResponse;

public class ThrowException extends AbstractProfileAction {
    
    final boolean commitResponse;
    
    /**
     * Constructor.
     *
     * @param commit whether to lock the response
     */
    public ThrowException(final boolean commit) {
        commitResponse = commit;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (commitResponse) {
            final SpringRequestContext springContext = profileRequestContext.getSubcontext(SpringRequestContext.class);
            final MockHttpServletResponse response =
                    (MockHttpServletResponse) springContext.getRequestContext().getExternalContext().getNativeResponse();
            response.setOutputStreamAccessAllowed(false);
            response.setWriterAccessAllowed(false);
        }
        
        throw new NullPointerException("foo");
    }
    
}