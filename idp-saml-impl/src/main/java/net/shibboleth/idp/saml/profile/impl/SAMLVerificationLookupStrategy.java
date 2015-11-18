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

package net.shibboleth.idp.saml.profile.impl;

import javax.annotation.Nullable;

import net.shibboleth.idp.profile.context.RelyingPartyContext;

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.messaging.context.navigate.ContextDataLookupFunction;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;

/**
 * A lookup strategy that returns true iff the {@link RelyingPartyContext} contains a reference
 * to a {@link SAMLPeerEntityContext} or {@link SAMLSelfEntityContext} that contains a
 * {@link SAMLMetadataContext} such that {@link SAMLMetadataContext#getEntityDescriptor()} is non-null.
 * 
 * <p>If no metadata exists, the context is treated as "unverified".</p>
 */
public class SAMLVerificationLookupStrategy implements ContextDataLookupFunction<RelyingPartyContext,Boolean> {

    /** {@inheritDoc} */
    @Override
    @Nullable public Boolean apply(@Nullable final RelyingPartyContext input) {
        
        final BaseContext ctx = input != null ? input.getRelyingPartyIdContextTree() : null;
        if (ctx != null) {
            if (ctx instanceof SAMLPeerEntityContext || ctx instanceof SAMLSelfEntityContext) {
                final SAMLMetadataContext mc = ctx.getSubcontext(SAMLMetadataContext.class);
                if (mc != null) {
                    return mc.getEntityDescriptor() != null;
                }
            }
        }
        
        return false;
    }

}