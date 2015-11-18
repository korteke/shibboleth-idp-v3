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

package net.shibboleth.idp.attribute.resolver;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Predicate;

/**
 * A proxy which wraps a resolved attribute definition and always returns the same attribute. The goal being that once
 * an attribute definition is resolved once this can be used in its place and calls to
 * {@link AttributeDefinition#resolve(AttributeResolutionContext)} are "free".
 * 
 * This proxy is immutable so all setter methods simply return.
 */
@ThreadSafe
public final class ResolvedAttributeDefinition extends AbstractAttributeDefinition {

    /** The attribute definition that was resolved to produce the attribute. */
    @Nonnull private final AttributeDefinition resolvedDefinition;

    /** The attribute produced by the resolved attribute definition. */
    @Nullable private final IdPAttribute resolvedAttribute;

    /**
     * Constructor.
     * 
     * @param definition attribute definition that was resolved to produce the given attribute
     * @param attribute attribute produced by the given attribute definition
     */
    public ResolvedAttributeDefinition(@Nonnull final AttributeDefinition definition,
            @Nullable final IdPAttribute attribute) {
        resolvedDefinition = Constraint.isNotNull(definition, "Resolved attribute definition can not be null");
        Constraint.isTrue(definition.isInitialized(), "Resolved definition must have been initialized");
        Constraint.isFalse(definition.isDestroyed(), "Resolved definition can not have been destroyed");
        resolvedAttribute = attribute;
    }

    /** {@inheritDoc} */
    @Override @Nullable protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
        return resolvedAttribute;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object obj) {
        return resolvedDefinition.equals(obj);
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements public Set<AttributeEncoder<?>> getAttributeEncoders() {
        return resolvedDefinition.getAttributeEncoders();
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements public Set<ResolverPluginDependency> getDependencies() {
        return resolvedDefinition.getDependencies();
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements public Map<Locale, String> getDisplayDescriptions() {
        return resolvedDefinition.getDisplayDescriptions();
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements public Map<Locale, String> getDisplayNames() {
        return resolvedDefinition.getDisplayNames();
    }

    /** {@inheritDoc} */
    @Override @Nullable public Predicate<ProfileRequestContext> getActivationCondition() {
        return null;
    }

    /** {@inheritDoc} */
    @Override @Nonnull public String getId() {
        return resolvedDefinition.getId();
    }

    /**
     * Gets the resolved attribute.
     * 
     * @return resolved attribute, or null
     */
    @Nullable public IdPAttribute getResolvedAttribute() {
        return resolvedAttribute;
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return resolvedDefinition.hashCode();
    }

    /** {@inheritDoc} */
    @Override public boolean isDependencyOnly() {
        return resolvedDefinition.isDependencyOnly();
    }

    /** {@inheritDoc} */
    @Override public boolean isPropagateResolutionExceptions() {
        return resolvedDefinition.isPropagateResolutionExceptions();
    }

    /** {@inheritDoc} */
    @Override public void setDependencyOnly(boolean isDependencyOnly) {
        return;
    }

    /** {@inheritDoc} */
    @Override public void setDisplayDescriptions(Map<Locale, String> descriptions) {
        return;
    }

    /** {@inheritDoc} */
    @Override public void setDisplayNames(Map<Locale, String> names) {
        return;
    }

    /** {@inheritDoc} */
    @Override public void setPropagateResolutionExceptions(boolean propagate) {
        return;
    }

    /** {@inheritDoc} */
    @Override @Nonnull public String toString() {
        return resolvedDefinition.toString();
    }

    /**
     * Gets the wrapped attribute definition that was resolved.
     * 
     * @return the resolved attribute definition
     */
    @Nonnull public AttributeDefinition getResolvedDefinition() {
        return resolvedDefinition;
    }

    /** {@inheritDoc} */
    @Override public boolean isInitialized() {
        return true;
    }

}