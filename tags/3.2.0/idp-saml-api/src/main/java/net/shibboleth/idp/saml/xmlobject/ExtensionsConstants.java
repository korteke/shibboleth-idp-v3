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

package net.shibboleth.idp.saml.xmlobject;

/** Constants used in XMLObject extensions. */
public final class ExtensionsConstants {

    /** Relative resource path for XML schema for Shibboleth extensions to SAML metadata. */
    public static final String SHIB_MDEXT10_SCHEMA_LOCATION = "/schema/shibboleth-metadata-1.0.xsd";
    
    /** URI for XML schema namespace for Shibboleth extensions to SAML metadata. */
    public static final String SHIB_MDEXT10_NS = "urn:mace:shibboleth:metadata:1.0";

    /** Namespace QName prefix for Shibboleth extensions to SAML metadata. */
    public static final String SHIB_MDEXT10_PREFIX = "shibmd";

    /** Relative resource path for XML schema for Shibboleth extensions supporting SAML delegation. */
    public static final String SHIB_DELEXT10_SCHEMA_LOCATION = "/schema/shibboleth-delegation-1.0.xsd";
    
    /** URI for XML schema namespace for Shibboleth extensions supporting SAML delegation. */
    public static final String SHIB_DELEXT10_NS = "urn:mace:shibboleth:delegation:1.0";

    /** Namespace QName prefix for Shibboleth extensions supporting AML delegation. */
    public static final String SHIB_DELEXT10_PREFIX = "shibdel";

    /** Constructor. */
    private ExtensionsConstants() {
    }
}