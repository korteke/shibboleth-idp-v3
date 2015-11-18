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
import net.shibboleth.idp.saml.nameid.NameIdentifierDecoder;

import org.opensaml.saml.saml1.core.NameIdentifier;

/**
 * Decodes {@link NameIdentifier#getValue()}  via the base class (reversing the work done by
 * {@link net.shibboleth.idp.saml.attribute.resolver.impl.TransientIdAttributeDefinition}).
 */
public class TransientNameIdentifierDecoder extends BaseTransientDecoder implements NameIdentifierDecoder {

    /** {@inheritDoc} */
    @Override
    @Nonnull public String decode(@Nonnull final SubjectCanonicalizationContext c14nContext,
            @Nonnull final NameIdentifier nameIdentifier) throws NameDecoderException {

        return super.decode(nameIdentifier.getValue(), c14nContext.getRequesterId());
    }

}