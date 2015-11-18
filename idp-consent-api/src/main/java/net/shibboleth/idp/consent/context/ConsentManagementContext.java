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

package net.shibboleth.idp.consent.context;

import javax.annotation.Nonnull;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.base.MoreObjects;

/**
 * Context representing signals to consent flows for managing their state.
 * 
 * <p>This context acts as a public interface between other flows and flows that implement
 * consent-related functionality.</p>
 */
public class ConsentManagementContext extends BaseContext {

    /** Whether to revoke consent previously granted. */
    private boolean revokeConsent;
    
    /** Constructor. */
    public ConsentManagementContext() {
        revokeConsent = false;
    }
    
    /**
     * Get whether consent should be revoked.
     * 
     * @return  whether consent should be revoked
     */
    public boolean getRevokeConsent() {
        return revokeConsent;
    }
    
    /**
     * Set whether consent should be revoked.
     * 
     * @param flag flag to set
     * @return this context
     */
    @Nonnull public ConsentManagementContext setRevokeConsent(final boolean flag) {
        revokeConsent = flag;
        
        return this;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("revokeConsent", revokeConsent)
                .toString();
    }

}