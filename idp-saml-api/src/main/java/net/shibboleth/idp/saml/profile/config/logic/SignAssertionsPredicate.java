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

package net.shibboleth.idp.saml.profile.config.logic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.logic.AbstractRelyingPartyPredicate;
import net.shibboleth.idp.saml.profile.config.SAMLProfileConfiguration;
import net.shibboleth.idp.saml.profile.context.navigate.SAMLMetadataContextLookupFunction;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import com.google.common.base.Function;

/** A predicate implementation that forwards to {@link SAMLProfileConfiguration#getSignAssertions()}. */
public class SignAssertionsPredicate extends AbstractRelyingPartyPredicate {

    /** Whether to override the result based on the WantAssertionsSigned flag in SAML metadata. */
    private boolean honorMetadata;
    
    /** Lookup strategy for {@link SAMLMetadataContext}. */
    private Function<ProfileRequestContext,SAMLMetadataContext> metadataContextLookupStrategy;
    
    /** Constructor. */
    public SignAssertionsPredicate() {
        honorMetadata = true;
        metadataContextLookupStrategy = new SAMLMetadataContextLookupFunction();
    }
    
    /**
     * Set whether to override the result based on the WantAssertionsSigned flag in SAML metadata.
     * 
     * @param flag flag to set
     */
    public void setHonorMetadata(final boolean flag) {
        honorMetadata = flag;
    }
    
    /**
     * Set lookup strategy for {@link SAMLMetadataContext}.
     * 
     * @param strategy lookup strategy
     */
    public void setMetadataContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,SAMLMetadataContext> strategy) {
        metadataContextLookupStrategy = Constraint.isNotNull(strategy,
                "SAMLMetadataContext lookup strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean apply(@Nullable final ProfileRequestContext input) {
        
        if (honorMetadata) {
            final SAMLMetadataContext metadataCtx = metadataContextLookupStrategy.apply(input);
            if (metadataCtx != null && metadataCtx.getRoleDescriptor() != null
                    && metadataCtx.getRoleDescriptor() instanceof SPSSODescriptor) {
                final Boolean flag = ((SPSSODescriptor) metadataCtx.getRoleDescriptor()).getWantAssertionsSigned();
                if (flag != null && flag.booleanValue()) {
                    return true;
                }
            }
        }
        
        final RelyingPartyContext rpc = getRelyingPartyContextLookupStrategy().apply(input);
        if (rpc != null) {
            final ProfileConfiguration pc = rpc.getProfileConfig();
            if (pc != null && pc instanceof SAMLProfileConfiguration) {
                return ((SAMLProfileConfiguration) pc).getSignAssertions().apply(input);
            }
        }

        return false;
    }

}