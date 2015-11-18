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
import net.shibboleth.idp.saml.attribute.mapping.AbstractSAMLAttributeMapper;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import org.opensaml.saml.saml2.core.Attribute;

/** A class that maps a SAML2 {@link Attribute} into an IdP {@link IdPAttribute}. */
public class SAML2AttributeMapper extends AbstractSAMLAttributeMapper<Attribute, IdPAttribute> {
    
    /** {@inheritDoc} */
    @Override
    @Nonnull protected IdPAttribute newAttribute(@Nonnull final Attribute input, @Nonnull @NotEmpty final String id) {
        return new IdPAttribute(id);
    }
    
}