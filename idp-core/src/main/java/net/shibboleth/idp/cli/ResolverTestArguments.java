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

package net.shibboleth.idp.cli;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beust.jcommander.Parameter;

/** Command line processing for ResolverTest flow. */
public class ResolverTestArguments extends AbstractCommandLineArguments {

    /** Attribute requester identity. */
    @Parameter(names = {"-r", "--requester"}, required = true, description = "Relying party identity")
    @Nullable private String requester;

    /** Identity of attribute subject. */
    @Parameter(names = {"-n", "--principal"}, required = true, description = "Subject principal name")
    @Nullable private String principal;

    /** Index into metadata.  */
    @Parameter(names = {"-i", "--acsIndex"}, description = "AttributeConsumingService index")
    @Nullable private Integer index;

    /** Show results with a custom protocol encoding.  */
    @Parameter(names = {"--protocol"}, description = "Show results with a custom protocol encoding")
    @Nullable private String protocol;

    /** Show results with SAML 1.1 encoding. */
    @Parameter(names = {"--saml1"}, description = "Show results with SAML 1.1 encoding")
    private boolean saml1;

    /** Show results with SAML 2.0 encoding. */
    @Parameter(names = {"--saml2"}, description = "Show results with SAML 2.0 encoding")
    private boolean saml2;

    /**
     * Below are legacy options from the 2.x AACLI tool that are no longer supported.
     */
    
    /** Obsolete. */
    @Parameter(names = "--configDir", description = "This option is obsolete", hidden = true)
    @Nullable private String dummy1;
    
    /** Obsolete. */
    @Parameter(names = "--springExt", description = "This option is obsolete", hidden = true)
    @Nullable private String dummy2;

    /** Obsolete. */
    @Parameter(names = "--issuer", description = "This option is obsolete", hidden = true)
    @Nullable private String dummy3;

    /** Obsolete. */
    @Parameter(names = "--authnMethod", description = "This option is obsolete", hidden = true)
    @Nullable private String dummy4;

    
    /** {@inheritDoc} */
    @Override
    public void validate() {
        if (saml1) {
            if (saml2 || protocol != null) {
                throw new IllegalArgumentException("The saml1, saml2, and protocol options are mutually exclusive");
            }
        } else if (saml2) {
            if (saml1 || protocol != null) {
                throw new IllegalArgumentException("The saml1, saml2, and protocol options are mutually exclusive");
            }
        } else if (protocol != null) {
            if (saml1 || saml2) {
                throw new IllegalArgumentException("The saml1, saml2, and protocol options are mutually exclusive");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected StringBuilder doBuildURL(@Nonnull final StringBuilder builder) {
        
        if (getPath() == null) {
            builder.append("/profile/admin/resolvertest");
        }
        
        if (builder.toString().contains("?")) {
            builder.append('&');
        } else {
            builder.append('?');
        }
        
        try {
            builder.append("requester=").append(URLEncoder.encode(requester, "UTF-8"));
            builder.append("&principal=").append(URLEncoder.encode(principal, "UTF-8"));
            if (index != null) {
                builder.append("&acsIndex").append(index.toString());
            }
            if (saml1) {
                builder.append("&saml1");
            } else if (saml2) {
                builder.append("&saml2");
            } else if (protocol != null) {
                builder.append("&protocol=").append(URLEncoder.encode(protocol, "UTF-8"));
            }
        } catch (final UnsupportedEncodingException e) {
            // UTF-8 is a required encoding. 
        }
        
        return builder;
    }
        
}