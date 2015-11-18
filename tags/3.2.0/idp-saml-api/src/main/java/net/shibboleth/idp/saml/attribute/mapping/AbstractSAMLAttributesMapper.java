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

package net.shibboleth.idp.saml.attribute.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.saml.attribute.encoding.AttributeMapperProcessor;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * The class contains the mechanics to go from a list of {@link Attribute}s (or derived) to a {@link Multimap} of
 * {@link String},{@link IdPAttribute} (or derived, or null). The representation as a {@link Multimap} is useful for
 * filtering situations and is exploited by AttributeInMetadata filter.
 * 
 * @param <InType> the type which is to be inspected and mapped
 * @param <OutType> some sort of representation of an IdP attribute
 */

public abstract class AbstractSAMLAttributesMapper<InType extends Attribute, OutType extends IdPAttribute> extends
        AbstractIdentifiableInitializableComponent implements AttributesMapper<InType, OutType> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractSAMLAttributesMapper.class);

    /** The mappers we can apply. */
    @Nonnull @NonnullElements private Collection<AttributeMapper<InType,OutType>> mappers = Collections.EMPTY_LIST;

    /** The String used to prefix log message. */
    @Nullable private String logPrefix;

    /** Default Constructor. */
    public AbstractSAMLAttributesMapper() {
        
    }
    
    /**
     * Constructor to create the mapping from an existing resolver. <br/>
     * This code inverts the {@link AttributeEncoder} (internal attribute -> SAML Attributes) into
     * {@link AttributeMapper} (SAML [RequestedAttributes] -> internal [Requested] Attributes). <br/>
     * to generate the {@link AbstractSAMLAttributeMapper} (with no
     * {@link AbstractSAMLAttributeMapper#getAttributeIds()}. These are accumulated into a {@link Multimap}, where the
     * key is the {@link AbstractSAMLAttributeMapper} and the values are the (IdP) attribute names. The collection of
     * {@link AttributeMapper}s can then be extracted from the map, and the appropriate internal names added (these
     * being the value of the {@link Multimap})
     * 
     * @param resolver The resolver
     * @param id The it
     * @param mapperFactory A factory to generate new mappers of the correct type.
     */
    public AbstractSAMLAttributesMapper(@Nonnull final AttributeResolver resolver, @Nonnull @NotEmpty final String id,
            @Nonnull final Supplier<AbstractSAMLAttributeMapper<InType,OutType>> mapperFactory) {

        setId(id); 

        final Multimap<AbstractSAMLAttributeMapper<InType,OutType>,String> theMappers;

        theMappers = HashMultimap.create();

        for (final AttributeDefinition attributeDef : resolver.getAttributeDefinitions().values()) {
            for (final AttributeEncoder encode : attributeDef.getAttributeEncoders()) {
                if (encode instanceof AttributeMapperProcessor) {
                    // There is an appropriate reverse mapper
                    final AttributeMapperProcessor factory = (AttributeMapperProcessor) encode;
                    final AbstractSAMLAttributeMapper<InType,OutType> mapper = mapperFactory.get();
                    factory.populateAttributeMapper(mapper);

                    theMappers.put(mapper, attributeDef.getId());
                }
            }
        }

        mappers = new ArrayList<>(theMappers.values().size());

        for (final Entry<AbstractSAMLAttributeMapper<InType,OutType>,Collection<String>> entry : theMappers.asMap()
                .entrySet()) {

            final AbstractSAMLAttributeMapper<InType, OutType> mapper = entry.getKey();
            mapper.setAttributeIds(new ArrayList<>(entry.getValue()));
            mappers.add(mapper);
        }
    }

    /**
     * Get the mappers.
     * 
     * @return Returns the mappers.
     */
    @Nonnull @NonnullElements public Collection<AttributeMapper<InType,OutType>> getMappers() {
        return mappers;
    }

    /**
     * Set the attribute mappers into the lookup map.
     * 
     * @param theMappers The mappers to set.
     */
    public void setMappers(@Nonnull Collection<AttributeMapper<InType,OutType>> theMappers) {
        mappers = Constraint.isNotNull(theMappers, "mappers list must be non null");
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NonnullElements public Multimap<String,OutType> mapAttributes(
            @Nonnull @NonnullElements final List<InType> prototypes) {

        final Multimap<String,OutType> result = ArrayListMultimap.create();

        for (final InType prototype : prototypes) {
            for (final AttributeMapper<InType,OutType> mapper : mappers) {

                final Map<String,OutType> mappedAttributes;
                try {
                    mappedAttributes = mapper.mapAttribute(prototype); 
                } catch (Exception e) {
                    log.error("{} Attempt to map SAML attribute '{}' with mapper '{}' failed: ", getLogPrefix(),
                            prototype.getName(),mapper.getId(), e);
                    throw e;
                }

                log.debug("{} SAML attribute '{}' mapped to {} attributes by mapper '{}'", getLogPrefix(),
                        prototype.getName(), mappedAttributes.size(), mapper.getId());

                for (final Entry<String,OutType> entry : mappedAttributes.entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        logPrefix = null;
        for (final AttributeMapper mapper : mappers) {
            ComponentSupport.initialize(mapper);
        }
    }

    /**
     * Return a string which is to be prepended to all log messages.
     * 
     * @return "Attribute Mappers '<ID>' :"
     */
    @Nonnull @NotEmpty private String getLogPrefix() {
        String s = logPrefix;
        if (null == s) {
            s = new StringBuilder("Attribute Mappers : '").append(getId()).append("':").toString();
            logPrefix = s;
        }
        return s;
    }
}
