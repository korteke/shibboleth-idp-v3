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

package net.shibboleth.idp.attribute.filter.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.MapConstraints;

/** A context supplying input to the {@link net.shibboleth.idp.attribute.filter.AttributeFilter} interface. */
@NotThreadSafe
public final class AttributeFilterContext extends BaseContext {

    /** Attributes which are to be filtered. */
    private Map<String, IdPAttribute> prefilteredAttributes;

    /** Attributes which have been filtered. */
    private Map<String, IdPAttribute> filteredAttributes;

    /** The principal associated with the filtering. */
    private String principal;

    /** The attribute source identity. */
    @Nullable private String attributeIssuerID;

    /** The attribute recipient identity. */
    @Nullable private String attributeRecipientID;

    /** How was the principal Authenticated? */
    @Nullable private String principalAuthenticationMethod;

    /** Cache of the metadata context. */
    @Nullable private SAMLMetadataContext requesterMetadataContext;

    /** How to get from hus to the SP metadata context. */
    @NonnullAfterInit
    private Function<AttributeFilterContext,SAMLMetadataContext> requesterMetadataContextLookupStrategy;

    /** Constructor. */
    public AttributeFilterContext() {
        prefilteredAttributes =
                MapConstraints.constrainedMap(new HashMap<String, IdPAttribute>(), MapConstraints.notNull());
        filteredAttributes =
                MapConstraints.constrainedMap(new HashMap<String, IdPAttribute>(), MapConstraints.notNull());
    }

    /**
     * Gets the collection of attributes that are to be filtered, indexed by attribute ID.
     * 
     * @return attributes to be filtered
     */
    @Nonnull @NonnullElements public Map<String, IdPAttribute> getPrefilteredIdPAttributes() {
        return prefilteredAttributes;
    }

    /**
     * Sets the attributes which are to be filtered.
     * 
     * @param attributes attributes which are to be filtered
     */
    public void setPrefilteredIdPAttributes(@Nullable @NullableElements final Collection<IdPAttribute> attributes) {
        Collection<IdPAttribute> checkedAttributes = new ArrayList<>();
        CollectionSupport.addIf(checkedAttributes, attributes, Predicates.notNull());

        prefilteredAttributes =
                MapConstraints.constrainedMap(new HashMap<String, IdPAttribute>(checkedAttributes.size()),
                        MapConstraints.notNull());

        for (final IdPAttribute attribute : checkedAttributes) {
            prefilteredAttributes.put(attribute.getId(), attribute);
        }
    }

    /**
     * Gets the collection of attributes, indexed by ID, left after the filtering process has run.
     * 
     * @return attributes left after the filtering process has run
     */
    @Nonnull @NonnullElements public Map<String, IdPAttribute> getFilteredIdPAttributes() {
        return filteredAttributes;
    }

    /**
     * Sets the attributes that have been filtered.
     * 
     * @param attributes attributes that have been filtered
     */
    public void setFilteredIdPAttributes(@Nullable @NullableElements final Collection<IdPAttribute> attributes) {
        Collection<IdPAttribute> checkedAttributes = new ArrayList<>();
        CollectionSupport.addIf(checkedAttributes, attributes, Predicates.notNull());

        filteredAttributes =
                MapConstraints.constrainedMap(new HashMap<String, IdPAttribute>(checkedAttributes.size()),
                        MapConstraints.notNull());

        for (final IdPAttribute attribute : checkedAttributes) {
            filteredAttributes.put(attribute.getId(), attribute);
        }
    }

    /**
     * Sets the principal associated with the filtering.
     * 
     * @return Returns the principal.
     */
    public String getPrincipal() {
        return principal;
    }

    /**
     * Gets the principal associated with the filtering.
     * 
     * @param who The principal to set.
     */
    public void setPrincipal(String who) {
        principal = who;
    }

    /**
     * Gets the attribute issuer (me) associated with this resolution.
     * 
     * @return the attribute issuer associated with this resolution.
     */
    @Nullable public String getAttributeIssuerID() {
        return attributeIssuerID;
    }

    /**
     * Sets the attribute issuer (me) associated with this resolution.
     * 
     * @param value the attribute issuer associated with this resolution.
     */
    @Nullable public void setAttributeIssuerID(@Nullable final String value) {
        attributeIssuerID = value;
    }

    /**
     * Gets the attribute recipient (her) associated with this resolution.
     * 
     * @return the attribute recipient associated with this resolution.
     */
    @Nullable public String getAttributeRecipientID() {
        return attributeRecipientID;
    }

    /**
     * Sets the attribute recipient (her) associated with this resolution.
     * 
     * @param value the attribute recipient associated with this resolution.
     */
    @Nullable public void setAttributeRecipientID(@Nullable final String value) {
        attributeRecipientID = value;
    }

    /**
     * Sets how the principal was authenticated.
     * 
     * @return Returns the principalAuthenticationMethod.
     */
    @Nullable public String getPrincipalAuthenticationMethod() {
        return principalAuthenticationMethod;
    }

    /**
     * Gets how the principal was authenticated.
     * 
     * @param method The principalAuthenticationMethod to set.
     */
    public void setPrincipalAuthenticationMethod(@Nullable final String method) {
        principalAuthenticationMethod = method;
    }

    /**
     * Get the strategy used to locate the SP's metadata context.
     * 
     * @return Returns the requesterMetadataContextLookupStrategy.
     */
    @NonnullAfterInit public Function<AttributeFilterContext, SAMLMetadataContext>
            getRequesterMetadataContextLookupStrategy() {
        return requesterMetadataContextLookupStrategy;
    }

    /**
     * Set the strategy used to locate the SP's metadata context.
     * 
     * @param strategy The requesterMetadataContextLookupStrategy to set.
     */
    public void setRequesterMetadataContextLookupStrategy(
            @Nonnull final Function<AttributeFilterContext, SAMLMetadataContext> strategy) {
        requesterMetadataContextLookupStrategy =
                Constraint.isNotNull(strategy, "MetadataContext lookup strategy cannot be null");
    }

    /** Get the Requester Metadata context.<br/> This value is cached and so only calculated once.
     * @return the cached context
     */
    @Nullable public SAMLMetadataContext getRequesterMetadataContext() {
        if (null == requesterMetadataContext && null != requesterMetadataContextLookupStrategy) {
            requesterMetadataContext = requesterMetadataContextLookupStrategy.apply(this);
        }
        return requesterMetadataContext;
    }
    
}