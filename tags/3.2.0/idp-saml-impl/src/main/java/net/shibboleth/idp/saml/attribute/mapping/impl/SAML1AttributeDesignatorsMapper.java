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

package net.shibboleth.idp.saml.attribute.mapping.impl;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.saml.attribute.mapping.AbstractSAMLAttributeDesignatorMapper;
import net.shibboleth.idp.saml.attribute.mapping.AbstractSAMLAttributeDesignatorsMapper;

import com.google.common.base.Supplier;

/**
 * This class conceptually represents the content of a attribute-map file, hence it describes (and then does) the
 * mappings from a {@link java.util.List} of SAML1 {@link org.opensaml.saml.saml1.core.AttributeDesignator} into a
 * {@link com.google.common.collect.Multimap} going from (SAML1) attributeId to {@link IdPAttribute}s.
 * 
 */
public class SAML1AttributeDesignatorsMapper extends AbstractSAMLAttributeDesignatorsMapper<IdPAttribute> {

    /** Default constructor. */
    public SAML1AttributeDesignatorsMapper() {
        
    }

    /**
     * Generate a specific mapper to go from {@link org.opensaml.saml.saml1.core.AttributeDesignator} to
     * {@link IdPAttribute} by inverting the function of the mappers in the profiled {@link AttributeResolver}.
     * 
     * @param resolver resolver to invert
     */
    public SAML1AttributeDesignatorsMapper(@Nonnull final AttributeResolver resolver) {
        super(resolver, "Mapper<" + resolver.getId() + ">",
                new Supplier<AbstractSAMLAttributeDesignatorMapper<IdPAttribute>>() {

            @Override public SAML1AttributeDesignatorMapper get() {
                return new SAML1AttributeDesignatorMapper();
            }
        });
    }

}