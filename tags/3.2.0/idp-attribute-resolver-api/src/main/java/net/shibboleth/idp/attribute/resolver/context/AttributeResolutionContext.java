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

package net.shibboleth.idp.attribute.resolver.context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapConstraints;

/** A context supplying input to the {@link net.shibboleth.idp.attribute.resolver.AttributeResolver} interface. */
@NotThreadSafe
public class AttributeResolutionContext extends BaseContext {

    /** (internal) Names of the attributes that have been requested to be resolved. */
    @Nonnull @NonnullElements private Set<String> requestedAttributeNames;

    /** The principal associated with this resolution. */
    @Nullable private String principal;

    /** The attribute source identity. */
    @Nullable private String attributeIssuerID;

    /** The attribute recipient identity. */
    @Nullable private String attributeRecipientID;

    /** How was the principal Authenticated? */
    @Nullable private String principalAuthenticationMethod;

    /** Attributes which were resolved and released by the attribute resolver. */
    @Nonnull @NonnullElements private Map<String, IdPAttribute> resolvedAttributes;
    
    /** Constructor. */
    public AttributeResolutionContext() {
        requestedAttributeNames = Collections.emptySet();
        resolvedAttributes =
                MapConstraints.constrainedMap(new HashMap<String, IdPAttribute>(), MapConstraints.notNull());
    }

    /**
     * Get the attribute issuer (me) associated with this resolution.
     * 
     * @return the attribute issuer associated with this resolution.
     */
    @Nullable public String getAttributeIssuerID() {
        return attributeIssuerID;
    }

    /**
     * Set the attribute issuer (me) associated with this resolution.
     * 
     * @param value the attribute issuer associated with this resolution.
     */
    @Nullable public void setAttributeIssuerID(@Nullable String value) {
        attributeIssuerID = value;
    }

    /**
     * Get the attribute recipient (her) associated with this resolution.
     * 
     * @return the attribute recipient associated with this resolution.
     */
    @Nullable public String getAttributeRecipientID() {
        return attributeRecipientID;
    }

    /**
     * Set the attribute recipient (her) associated with this resolution.
     * 
     * @param value the attribute recipient associated with this resolution.
     */
    @Nullable public void setAttributeRecipientID(@Nullable String value) {
        attributeRecipientID = value;
    }

    /**
     * Set how the principal was authenticated.
     * 
     * @return Returns the principalAuthenticationMethod.
     */
    @Nullable public String getPrincipalAuthenticationMethod() {
        return principalAuthenticationMethod;
    }

    /**
     * Get how the principal was authenticated.
     * 
     * @param method The principalAuthenticationMethod to set.
     */
    public void setPrincipalAuthenticationMethod(@Nullable String method) {
        principalAuthenticationMethod = method;
    }

    /**
     * Set the principal associated with this resolution.
     * 
     * @return Returns the principal.
     */
    @Nullable public String getPrincipal() {
        return principal;
    }

    /**
     * Get the principal associated with this resolution.
     * 
     * @param who The principal to set.
     */
    public void setPrincipal(@Nullable String who) {
        principal = who;
    }

    /**
     * Get the (internal) names of the attributes requested to be resolved.
     * 
     * @return set of attributes requested to be resolved
     */
    @Nonnull @NonnullElements public Collection<String> getRequestedIdPAttributeNames() {
        return requestedAttributeNames;
    }

    /**
     * Set the (internal) names of the attributes requested to be resolved.
     * 
     * @param names the (internal) names of the attributes requested to be resolved
     */
    public void setRequestedIdPAttributeNames(@Nonnull @NonnullElements final Collection<String> names) {
        Constraint.isNotNull(names, "Requested IdPAttribute collection cannot be null");

        requestedAttributeNames = new HashSet<>(Collections2.filter(names, Predicates.notNull()));
    }

    /**
     * Get the collection of resolved attributes.
     * 
     * @return set of resolved attributes
     */
    @Nonnull @NonnullElements @Unmodifiable @NotLive public Map<String, IdPAttribute> getResolvedIdPAttributes() {
        return ImmutableMap.copyOf(resolvedAttributes);
    }

    /**
     * Set the set of resolved attributes.
     * 
     * @param attributes set of resolved attributes
     */
    public void setResolvedIdPAttributes(@Nullable @NullableElements final Collection<IdPAttribute> attributes) {
        resolvedAttributes = new HashMap<>();

        if (attributes != null) {
            for (IdPAttribute attribute : attributes) {
                if (attribute != null) {
                    resolvedAttributes.put(attribute.getId(), attribute);
                }
            }
        }
    }
    
}