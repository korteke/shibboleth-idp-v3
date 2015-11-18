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
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.security.DataExpiredException;
import net.shibboleth.utilities.java.support.security.DataSealer;
import net.shibboleth.utilities.java.support.security.DataSealerException;

/**
 * An abstract action which contains the logic to do crypto transient decoding matching. This reverses the work done by
 * {@link net.shibboleth.idp.saml.attribute.resolver.impl.TransientIdAttributeDefinition} and
 * {@link CryptoTransientIdGenerationStrategy}
 */
public abstract class BaseCryptoTransientDecoder extends AbstractIdentifiableInitializableComponent {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(BaseCryptoTransientDecoder.class);
    
    /** Object used to protect and encrypt the data. */
    @NonnullAfterInit private DataSealer dataSealer;

    /** cache for the log prefix - to save multiple recalculations. */
    @Nullable private String logPrefix;

    /**
     * Get the Data Sealer we are using.
     * 
     * @return the Data Sealer we are using.
     */
    @NonnullAfterInit public DataSealer getDataSealer() {
        return dataSealer;
    }

    /**
     * Set the Data Sealer we should use.
     * 
     * @param sealer the Data Sealer to use.
     */
    public void setDataSealer(@Nonnull final DataSealer sealer) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        dataSealer = Constraint.isNotNull(sealer, "DataSealer cannot be null");
    }

    /**
     * Convert the transient Id into the principal.
     * 
     * @param transientId the encrypted transientID
     * @param requesterId the requester ID
     * 
     * @return the decoded entity.
     * @throws NameDecoderException if a decode error occurs.
     */
    @Nullable protected String decode(@Nonnull final String transientId, @Nonnull @NotEmpty final String requesterId)
            throws NameDecoderException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        if (null == transientId) {
            throw new NameDecoderException(getLogPrefix() + " Transient identifier was null");
        } else if (Strings.isNullOrEmpty(requesterId)) {
            throw new NameDecoderException(getLogPrefix() + " Requester ID was null");
        }

        final String decodedId;
        try {
            decodedId = dataSealer.unwrap(transientId);
        } catch (final DataExpiredException e) {
            throw new NameDecoderException(getLogPrefix() + " Principal identifier has expired");
        } catch (final DataSealerException e) {
            log.debug("{} Caught exception unwrapping principal identifier", getLogPrefix(), e);
            return null;
        }

        if (decodedId == null) {
            throw new NameDecoderException(getLogPrefix() + " Unable to recover principal from transient identifier: "
                    + transientId);
        }

        // Split the identifier.
        final String[] parts = decodedId.split("!");
        if (parts.length != 2) {
            log.warn("{} Decoded principal information was invalid: {}", getLogPrefix(), decodedId);
            return null;
        }

        if (requesterId != null && !requesterId.equals(parts[0])) {
            log.warn("{} Transient identifier issued to {} but requested by {}", getLogPrefix(), parts[0], requesterId);
            return null;
        }

        return parts[1];
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == dataSealer) {
            throw new ComponentInitializationException(getLogPrefix() + " no data sealer set");
        }
    }

    /**
     * Return a prefix for logging messages for this component.
     * 
     * @return a string for insertion at the beginning of any log messages
     */
    @Nonnull @NotEmpty protected String getLogPrefix() {
        // local cache of cached entry to allow unsynchronised clearing.
        String prefix = logPrefix;
        if (null == prefix) {
            StringBuilder builder = new StringBuilder("Crypto Transient Decoder '").append(getId()).append("':");
            prefix = builder.toString();
            if (null == logPrefix) {
                logPrefix = prefix;
            }
        }
        return prefix;
    }

}