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

package net.shibboleth.idp.saml.nameid;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.shibboleth.idp.authn.SubjectCanonicalizationFlowDescriptor;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

/**
 * A class used to describe flow descriptors for {@link net.shibboleth.idp.saml.authn.principal.NameIDPrincipal} and
 * {@link net.shibboleth.idp.saml.authn.principal.NameIdentifierPrincipal}. This adds the concept of formats to the base
 * class.
 */
public class NameIDCanonicalizationFlowDescriptor extends SubjectCanonicalizationFlowDescriptor {

    /** Store Set of acceptable formats. */
    @NonnullAfterInit @Unmodifiable private Set<String> formats;

    /**
     * Return the set of acceptable formats.
     * 
     * @return Returns the formats. Never empty after initialization.
     */
    @Nonnull public Collection<String> getFormats() {
        return formats;
    }

    /**
     * Sets the acceptable formats.
     * 
     * @param theFormats The formats to set.
     */
    public void setFormats(@Nonnull Collection<String> theFormats) {
        Constraint.isNotNull(theFormats, "Format list must be non null");
        Constraint.isNotEmpty(theFormats, "Format list must be non empty");
        final Set<String> newFormats = new HashSet(theFormats.size());
        CollectionSupport.addIf(newFormats, theFormats, Predicates.notNull());

        formats = ImmutableSet.copyOf(newFormats);
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (null == formats || formats.isEmpty()) {
            throw new ComponentInitializationException("NameIDFlow Descriptor " + getId()
                    + " Should specify one or more formats");
        }
    }
    
}