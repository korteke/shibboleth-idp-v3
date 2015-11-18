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

import javax.annotation.Nullable;

import net.shibboleth.idp.profile.context.RelyingPartyContext;

import org.opensaml.profile.context.ProfileRequestContext;

/**
 * A function that returns {@link RelyingPartyContext#getRelyingPartyId()} from a
 * {@link RelyingPartyContext} obtained via a lookup function, by default a child of the {@link ProfileRequestContext}.
 * 
 * <p>If a specific setting is unavailable, a null value is returned.</p>
 */
public class RelyingPartyIdLookupFunction extends AbstractRelyingPartyLookupFunction<String> {

    /** {@inheritDoc} */
    @Override
    @Nullable public String apply(@Nullable final ProfileRequestContext input) {
        if (input != null) {
            final RelyingPartyContext rpc = getRelyingPartyContextLookupStrategy().apply(input);
            if (rpc != null) {
                return rpc.getRelyingPartyId();
            }
        }
        
        return null;
    }

}