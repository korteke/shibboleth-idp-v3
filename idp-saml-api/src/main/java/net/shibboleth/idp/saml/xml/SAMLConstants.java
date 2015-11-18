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

package net.shibboleth.idp.saml.xml;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/** XML related constants used with SAML. */
public final class SAMLConstants {
    
    /** Legacy Shibboleth format for transient IDs in SAML 1.x. */
    @Nonnull @NotEmpty
    public static final String SAML1_NAMEID_TRANSIENT = "urn:mace:shibboleth:1.0:nameIdentifier";

    /** Legacy Shibboleth SAML 1.x AttributeNamespace for URI-named attributes. */
    @Nonnull @NotEmpty
    public static final String SAML1_ATTR_NAMESPACE_URI = "urn:mace:shibboleth:1.0:attributeNamespace:uri";
    
    /** Constructor. */
    private SAMLConstants() {

    }
}