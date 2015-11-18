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

import net.shibboleth.idp.attribute.IdPRequestedAttribute;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.saml.attribute.mapping.AbstractSAMLAttributeMapper;
import net.shibboleth.idp.saml.attribute.mapping.AbstractSAMLAttributesMapper;

import org.opensaml.saml.saml2.metadata.RequestedAttribute;

import com.google.common.base.Supplier;

/**
 * This class conceptually represents the content of a attribute-map file, hence it describes (and then does) the
 * mappings from a {@link java.util.List} of SAML2 {@link RequestedAttribute} into a
 * {@link com.google.common.collect.Multimap} going from (SAML2) attributeId to {@link IdPRequestedAttribute}s.
 * 
 */
public class RequestedAttributesMapper extends AbstractSAMLAttributesMapper<RequestedAttribute, IdPRequestedAttribute> {
    
    /** Default constructor. */
    public RequestedAttributesMapper() {
        
    }

    /**
     * Generate a specific mapper to go from {@link RequestedAttribute} to {@link IdPRequestedAttribute} by inverting
     * the function of the mappers in the profiled {@link AttributeResolver}.
     * 
     * @param resolver resolver to invert
     */
    public RequestedAttributesMapper(@Nonnull final AttributeResolver resolver) {
        super(resolver, "RequestedMapper<" + resolver.getId() + ">",
                new Supplier<AbstractSAMLAttributeMapper<RequestedAttribute, IdPRequestedAttribute>>() {

                    @Override public RequestedAttributeMapper get() {
                        return new RequestedAttributeMapper();
                    }
                });
    }

}