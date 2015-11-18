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

import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.logic.AbstractRelyingPartyPredicate;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.OutboundMessageContextLookup;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * A predicate that evaluates a SSO {@link ProfileRequestContext} and determines whether an attribute statement
 * should be included in the outgoing assertion.
 * 
 * <p>The "includeAttributeStatement" profile configuration flag is the main setting governing this decision,
 * but is overridden to "true" in the case that the outgoing {@link SAMLBindingContext} indicates the outbound
 * binding is an artifact mechanism.</p> 
 */
public class IncludeAttributeStatementPredicate extends AbstractRelyingPartyPredicate {
    
    /** Strategy function for access to {@link SAMLBindingContext} to populate. */
    @Nonnull private Function<ProfileRequestContext,SAMLBindingContext> bindingContextLookupStrategy;
    
    /** Constructor. */
    public IncludeAttributeStatementPredicate() {
        bindingContextLookupStrategy = Functions.compose(new ChildContextLookup<>(SAMLBindingContext.class),
                new OutboundMessageContextLookup());
    }

    /**
     * Set lookup strategy for {@link SAMLBindingContext} to examine.
     * 
     * @param strategy  lookup strategy
     */
    public void setBindingContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,SAMLBindingContext> strategy) {
        bindingContextLookupStrategy = Constraint.isNotNull(strategy,
                "SAMLBindingContext lookup strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean apply(@Nullable final ProfileRequestContext input) {
        
        // Check for an artifact binding.
        final SAMLBindingContext bindingCtx = bindingContextLookupStrategy.apply(input);
        if (bindingCtx != null && bindingCtx.getBindingDescriptor() != null
                && bindingCtx.getBindingDescriptor().isArtifact()) {
            return true;
        }
        
        final RelyingPartyContext rpc = getRelyingPartyContextLookupStrategy().apply(input);
        if (rpc != null && rpc.getProfileConfig() != null) {
            if (rpc.getProfileConfig()
                    instanceof net.shibboleth.idp.saml.saml1.profile.config.BrowserSSOProfileConfiguration) {
                return ((net.shibboleth.idp.saml.saml1.profile.config.BrowserSSOProfileConfiguration)
                        rpc.getProfileConfig()).includeAttributeStatement();
            } else if (rpc.getProfileConfig()
                    instanceof net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration) {
                return ((net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration)
                        rpc.getProfileConfig()).includeAttributeStatement();
            } else if (rpc.getProfileConfig()
                    instanceof net.shibboleth.idp.saml.saml1.profile.config.AttributeQueryProfileConfiguration) {
                return true;
            } else if (rpc.getProfileConfig()
                    instanceof net.shibboleth.idp.saml.saml2.profile.config.AttributeQueryProfileConfiguration) {
                return true;
            }
        }
        
        return false;
    }

}