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

package net.shibboleth.idp.saml.saml2.profile.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.attribute.mapping.impl.SAML2AttributesMapperService;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.messaging.context.navigate.MessageLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Multimap;

/**
 * Action that filters a set of attributes against the {@link org.opensaml.saml.saml2.core.Attribute} objects in
 * an {@link AttributeQuery}.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 */
public class FilterByQueriedAttributes extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(FilterByQueriedAttributes.class);

    /** Service used to get the engine used to filter attributes. */
    @Nonnull private final SAML2AttributesMapperService mapperService;

    /** Strategy used to locate the {@link AttributeQuery} to filter against. */
    @Nonnull private Function<ProfileRequestContext,AttributeQuery> queryLookupStrategy;

    /** Strategy used to locate the {@link AttributeContext} to filter. */
    @Nonnull private Function<ProfileRequestContext,AttributeContext> attributeContextLookupStrategy;

    /** Query to filter against. */
    @Nullable private AttributeQuery query;
    
    /** AttributeContext to filter. */
    @Nullable private AttributeContext attributeContext;

    /**
     * Constructor.
     * 
     * @param mapper mapper used to consume designators
     */
    public FilterByQueriedAttributes(@Nonnull final SAML2AttributesMapperService mapper) {
        mapperService = Constraint.isNotNull(mapper, "MapperService cannot be null");
        
        attributeContextLookupStrategy = Functions.compose(new ChildContextLookup<>(AttributeContext.class),
                new ChildContextLookup<ProfileRequestContext,RelyingPartyContext>(RelyingPartyContext.class));
        
        queryLookupStrategy = Functions.compose(new MessageLookup(AttributeQuery.class),
                new InboundMessageContextLookup());
    }

    /**
     * Set the strategy used to locate the {@link AttributeQuery} associated with a given {@link ProfileRequestContext}.
     * 
     * @param strategy lookup strategy
     */
    public void setQueryLookupStrategy(@Nonnull final Function<ProfileRequestContext,AttributeQuery> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        queryLookupStrategy = Constraint.isNotNull(strategy, "Request lookup strategy cannot be null");
    }

    /**
     * Set the strategy used to locate the {@link AttributeContext} associated with a given
     * {@link ProfileRequestContext}.
     * 
     * @param strategy lookup strategy
     */
    public void setAttributeContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,AttributeContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        attributeContextLookupStrategy =
                Constraint.isNotNull(strategy, "AttributeContext lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        
        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        query = queryLookupStrategy.apply(profileRequestContext);
        
        if (query == null || query.getAttributes().isEmpty()) {
            log.debug("No queried Attributes found, nothing to do ");
            return false;
        }
        
        attributeContext = attributeContextLookupStrategy.apply(profileRequestContext);
        if (attributeContext == null) {
            log.debug("{} No attribute context, no attributes to filter", getLogPrefix());
            return false;
        }

        if (attributeContext.getIdPAttributes().isEmpty()) {
            log.debug("{} No attributes to filter", getLogPrefix());
            return false;
        }

        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        
        final Collection<IdPAttribute> keepers = new ArrayList<>(query.getAttributes().size());
        
        final Multimap<String,IdPAttribute> mapped = mapperService.mapAttributes(query.getAttributes());
        log.debug("{} Query content mapped to attribute IDs: {}", getLogPrefix(), mapped.keySet());
        
        for (final IdPAttribute attribute : attributeContext.getIdPAttributes().values()) {
            
            final Collection<IdPAttribute> requested = mapped.get(attribute.getId());
            
            if (!requested.isEmpty()) {
                log.debug("{} Attribute '{}' requested by query, checking for requested values", getLogPrefix(),
                        attribute.getId());
                
                final int count = filterRequestedValues(attribute, requested);
                if (count > 0) {
                    log.debug("{} Retaining requested attribute '{}' with {} value(s)", getLogPrefix(),
                            attribute.getId(), count);
                    keepers.add(attribute);
                } else {
                    log.debug("{} Removing requested attribute '{}', no values left after filtering", getLogPrefix(),
                            attribute.getId());
                }
            } else {
                log.debug("{} Removing attribute '{}' not requested by query", getLogPrefix(), attribute.getId());
            }
        }
        
        attributeContext.setIdPAttributes(keepers);
    }
    
    /**
     * Adjust an input attribute's values based on any values requested.
     * 
     * @param attribute attribute to filter
     * @param requestedAttributes the attributes (and possibly values) requested
     * 
     * @return  the number of values left in the input attribute
     */
    private int filterRequestedValues(@Nonnull final IdPAttribute attribute,
            @Nonnull @NonnullElements final Collection<IdPAttribute> requestedAttributes) {
        
        boolean requestedValues = false;
        
        final Collection<IdPAttributeValue<?>> keepers = new ArrayList<>(attribute.getValues().size());
        
        for (final IdPAttributeValue<?> value : attribute.getValues()) {
            
            for (final IdPAttribute requested : requestedAttributes) {
                if (!requested.getValues().isEmpty()) {
                    requestedValues = true;
                    if (requested.getValues().contains(value)) {
                        keepers.add(value);
                        break;
                    }
                }
            }
            
            if (!requestedValues) {
                keepers.add(value);
            }
        }
        
        attribute.setValues(keepers);
        return keepers.size();
    }

}