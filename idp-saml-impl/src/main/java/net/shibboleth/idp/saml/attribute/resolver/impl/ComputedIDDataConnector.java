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

package net.shibboleth.idp.saml.attribute.resolver.impl;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.saml.nameid.impl.ComputedPersistentIdGenerationStrategy;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

import org.opensaml.saml.common.SAMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * A data connector that delegates generation of IDs to a {@link ComputedPersistentIdGenerationStrategy}.
 */
public class ComputedIDDataConnector extends AbstractPersistentIdDataConnector {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ComputedIDDataConnector.class);

    /** Generation strategy for IDs. */
    @Nonnull private final ComputedPersistentIdGenerationStrategy idStrategy;
    
    /** Constructor. */
    public ComputedIDDataConnector() {
        idStrategy = new ComputedPersistentIdGenerationStrategy();
    }
    
    /**
     * Get the strategy plugin that generates computed IDs.
     * 
     * @return strategy for computing IDs
     */
    @Nonnull public ComputedPersistentIdGenerationStrategy getComputedIdStrategy() {
        return idStrategy;
    }
    
    /**
     * Get the salt used when computing the ID.
     * 
     * @return salt used when computing the ID
     */
    @Nullable public byte[] getSalt() {
        return idStrategy.getSalt();
    }

    /**
     * Set the salt used when computing the ID.
     * 
     * @param salt used when computing the ID
     */
    public void setSalt(@Nullable byte[] salt) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        if (null != salt) {
            log.debug("{} Salt set as bytes to {}", getLogPrefix(), salt.toString());
            idStrategy.setSalt(salt);
        } else {
            log.debug("{} Null salt passed, nothing set", getLogPrefix());
        }
    }
    
    /**
     * Set the salt used when computing the ID.
     * 
     * @param salt used when computing the ID
     */
    public void setSalt(@Nullable String salt) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        if (null != salt) {
            log.debug("{} Salt set as bytes to {}", getLogPrefix(), salt.toString());
            idStrategy.setSalt(salt.getBytes());
        } else {
            log.debug("{} Null salt passed, nothing set", getLogPrefix());
        }
    }


    /**
     * Set the JCE algorithm name of the digest algorithm to use (default is SHA).
     * 
     * @param alg JCE message digest algorithm
     */
    public void setAlgorithm(@Nonnull @NotEmpty final String alg) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        idStrategy.setAlgorithm(alg);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        idStrategy.initialize();
    }

    /** {@inheritDoc} */
    @Override
    protected void doDestroy() {
        idStrategy.destroy();
        
        super.doDestroy();
    }
    
    /** {@inheritDoc} */
    @Override
    @Nullable protected Map<String, IdPAttribute> doDataConnectorResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        final String principalName = resolutionContext.getPrincipal();
        if (Strings.isNullOrEmpty(principalName)) {
            log.warn("{} No principal name located, unable to compute ID", getLogPrefix());
            return null;
        }
                
        final String attributeIssuerId = resolutionContext.getAttributeIssuerID();
        if (Strings.isNullOrEmpty(attributeIssuerId)) {
            log.warn("{} No Attribute issuer ID located, unable to compute ID", getLogPrefix());
            return null;
        }
        
        final String attributeRecipientID = resolutionContext.getAttributeRecipientID();
        if (Strings.isNullOrEmpty(attributeRecipientID)) {
            log.warn("{} No Attribute Recipient ID located, unable to compute ID", getLogPrefix());
            return null;
        }
        
        final String sourceId = resolveSourceAttribute(workContext);
        if (Strings.isNullOrEmpty(sourceId)) {
            return null;
        }
        
        try {
            return encodeAsAttribute(idStrategy.generate(attributeIssuerId, attributeRecipientID, principalName,
                    sourceId));
        } catch (final SAMLException e) {
            throw new ResolutionException(e);
        }
    }

}