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

package net.shibboleth.idp.saml.profile.config;

import javax.annotation.Nullable;

/**
 * Interface for outbound SAML artifact configuration.
 * 
 * <p>While sufficient for known SAML 1 and 2 artifact types, the interface
 * may be extended if necessary to carry type-specific additions.</p>
 */
public interface SAMLArtifactConfiguration {

    /**
     * Get the type code of the artifact to use.
     * 
     * @return the artifact type code
     */
    @Nullable byte[] getArtifactType();
    
    /**
     * Get the location, as a URL, of the issuer's resolution service endpoint.
     * 
     * @return  location of resolution service endpoint
     */
    @Nullable String getArtifactResolutionServiceURL();
    
    /**
     * Get the index of the issuer's resolution service endpoint, corresponding to its metadata. 
     * 
     * @return  index of resolution service endpoint in metadata
     */
    @Nullable Integer getArtifactResolutionServiceIndex();
}