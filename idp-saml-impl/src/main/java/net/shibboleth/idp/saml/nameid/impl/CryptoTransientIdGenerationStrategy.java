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

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.security.DataSealer;
import net.shibboleth.utilities.java.support.security.DataSealerException;

import org.opensaml.saml.common.SAMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates transients using a {@link DataSealer} to encrypt the result into a recoverable value,
 * for use with stateless clustering.
 */
public class CryptoTransientIdGenerationStrategy extends AbstractIdentifiableInitializableComponent
        implements TransientIdGenerationStrategy {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(CryptoTransientIdGenerationStrategy.class);

    /** Object used to protect and encrypt the data. */
    @NonnullAfterInit private DataSealer dataSealer;

    /** Length, in milliseconds, tokens are valid. */
    @Duration @Positive private long idLifetime;

    /** Constructor. */
    public CryptoTransientIdGenerationStrategy() {
        idLifetime = 1000 * 60 * 60 * 4;
    }

    /**
     * Set the data sealer to use.
     * 
     * @param sealer object used to protect and encrypt the data
     */
    public void setDataSealer(@Nonnull final DataSealer sealer) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        dataSealer = Constraint.isNotNull(sealer, "DataSealer cannot be null");
    }
    
    /**
     * Get the time, in milliseconds, ids are valid.
     * 
     * @return  time, in milliseconds, ids are valid
     */
    @Positive public long getIdLifetime() {
        return idLifetime;
    }

    /**
     * Set the time, in milliseconds, ids are valid.
     * 
     * @param lifetime time, in milliseconds, ids are valid
     */
    public void setIdLifetime(@Duration @Positive final long lifetime) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        idLifetime = Constraint.isGreaterThan(0, lifetime, "ID lifetime must be positive");
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == dataSealer) {
            throw new ComponentInitializationException("DataSealer cannot be null");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String generate(@Nonnull @NotEmpty final String relyingPartyId,
            @Nonnull @NotEmpty final String principalName) throws SAMLException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        final StringBuilder principalTokenIdBuilder = new StringBuilder();
        principalTokenIdBuilder.append(relyingPartyId).append("!").append(principalName);

        try {
            return dataSealer.wrap(principalTokenIdBuilder.toString(), System.currentTimeMillis() + idLifetime);
        } catch (final DataSealerException e) {
            throw new SAMLException("Exception wrapping principal identifier", e);
        }
    }

}