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

package net.shibboleth.idp.consent.logic.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import com.google.common.base.Predicate;

/**
 * Predicate to determine whether consent should be obtained for an attribute.
 */
public class AttributePredicate extends AbstractInitializableComponent implements Predicate<IdPAttribute> {

    /** Whitelist of attribute IDs to allow. */
    @Nonnull @NonnullElements private Set<String> whitelistedAttributeIds;

    /** Blacklist of attribute IDs to deny. */
    @Nonnull @NonnullElements private Set<String> blacklistedAttributeIds;

    /** Regular expression to apply for acceptance testing. */
    @Nullable private Pattern matchExpression;

    /** Constructor. */
    public AttributePredicate() {
        whitelistedAttributeIds = Collections.emptySet();
        blacklistedAttributeIds = Collections.emptySet();
    }

    /**
     * Set the whitelisted attribute IDs.
     * 
     * @param whitelist whitelisted attribute IDs
     */
    public void setWhitelistedAttributeIds(@Nonnull @NonnullElements final Collection<String> whitelist) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        whitelistedAttributeIds = new HashSet<>(StringSupport.normalizeStringCollection(whitelist));
    }

    /**
     * Set the blacklisted attribute IDs.
     * 
     * @param blacklist blacklisted attribute IDs
     */
    public void setBlacklistedAttributeIds(@Nonnull @NonnullElements final Collection<String> blacklist) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        blacklistedAttributeIds = new HashSet<>(StringSupport.normalizeStringCollection(blacklist));
    }

    /**
     * Set an attribute ID matching expression to apply for acceptance.
     * 
     * @param expression an attribute ID matching expression
     */
    public void setAttributeIdMatchExpression(@Nullable final Pattern expression) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        matchExpression = expression;
    }

    /** {@inheritDoc} */
    @Override
    public boolean apply(@Nullable final IdPAttribute input) {

        final String attributeId = input.getId();

        if (!whitelistedAttributeIds.isEmpty() && !whitelistedAttributeIds.contains(attributeId)) {
            // Not in whitelist. Only accept if a regexp applies.
            if (matchExpression == null) {
                return false;
            } else {
                return matchExpression.matcher(attributeId).matches();
            }
        } else {
            // In whitelist (or none). Check blacklist, and if necessary a regexp.
            return !blacklistedAttributeIds.contains(attributeId)
                    && (matchExpression == null || matchExpression.matcher(attributeId).matches());
        }
    }
}
