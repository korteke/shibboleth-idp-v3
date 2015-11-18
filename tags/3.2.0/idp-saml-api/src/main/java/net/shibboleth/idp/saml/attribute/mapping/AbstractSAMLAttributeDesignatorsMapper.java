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
import net.shibboleth.idp.saml.attribute.encoding.AttributeDesignatorMapperProcessor;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.saml.saml1.core.AttributeDesignator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * The class contains the mechanics to go from a list of {@link AttributeDesignator}s to a {@link Multimap} of
 * {@link String},{@link IdPAttribute} (or derived, or null). The representation as a {@link Multimap} is useful for
 * filtering situations and is exploited by the AttributeInMetadata filter.
 * 
 * @param <OutType> some sort of representation of an IdP attribute
 */
public abstract class AbstractSAMLAttributeDesignatorsMapper<OutType extends IdPAttribute>
        extends AbstractIdentifiableInitializableComponent implements AttributesMapper<AttributeDesignator,OutType> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractSAMLAttributeDesignatorsMapper.class);

    /** The mappers we can apply. */
    @Nonnull @NonnullElements private Collection<AttributeMapper<AttributeDesignator,OutType>> mappers;

    /** The String used to prefix log message. */
    @Nullable private String logPrefix;

    /** Default Constructor. */
    public AbstractSAMLAttributeDesignatorsMapper() {
        mappers = Collections.emptyList();
    }
    
    /**
     * Constructor to create the mapping from an existing resolver.
     * 
     * <p>This code inverts the {@link AttributeEncoder} (internal attribute -> SAML Attributes) into
     * {@link AttributeMapper} (SAML [AttributeDesignators] -> internal [Requested] Attributes). <br/>
     * to generate the {@link AbstractSAMLAttributeDesignatorMapper} (with no
     * {@link AbstractSAMLAttributeDesignatorMapper#getAttributeIds()}. These are accumulated into a {@link Multimap},
     * where the key is the {@link AbstractSAMLAttributeDesignatorMapper} and the values are the (IdP) attribute names.
     * The collection of {@link AttributeMapper}s can then be extracted from the map, and the appropriate internal names
     * added (these being the value of the {@link Multimap})</p>
     * 
     * @param resolver the resolver
     * @param id the ID
     * @param mapperFactory factory to generate new mappers of the correct type.
     */
    public AbstractSAMLAttributeDesignatorsMapper(@Nonnull final AttributeResolver resolver,
            @Nonnull @NotEmpty final String id,
            @Nonnull final Supplier<AbstractSAMLAttributeDesignatorMapper<OutType>> mapperFactory) {

        setId(id); 
        mappers = Collections.emptyList();
        
        final Multimap<AbstractSAMLAttributeDesignatorMapper<OutType>,String> theMappers = HashMultimap.create();

        for (final AttributeDefinition attributeDef : resolver.getAttributeDefinitions().values()) {
            for (final AttributeEncoder encoder : attributeDef.getAttributeEncoders()) {
                if (encoder instanceof AttributeDesignatorMapperProcessor) {
                    // There is an appropriate reverse mapper.
                    final AttributeDesignatorMapperProcessor factory = (AttributeDesignatorMapperProcessor) encoder;
                    final AbstractSAMLAttributeDesignatorMapper<OutType> mapper = mapperFactory.get();
                    factory.populateAttributeMapper(mapper);

                    theMappers.put(mapper, attributeDef.getId());
                }
            }
        }

        mappers = new ArrayList<>(theMappers.values().size());

        for (final Entry<AbstractSAMLAttributeDesignatorMapper<OutType>,Collection<String>> entry
                : theMappers.asMap().entrySet()) {

            final AbstractSAMLAttributeDesignatorMapper<OutType> mapper = entry.getKey();
            mapper.setAttributeIds(new ArrayList<>(entry.getValue()));
            mappers.add(mapper);
        }
    }

    /**
     * Get the mappers.
     * 
     * @return Returns the mappers.
     */
    @Nonnull @NonnullElements public Collection<AttributeMapper<AttributeDesignator,OutType>> getMappers() {
        return mappers;
    }

    /**
     * Set the attribute mappers into the lookup map.
     * 
     * @param theMappers The mappers to set.
     */
    public void setMappers(@Nonnull Collection<AttributeMapper<AttributeDesignator,OutType>> theMappers) {
        mappers = Constraint.isNotNull(theMappers, "Mappers list cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NonnullElements public Multimap<String,OutType> mapAttributes(
            @Nonnull @NonnullElements final List<AttributeDesignator> prototypes) {

        final Multimap<String,OutType> result = ArrayListMultimap.create();

        for (final AttributeDesignator prototype : prototypes) {
            for (final AttributeMapper<AttributeDesignator,OutType> mapper : mappers) {

                final Map<String,OutType> mappedAttributes = mapper.mapAttribute(prototype);

                log.debug("{} SAML attribute '{}' mapped to {} attributes by mapper '{}'", getLogPrefix(),
                        prototype.getAttributeName(), mappedAttributes.size(), mapper.getId());

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