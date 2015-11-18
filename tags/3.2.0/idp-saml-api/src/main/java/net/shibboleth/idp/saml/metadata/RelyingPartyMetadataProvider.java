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

package net.shibboleth.idp.saml.metadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.ext.spring.service.AbstractServiceableComponent;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.joda.time.DateTime;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.RefreshableMetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * This class exists primarily to allow the parsing of relying-party.xml to create a serviceable implementation of
 * {@link MetadataResolver}.
 */
public class RelyingPartyMetadataProvider extends AbstractServiceableComponent<MetadataResolver> implements
        RefreshableMetadataResolver, Comparable<RelyingPartyMetadataProvider> {

    /** If we autogenerate a sort key it comes from this count. */
    private static int sortKeyValue;

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(RelyingPartyMetadataProvider.class);

    /** The embedded resolver. */
    @Nonnull private final MetadataResolver resolver;

    /** The key by which we sort the provider. */
    @NonnullAfterInit private Integer sortKey;

    /**
     * Constructor.
     * 
     * @param child The {@link MetadataResolver} to embed.
     */
    public RelyingPartyMetadataProvider(@Nonnull final MetadataResolver child) {
        resolver = Constraint.isNotNull(child, "MetadataResolver cannot be null");
    }

    /**
     * Set the sort key.
     * 
     * @param key what to set
     */
    public void setSortKey(int key) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        sortKey = new Integer(key);
    }

    /**
     * Return what we are build around. Used for testing.
     * 
     * @return the parameter we got as a constructor
     */
    @Nonnull public MetadataResolver getEmbeddedResolver() {
        return resolver;
    }

    /** {@inheritDoc} */
    @Override @Nonnull public Iterable<EntityDescriptor> resolve(@Nullable final CriteriaSet criteria)
            throws ResolverException {

        return resolver.resolve(criteria);
    }

    /** {@inheritDoc} */
    @Override @Nullable public EntityDescriptor resolveSingle(@Nullable final CriteriaSet criteria)
            throws ResolverException {

        return resolver.resolveSingle(criteria);
    }

    /** {@inheritDoc} */
    @Override public boolean isRequireValidMetadata() {
        return resolver.isRequireValidMetadata();
    }

    /** {@inheritDoc} */
    @Override public void setRequireValidMetadata(final boolean requireValidMetadata) {
        resolver.setRequireValidMetadata(requireValidMetadata);

    }

    /** {@inheritDoc} */
    @Override @Nullable public MetadataFilter getMetadataFilter() {
        return resolver.getMetadataFilter();
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        setId(resolver.getId());
        super.doInitialize();
        if (null == sortKey) {
            synchronized (this) {
                sortKeyValue++;
                sortKey = new Integer(sortKeyValue);
            }
            log.info("Top level Metadata Provider '{}' did not have a sort key; giving it value '{}'",
                    getId(), sortKey);
        }
    }

    /** {@inheritDoc} */
    @Override public void setMetadataFilter(@Nullable final MetadataFilter newFilter) {
        resolver.setMetadataFilter(newFilter);
    }

    /** {@inheritDoc} */
    @Override @Nonnull public MetadataResolver getComponent() {
        return this;
    }

    /** {@inheritDoc} */
    @Override public void refresh() throws ResolverException {
        if (resolver instanceof RefreshableMetadataResolver) {
            ((RefreshableMetadataResolver) resolver).refresh();
        }
    }

    /** {@inheritDoc} */
    @Override
    public DateTime getLastRefresh() {
        if (resolver instanceof RefreshableMetadataResolver) {
            return ((RefreshableMetadataResolver) resolver).getLastRefresh();
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public DateTime getLastUpdate() {
        if (resolver instanceof RefreshableMetadataResolver) {
            return ((RefreshableMetadataResolver) resolver).getLastUpdate();
        } else {
            return null;
        }
    }
    
    /** {@inheritDoc} */
    @Override public int compareTo(RelyingPartyMetadataProvider other) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        int result = sortKey.compareTo(other.sortKey);
        if (result != 0) {
            return result;
        }
        if (equals(other)) {
            return 0;
        }
        return getId().compareTo(other.getId());
    }

    /**
     * {@inheritDoc}. We are within a spring context and so equality can be determined by ID, however we also test by
     * sortKey just in case.
     */
    @Override public boolean equals(Object other) {
        if (null == other) {
            return false;
        }
        if (!(other instanceof RelyingPartyMetadataProvider)) {
            return false;
        }
        final RelyingPartyMetadataProvider otherRp = (RelyingPartyMetadataProvider) other;
        
        return Objects.equal(otherRp.sortKey, sortKey) && Objects.equal(getId(), otherRp.getId());
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return Objects.hashCode(sortKey, getId());
    }

}