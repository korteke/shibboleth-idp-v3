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

package net.shibboleth.idp.saml.nameid.impl;

import javax.annotation.Nonnull;

import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.idp.saml.nameid.NameIDDecoder;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import org.opensaml.saml.saml2.core.NameID;

/**
 * Decodes {@link NameID#getValue()}  via the base class (reversing the work done by
 * {@link net.shibboleth.idp.saml.attribute.resolver.impl.TransientIdAttributeDefinition}).
 */
public class TransientNameIDDecoder extends BaseTransientDecoder implements NameIDDecoder {

    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String decode(@Nonnull final SubjectCanonicalizationContext c14nContext,
            @Nonnull final NameID nameID) throws NameDecoderException {

        return super.decode(nameID.getValue(), c14nContext.getRequesterId());
    }

}