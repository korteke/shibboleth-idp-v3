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

package net.shibboleth.idp.saml.attribute.encoding;

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;

import org.opensaml.saml.saml1.core.Attribute;


/**
 * Interface for encoders that produce a SAML 1 {@link Attribute}.
 * 
 * @param <EncodedType> the type of data that can be encoded by the encoder
 */
public interface SAML1AttributeEncoder<EncodedType extends IdPAttributeValue> extends AttributeEncoder<Attribute> {

    /**
     * Get the encoded Name of the attribute.
     * 
     * @return name of the attribute
     */
    @NonnullAfterInit String getName();
 
    /**
     * Get the encoded Namespace of the attribute.
     * 
     * @return namespace of the attribute
     */
    @NonnullAfterInit String getNamespace();

}