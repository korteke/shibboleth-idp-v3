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

package net.shibboleth.idp.saml.profile.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.config.AuthenticationProfileConfiguration;
import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.profile.logic.MetadataNameIdentifierFormatStrategy;
import org.opensaml.saml.saml2.core.NameID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Function to filter a set of candidate NameIdentifier/NameID Format values derived from an entity's SAML metadata
 * against configuration preferences.
 */
public class DefaultNameIdentifierFormatStrategy extends MetadataNameIdentifierFormatStrategy {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(DefaultNameIdentifierFormatStrategy.class);

    /**
     * Strategy used to locate the {@link RelyingPartyContext} associated with a given {@link ProfileRequestContext}.
     */
    @Nonnull private Function<ProfileRequestContext, RelyingPartyContext> relyingPartyContextLookupStrategy;

    /** Default format to use if nothing else is known. */
    @Nonnull @NotEmpty private String defaultFormat;

    /** Constructor. */
    public DefaultNameIdentifierFormatStrategy() {
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
        defaultFormat = NameID.UNSPECIFIED;
    }

    /**
     * Set the strategy used to locate the {@link RelyingPartyContext} associated with a given
     * {@link ProfileRequestContext}.
     * 
     * @param strategy strategy used to locate the {@link RelyingPartyContext} associated with a given
     *            {@link ProfileRequestContext}
     */
    public void setRelyingPartyContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, RelyingPartyContext> strategy) {

        relyingPartyContextLookupStrategy =
                Constraint.isNotNull(strategy, "RelyingPartyContext lookup strategy cannot be null");
    }

    /**
     * Set the default format to return.
     * 
     * @param format default format
     */
    public void setDefaultFormat(@Nonnull @NotEmpty final String format) {
        defaultFormat =
                Constraint.isNotNull(StringSupport.trimOrNull(format), "Default format cannot be null or empty");
    }

    /** {@inheritDoc} */
    @Override @Nullable public List<String> apply(@Nullable final ProfileRequestContext input) {
        List<String> fromConfig = new ArrayList<>();
        final List<String> fromMetadata = super.apply(input);

        final RelyingPartyContext relyingPartyCtx = relyingPartyContextLookupStrategy.apply(input);
        if (relyingPartyCtx != null) {
            final ProfileConfiguration profileConfig = relyingPartyCtx.getProfileConfig();
            if (profileConfig != null && profileConfig instanceof AuthenticationProfileConfiguration) {
                fromConfig.addAll(((AuthenticationProfileConfiguration) profileConfig).getNameIDFormatPrecedence());
                log.debug("Configuration specifies the following formats: {}", fromConfig);
            } else {
                log.debug("No ProfileConfiguraton available (or not an AuthenticationProfileConfiguration)");
            }
        } else {
            log.debug("No RelyingPartyContext available");
        }

        if (fromConfig.isEmpty()) {
            if (fromMetadata.isEmpty()) {
                log.debug("No formats specified in configuration or in metadata, returning default");
                return Collections.singletonList(defaultFormat);
            } else {
                log.debug("Configuration did not specify any formats, relying on metadata alone");
                return fromMetadata;
            }
        } else if (fromMetadata.isEmpty()) {
            log.debug("Metadata did not specify any formats, relying on configuration alone");
            return fromConfig;
        } else {
            fromConfig.retainAll(fromMetadata);
            log.debug("Filtered non-metadata-supported formats from configured formats, leaving: {}", fromConfig);
            return fromConfig;
        }
    }

}