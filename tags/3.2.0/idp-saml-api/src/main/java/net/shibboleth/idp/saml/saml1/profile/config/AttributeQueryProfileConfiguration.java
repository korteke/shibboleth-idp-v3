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

package net.shibboleth.idp.saml.saml1.profile.config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.profile.logic.NoIntegrityMessageChannelPredicate;

import net.shibboleth.idp.saml.profile.config.AbstractSAMLProfileConfiguration;
import net.shibboleth.idp.saml.profile.config.SAMLArtifactAwareProfileConfiguration;
import net.shibboleth.idp.saml.profile.config.SAMLArtifactConfiguration;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/** Configuration support for SAML 1 attribute query requests. */
public class AttributeQueryProfileConfiguration
        extends AbstractSAMLProfileConfiguration
        implements SAML1ProfileConfiguration, SAMLArtifactAwareProfileConfiguration {

    /** ID for this profile configuration. */
    public static final String PROFILE_ID = "http://shibboleth.net/ns/profiles/saml1/query/attribute";

    /** SAML artifact configuration. */
    @Nullable private SAMLArtifactConfiguration artifactConfig;
    
    /** Constructor. */
    public AttributeQueryProfileConfiguration() {
        this(PROFILE_ID);
    }

    /**
     * Constructor.
     * 
     * @param profileId unique ID for this profile
     */
    protected AttributeQueryProfileConfiguration(@Nonnull @NotEmpty final String profileId) {
        super(profileId);
        setSignResponses(new NoIntegrityMessageChannelPredicate());
    }
    
    /** {@inheritDoc} */
    @Override @Nullable public SAMLArtifactConfiguration getArtifactConfiguration() {
        return artifactConfig;
    }

    /**
     * Set the SAML artifact configuration, if any.
     * 
     * @param config configuration to set
     */
    public void setArtifactConfiguration(@Nullable final SAMLArtifactConfiguration config) {
        artifactConfig = config;
    }

}