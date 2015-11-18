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

package net.shibboleth.idp.saml.saml1.profile.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.attribute.mapping.impl.SAML1AttributeDesignatorsMapperService;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.messaging.context.navigate.MessageLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;
import org.opensaml.saml.saml1.core.AttributeQuery;
import org.opensaml.saml.saml1.core.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Multimap;

/**
 * Action that filters a set of attributes against the {@link org.opensaml.saml.saml1.core.AttributeDesignator}
 * objects in an {@link AttributeQuery}.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 */
public class FilterByQueriedAttributeDesignators extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(FilterByQueriedAttributeDesignators.class);

    /** Service used to get the engine used to filter attributes. */
    @Nonnull private final SAML1AttributeDesignatorsMapperService mapperService;

    /** Strategy used to locate the {@link Request} containing the query to filter against. */
    @Nonnull private Function<ProfileRequestContext,Request> requestLookupStrategy;

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
    public FilterByQueriedAttributeDesignators(@Nonnull final SAML1AttributeDesignatorsMapperService mapper) {
        mapperService = Constraint.isNotNull(mapper, "MapperService cannot be null");
        
        attributeContextLookupStrategy = Functions.compose(new ChildContextLookup<>(AttributeContext.class),
                new ChildContextLookup<ProfileRequestContext,RelyingPartyContext>(RelyingPartyContext.class));
        
        requestLookupStrategy = Functions.compose(new MessageLookup(Request.class), new InboundMessageContextLookup());
    }

    /**
     * Set the strategy used to locate the {@link Request} associated with a given {@link ProfileRequestContext}.
     * 
     * @param strategy lookup strategy
     */
    public void setRequestLookupStrategy(@Nonnull final Function<ProfileRequestContext,Request> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        requestLookupStrategy = Constraint.isNotNull(strategy, "Request lookup strategy cannot be null");
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
        
        final Request request = requestLookupStrategy.apply(profileRequestContext);
        if (request != null) {
            query = request.getAttributeQuery();
        }
        
        if (query == null || query.getAttributeDesignators().isEmpty()) {
            log.debug("No AttributeDesignators found, nothing to do ");
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
        
        final Collection<IdPAttribute> keepers = new ArrayList<>(query.getAttributeDesignators().size());
        
        final Multimap<String,IdPAttribute> mapped = mapperService.mapAttributes(query.getAttributeDesignators());
        log.debug("Query content mapped to attribute IDs: {}", mapped.keySet());
        
        for (final IdPAttribute attribute : attributeContext.getIdPAttributes().values()) {
            if (mapped.containsKey(attribute.getId())) {
                log.debug("Retaining attribute '{}' requested by query", attribute.getId());
                keepers.add(attribute);
            } else {
                log.debug("Removing attribute '{}' not requested by query", attribute.getId());
            }
        }
        
        attributeContext.setIdPAttributes(keepers);
    }

}