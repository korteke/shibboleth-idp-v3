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

import org.opensaml.messaging.context.navigate.ContextDataLookupFunction;

import net.shibboleth.idp.profile.context.MultiRelyingPartyContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;

/** A function that returns {@link MultiRelyingPartyContext#getCurrentRelyingPartyContext()}. */
public class RelyingPartyContextLookupByCurrent
        implements ContextDataLookupFunction<MultiRelyingPartyContext,RelyingPartyContext> {

    /** {@inheritDoc} */
    @Override
    @Nullable public RelyingPartyContext apply(@Nullable final MultiRelyingPartyContext input) {
        if (input == null) {
            return null;
        }
        
        return input.getCurrentRelyingPartyContext();
    }
    
}