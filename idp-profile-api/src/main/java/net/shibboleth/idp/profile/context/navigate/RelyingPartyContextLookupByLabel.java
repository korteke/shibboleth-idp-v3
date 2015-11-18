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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;

import net.shibboleth.idp.profile.context.MultiRelyingPartyContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * A function that returns a collection of {@link RelyingPartyContext}s based on a label.
 */
public class RelyingPartyContextLookupByLabel
        implements Function<MultiRelyingPartyContext, Collection<RelyingPartyContext>> {

    /** Label to use for auto-creation. */
    @Nullable private final String label; 
    
    /**
     * Constructor.
     * 
     * @param theLabel indicates context should be created if not present, using this label
     */
    public RelyingPartyContextLookupByLabel(@Nonnull @NotEmpty final String theLabel) {
        label = Constraint.isNotNull(StringSupport.trimOrNull(theLabel), "Label cannot be null or empty");
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public Collection<RelyingPartyContext> apply(@Nullable final MultiRelyingPartyContext input) {
        if (input == null) {
            return null;
        }
        
        return input.getRelyingPartyContexts(label);
    }
    
}