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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.saml.common.SAMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basis of a {@link PersistentIdGenerationStrategy} that generates a unique ID by computing the hash of
 * a given attribute value, the entity ID of the inbound message issuer, and a provided salt.
 */
public class ComputedPersistentIdGenerationStrategy extends AbstractInitializableComponent
        implements PersistentIdGenerationStrategy {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ComputedPersistentIdGenerationStrategy.class);

    /** Salt used when computing the ID. */
    @NonnullAfterInit private byte[] salt;

    /** JCE digest algorithm name to use. */
    @Nonnull @NotEmpty private String algorithm;
    
    /** Constructor. */
    public ComputedPersistentIdGenerationStrategy() {
        algorithm = "SHA";
    }
    
    /**
     * Get the salt used when computing the ID.
     * 
     * @return salt used when computing the ID
     */
    @NonnullAfterInit public byte[] getSalt() {
        return salt;
    }

    /**
     * Set the salt used when computing the ID.
     * 
     * @param newValue used when computing the ID
     */
    public void setSalt(@Nonnull @NotEmpty final byte[] newValue) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        salt = Constraint.isNotEmpty(newValue, "Salt cannot be null or empty");
    }

    /**
     * Set the JCE algorithm name of the digest algorithm to use (default is SHA).
     * 
     * @param alg JCE message digest algorithm
     */
    public void setAlgorithm(@Nonnull @NotEmpty final String alg) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        algorithm = Constraint.isNotNull(StringSupport.trimOrNull(alg), "Digest algorithm cannot be null or empty");
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (null == getSalt()) {
            throw new ComponentInitializationException("Salt cannot be null");
        }

        if (getSalt().length < 16) {
            throw new ComponentInitializationException("Salt must be at least 16 bytes in size");
        }

    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String generate(@Nonnull @NotEmpty final String assertingPartyId,
            @Nonnull @NotEmpty final String relyingPartyId, @Nonnull @NotEmpty final String principalName,
            @Nonnull @NotEmpty final String sourceId) throws SAMLException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        try {
            final MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(relyingPartyId.getBytes());
            md.update((byte) '!');
            md.update(sourceId.getBytes());
            md.update((byte) '!');

            return Base64Support.encode(md.digest(salt), Base64Support.UNCHUNKED);
        } catch (final NoSuchAlgorithmException e) {
            log.error("Digest algorithm {} is not supported", algorithm);
            throw new SAMLException("Digest algorithm was not supported, unable to compute ID", e);
        }
    }
    
}