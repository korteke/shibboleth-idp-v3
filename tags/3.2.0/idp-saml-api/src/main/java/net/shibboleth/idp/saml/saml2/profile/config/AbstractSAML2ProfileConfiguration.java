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

package net.shibboleth.idp.saml.saml2.profile.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.shibboleth.idp.saml.profile.config.AbstractSAMLProfileConfiguration;
import net.shibboleth.utilities.java.support.annotation.constraint.NonNegative;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

/** Base class for SAML 2 profile configurations. */
public abstract class AbstractSAML2ProfileConfiguration extends AbstractSAMLProfileConfiguration implements
        SAML2ProfileConfiguration {

    /** Whether encryption is optional in the face of no key, etc. */
    private boolean encryptionOptional;
    
    /** Predicate used to determine if assertions should be encrypted. */
    @Nonnull private Predicate<ProfileRequestContext> encryptAssertionsPredicate;

    /** Predicate used to determine if name identifiers should be encrypted. */
    @Nonnull private Predicate<ProfileRequestContext> encryptNameIDsPredicate;

    /** Predicate used to determine if attributes should be encrypted. */
    @Nonnull private Predicate<ProfileRequestContext> encryptAttributesPredicate;

    /** Maximum proxy count for an assertion. Default value: 0 */
    private long proxyCount;

    /** Audiences for the proxy. */
    @Nonnull @NonnullElements private Set<String> proxyAudiences;

    /**
     * Constructor.
     * 
     * @param profileId ID of the communication profile, never null or empty
     */
    public AbstractSAML2ProfileConfiguration(@Nonnull @NotEmpty final String profileId) {
        super(profileId);

        encryptionOptional = false;
        encryptAssertionsPredicate = Predicates.alwaysFalse();
        encryptNameIDsPredicate = Predicates.alwaysFalse();
        encryptAttributesPredicate = Predicates.alwaysFalse();
        proxyCount = 0;
        proxyAudiences = Collections.emptySet();
    }

    /** {@inheritDoc} */
    @Override public long getProxyCount() {
        return proxyCount;
    }

    /**
     * Set the maximum number of times an assertion may be proxied.
     * 
     * @param count maximum number of times an assertion may be proxied
     */
    public void setProxyCount(@NonNegative final long count) {
        proxyCount = Constraint.isGreaterThanOrEqual(0, count, "Proxy count must be greater than or equal to 0");
    }

    /** {@inheritDoc} */
    @Override public Collection<String> getProxyAudiences() {
        return ImmutableList.copyOf(proxyAudiences);
    }

    /**
     * Set the proxy audiences to be added to responses.
     * 
     * @param audiences proxy audiences to be added to responses
     */
    public void setProxyAudiences(@Nonnull @NonnullElements final Collection<String> audiences) {
        if (audiences == null || audiences.isEmpty()) {
            proxyAudiences = Collections.emptySet();
            return;
        }

        proxyAudiences = new HashSet<>();
        String trimmedAudience;
        for (String audience : audiences) {
            trimmedAudience = StringSupport.trimOrNull(audience);
            if (trimmedAudience != null) {
                proxyAudiences.add(trimmedAudience);
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override public boolean isEncryptionOptional() {
        return encryptionOptional;
    }
    
    /**
     * Set whether encryption is optional in the face of a missing key, etc.
     * 
     * @param flag  flag to set
     */
    public void setEncryptionOptional(final boolean flag) {
        encryptionOptional = flag;
    }

    /** {@inheritDoc} */
    @Override @Nonnull public Predicate<ProfileRequestContext> getEncryptAssertions() {
        return encryptAssertionsPredicate;
    }

    /**
     * Set the predicate used to determine if assertions should be encrypted.
     * 
     * @param predicate predicate used to determine if assertions should be encrypted
     */
    public void setEncryptAssertions(@Nonnull final Predicate<ProfileRequestContext> predicate) {
        encryptAssertionsPredicate =
                Constraint.isNotNull(predicate,
                        "Predicate to determine if assertions should be enecrypted cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nonnull public Predicate<ProfileRequestContext> getEncryptNameIDs() {
        return encryptNameIDsPredicate;
    }

    /**
     * Set the predicate used to determine if name identifiers should be encrypted.
     * 
     * @param predicate predicate used to determine if name identifiers should be encrypted
     */
    public void setEncryptNameIDs(@Nonnull final Predicate<ProfileRequestContext> predicate) {
        encryptNameIDsPredicate =
                Constraint.isNotNull(predicate,
                        "Predicate to determine if name identifiers should be encrypted cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nonnull public Predicate<ProfileRequestContext> getEncryptAttributes() {
        return encryptAttributesPredicate;
    }

    /**
     * Set the predicate used to determine if attributes should be encrypted.
     * 
     * @param predicate predicate used to determine if attributes should be encrypted
     */
    public void setEncryptAttributes(@Nonnull final Predicate<ProfileRequestContext> predicate) {
        encryptAttributesPredicate =
                Constraint.isNotNull(predicate,
                        "Predicate to determine if attributes should be encrypted cannot be null");
    }

}