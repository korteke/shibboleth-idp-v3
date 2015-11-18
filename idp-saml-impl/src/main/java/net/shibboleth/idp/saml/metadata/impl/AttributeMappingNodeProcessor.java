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

package net.shibboleth.idp.saml.metadata.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPRequestedAttribute;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.saml.attribute.mapping.AttributesMapContainer;
import net.shibboleth.idp.saml.attribute.mapping.impl.RequestedAttributesMapper;
import net.shibboleth.idp.saml.attribute.mapping.impl.SAML2AttributesMapper;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.metadata.resolver.filter.FilterException;
import org.opensaml.saml.metadata.resolver.filter.MetadataNodeProcessor;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

/**
 * An implementation of {@link MetadataNodeProcessor} which extracts {@link IdPRequestedAttribute}s from any
 * {@link AttributeConsumingService} we find and {@link IdPAttribute}s from any {@link EntityDescriptor} that we find.
 */
@NotThreadSafe
public class AttributeMappingNodeProcessor implements MetadataNodeProcessor {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AttributeMappingNodeProcessor.class);

    /** Service used to get the resolver used to fetch attributes. */
    @Nonnull private final ReloadableService<AttributeResolver> attributeResolverService;

    /** Whether the last invocation of {@link #refreshMappers()} failed. */
    @Nonnull private boolean refreshFailed;

    /** Cached RequestedAttributeMapper. */
    @Nullable private RequestedAttributesMapper requestedAttributesMapper;

    /** Cached AttributeMapper. */
    @Nullable private SAML2AttributesMapper attributesMapper;

    /** Date when the cache was last refreshed. */
    @Nullable private DateTime lastReload;

    /**
     * Constructor.
     * 
     * @param resolverService the service for the attribute resolver we are to derive unmapping info from
     */
    public AttributeMappingNodeProcessor(@Nonnull final ReloadableService<AttributeResolver> resolverService) {
        attributeResolverService = Constraint.isNotNull(resolverService, "AttributeResolver cannot be null");
    }

    /**
     * Get the current RequestedAttributesMapper.
     * 
     * @return Returns the mapper.
     */
    public RequestedAttributesMapper getRequestedAttributesMapper() {
        return requestedAttributesMapper;
    }

    /**
     * Set the current RequestedAttributesMapper.
     * 
     * @param mapper what to set.
     */
    public void setRequestedAttributesMapper(@Nullable RequestedAttributesMapper mapper) {
        requestedAttributesMapper = mapper;
    }

    /**
     * Get the current AttributesMapper.
     * 
     * @return Returns the mapper.
     */
    public SAML2AttributesMapper getAttributesMapper() {
        return attributesMapper;
    }

    /**
     * Set the current AttributesMapper.
     * 
     * @param mapper what to set.
     */
    public void setAttributesMapper(@Nullable SAML2AttributesMapper mapper) {
        attributesMapper = mapper;
    }

    /**
     * Inspect the service and see whether we need to reload the mappers.
     * 
     * @throws FilterException if the mapping generation fails
     */
    protected void refreshMappers() throws FilterException {
        if (lastReload != null && lastReload.equals(attributeResolverService.getLastSuccessfulReloadInstant())) {
            // Nothing has changed since we last reloaded.
            return;
        }

        // Reload
        ServiceableComponent<AttributeResolver> component = null;
        RequestedAttributesMapper ram = null;
        SAML2AttributesMapper am = null;
        try {
            // get date before we get the component. That way we'll not leak changes.
            final DateTime when = attributeResolverService.getLastSuccessfulReloadInstant();
            component = attributeResolverService.getServiceableComponent();
            if (null == component) {
                if (!refreshFailed) {
                    log.error("Requested Attributes Mapper: Invalid Attribute resolver configuration.");
                }
                refreshFailed = true;
            } else {
                final AttributeResolver attributeResolver = component.getComponent();
                ram = new RequestedAttributesMapper(attributeResolver);
                am = new SAML2AttributesMapper(attributeResolver);

                refreshFailed = false;
                lastReload = when;
            }
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
        try {
            if (null != ram) {
                ram.initialize();
            }
            if (null != am) {
                am.initialize();
            }
        } catch (ComponentInitializationException e) {
            throw new FilterException(e);
        }
        setRequestedAttributesMapper(ram);
        setAttributesMapper(am);
    }

    /** {@inheritDoc} */
    @Override public void process(XMLObject metadataNode) throws FilterException {
        refreshMappers();
        if (metadataNode instanceof AttributeConsumingService) {

            handleAttributeConsumingService((AttributeConsumingService) metadataNode);

        } else if (metadataNode instanceof EntityDescriptor) {
            handleEntityDescriptor((EntityDescriptor) metadataNode);
        }
    }

    /**
     * Look inside the {@link AttributeConsumingService} for any {@link RequestedAttribute}s and map them.
     * 
     * @param acs the {@link AttributeConsumingService} to look at
     */
    private void handleAttributeConsumingService(AttributeConsumingService acs) {
        final List<RequestedAttribute> requestedAttributes = acs.getRequestAttributes();
        final RequestedAttributesMapper mapper = getRequestedAttributesMapper();
        if (null == requestedAttributes || requestedAttributes.isEmpty() || null == mapper) {
            return;
        }
        final Multimap<String, IdPRequestedAttribute> maps = mapper.mapAttributes(requestedAttributes);
        if (null == maps || maps.isEmpty()) {
            return;
        }
        acs.getObjectMetadata().put(new AttributesMapContainer<>(maps));
    }

    /**
     * Look inside the {@link EntityDescriptor} for entities Attributes and map them.
     * 
     * @param entity the entity
     */
    private void handleEntityDescriptor(EntityDescriptor entity) {
        final SAML2AttributesMapper mapper = getAttributesMapper();
        final Extensions extensions = entity.getExtensions();
        if (null == extensions || null == mapper) {
            return;
        }
        final List<XMLObject> entityAttributesList =
                extensions.getUnknownXMLObjects(EntityAttributes.DEFAULT_ELEMENT_NAME);
        if (null == entityAttributesList || entityAttributesList.isEmpty()) {
            return;
        }
        final List<Attribute> entityAttributes = new ArrayList<>();
        for (XMLObject xmlObj : entityAttributesList) {
            if (xmlObj instanceof EntityAttributes) {
                EntityAttributes ea = (EntityAttributes) xmlObj;
                entityAttributes.addAll(ea.getAttributes());
            }
        }
        final Multimap<String, IdPAttribute> maps = mapper.mapAttributes(entityAttributes);
        if (null == maps || maps.isEmpty()) {
            return;
        }
        entity.getObjectMetadata().put(new AttributesMapContainer<>(maps));
    }
}
