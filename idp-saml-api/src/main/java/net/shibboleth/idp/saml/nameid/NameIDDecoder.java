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

package net.shibboleth.idp.saml.nameid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;

import org.opensaml.saml.saml2.core.NameID;

/** Interface for converting a {@link NameID} back into a principal name. */
public interface NameIDDecoder {

    /**
     * Decode the provided {@link NameID}.
     * 
     * <p>If the object is incompatible with the decoder in some way, a null is returned.</p> 
     * 
     * @param c14nContext the active c14n context
     * @param nameID the object to decode
     * 
     * @return the principal decoded from the value, or null
     * @throws NameDecoderException if an error occurred during translation
     */
    @Nullable String decode(@Nonnull final SubjectCanonicalizationContext c14nContext, @Nonnull final NameID nameID)
            throws NameDecoderException;

}