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

package net.shibboleth.idp.attribute.resolver.dc.ldap.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.MultipleResultAnErrorResolutionException;
import net.shibboleth.idp.attribute.resolver.NoResultAnErrorResolutionException;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.dc.AbstractMappingStrategy;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple {@link SearchResultMappingStrategy} that iterates over all result entries and includes all attribute values
 * as strings.
 */
public class StringAttributeValueMappingStrategy extends AbstractMappingStrategy<SearchResult>
        implements SearchResultMappingStrategy {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(StringAttributeValueMappingStrategy.class);

 // Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override @Nullable public Map<String,IdPAttribute> map(@Nonnull final SearchResult results)
            throws ResolutionException {
        Constraint.isNotNull(results, "Results can not be null");

        if (results.size() == 0) {
            log.debug("Results did not contain any entries, nothing to map");
            if (isNoResultAnError()) {
                throw new NoResultAnErrorResolutionException("No entries returned from search");
            }
            return null;
        } else if (results.size() > 1 && isMultipleResultsAnError()) {
            throw new MultipleResultAnErrorResolutionException("Multiple entries returned from search");
        }

        final Map<String,IdPAttribute> attributes = new HashMap<>(results.size());
        
        final Map<String,String> aliases = getResultRenamingMap();

        for (final LdapEntry entry : results.getEntries()) {
            for (final LdapAttribute attr : entry.getAttributes()) {
                
                final String originalId = attr.getName();
                final String effectiveId = aliases.containsKey(originalId) ? aliases.get(originalId) : originalId;
                if (log.isDebugEnabled()) {
                    if (!effectiveId.equals(originalId)) {
                        log.debug("Remapping attribute {} to {}", originalId, effectiveId);
                    }
                }
                
                IdPAttribute attribute = attributes.get(effectiveId);
                if (attribute == null) {
                    attribute = new IdPAttribute(effectiveId);
                    attributes.put(effectiveId, attribute);
                }

                final List<IdPAttributeValue<?>> values = new ArrayList<>(
                        attr.getStringValues().size() + attribute.getValues().size());

                values.addAll(attribute.getValues());
                
                for (final String value : attr.getStringValues()) {
                    values.add(StringAttributeValue.valueOf(value));
                }
                attribute.setValues(values);
            }
        }
        
        log.trace("Mapping strategy mapped {} to {}", results, attributes);
        if (attributes.isEmpty()) {
            return null;
        } else {
            return attributes;
        }
    }
 // Checkstyle: CyclomaticComplexity ON
    
}