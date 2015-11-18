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

package net.shibboleth.idp.saml.profile.context.navigate;

import javax.annotation.Nullable;

import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.AbstractRelyingPartyLookupFunction;

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;

/**
 * A function to access a {@link SAMLMetadataContext} underlying a {@link RelyingPartyContext} located via a
 * lookup function, by default a child of the profile request context.
 */
public class SAMLMetadataContextLookupFunction extends AbstractRelyingPartyLookupFunction<SAMLMetadataContext> {

    /** {@inheritDoc} */
    @Override
    @Nullable public SAMLMetadataContext apply(@Nullable final ProfileRequestContext input) {
        final RelyingPartyContext rpCtx = getRelyingPartyContextLookupStrategy().apply(input);
        if (rpCtx != null) {
            final BaseContext peer = rpCtx.getRelyingPartyIdContextTree();
            if (peer != null) {
                if (peer instanceof SAMLMetadataContext) {
                    return (SAMLMetadataContext) peer;
                } else {
                    return peer.getSubcontext(SAMLMetadataContext.class);
                }    
            }
        }
        
        return null;
    }
    
}