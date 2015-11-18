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

package net.shibboleth.idp.profile.config;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;

import org.opensaml.security.x509.tls.ClientTLSValidationConfiguration;
import org.opensaml.xmlsec.DecryptionConfiguration;
import org.opensaml.xmlsec.EncryptionConfiguration;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.SignatureValidationConfiguration;

/** Configuration for security behavior of profiles. */
public class SecurityConfiguration {

    /** Acceptable clock skew expressed in milliseconds. */
    @Duration @Positive private final long clockSkew;

    /** Generator used to generate various secure IDs (e.g., message identifiers). */
    @Nonnull private final IdentifierGenerationStrategy idGenerator;

    /** Configuration used when validating protocol message signatures. */
    @Nullable private SignatureValidationConfiguration sigValidateConfig;

    /** Configuration used when generating protocol message signatures. */
    @Nullable private SignatureSigningConfiguration sigSigningConfig;

    /** Configuration used when decrypting protocol message information. */
    @Nullable private DecryptionConfiguration decryptConfig;

    /** Configuration used when encrypting protocol message information. */
    @Nullable private EncryptionConfiguration encryptConfig;
    
    /** Configuration used when validating client TLS X509Credentials. */
    @Nullable private ClientTLSValidationConfiguration clientTLSConfig;

    /**
     * Constructor.
     * 
     * Initializes the clock skew to 5 minutes and the identifier generator to
     * {@link SecureRandomIdentifierGenerationStrategy} using the SHA1PRNG algorithm.
     */
    public SecurityConfiguration() {
        clockSkew = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
        idGenerator = new SecureRandomIdentifierGenerationStrategy();
    }

    /**
     * Constructor.
     * 
     * @param skew the clock skew, must be greater than 0
     * @param generator the identifier generator, must not be null
     */
    public SecurityConfiguration(@Duration @Positive final long skew,
            @Nonnull final IdentifierGenerationStrategy generator) {
        clockSkew = (int) Constraint.isGreaterThan(0, skew, "Clock skew must be greater than 0");
        idGenerator = Constraint.isNotNull(generator, "Identifier generator cannot be null");
    }

    /**
     * Get the acceptable clock skew expressed in milliseconds.
     * 
     * @return acceptable clock skew expressed in milliseconds
     */
    @Positive public long getClockSkew() {
        return clockSkew;
    }

    /**
     * Get the generator used to generate secure identifiers.
     * 
     * @return generator used to generate secure identifiers
     */
    @Nonnull public IdentifierGenerationStrategy getIdGenerator() {
        return idGenerator;
    }

    /**
     * Get the configuration used when validating protocol message signatures.
     * 
     * @return configuration used when validating protocol message signatures, or null
     */
    @Nullable public SignatureValidationConfiguration getSignatureValidationConfiguration() {
        return sigValidateConfig;
    }

    /**
     * Set the configuration used when validating protocol message signatures.
     * 
     * @param config configuration used when validating protocol message signatures, or null
     */
    public void setSignatureValidationConfiguration(@Nullable final SignatureValidationConfiguration config) {
        sigValidateConfig = config;
    }

    /**
     * Get the configuration used when generating protocol message signatures.
     * 
     * @return configuration used when generating protocol message signatures, or null
     */
    @Nullable public SignatureSigningConfiguration getSignatureSigningConfiguration() {
        return sigSigningConfig;
    }

    /**
     * Set the configuration used when generating protocol message signatures.
     * 
     * @param config configuration used when generating protocol message signatures, or null
     */
    public void setSignatureSigningConfiguration(@Nullable final SignatureSigningConfiguration config) {
        sigSigningConfig = config;
    }

    /**
     * Get the configuration used when decrypting protocol message information.
     * 
     * @return configuration used when decrypting protocol message information, or null
     */
    @Nullable public DecryptionConfiguration getDecryptionConfiguration() {
        return decryptConfig;
    }

    /**
     * Set the configuration used when decrypting protocol message information.
     * 
     * @param config configuration used when decrypting protocol message information, or null
     */
    public void setDecryptionConfiguration(@Nullable final DecryptionConfiguration config) {
        decryptConfig = config;
    }

    /**
     * Get the configuration used when encrypting protocol message information.
     * 
     * @return configuration used when encrypting protocol message information, or null
     */
    @Nullable public EncryptionConfiguration getEncryptionConfiguration() {
        return encryptConfig;
    }

    /**
     * Set the configuration used when encrypting protocol message information.
     * 
     * @param config configuration used when encrypting protocol message information, or null
     */
    public void setEncryptionConfiguration(@Nullable final EncryptionConfiguration config) {
        encryptConfig = config;
    }

    /**
     * Get the configuration used when validating client TLS X509Credentials.
     * 
     * @return configuration used when validating client TLS X509Credentials, or null
     */
    public ClientTLSValidationConfiguration getClientTLSValidationConfiguration() {
        return clientTLSConfig;
    }

    /**
     * Set the configuration used when validating client TLS X509Credentials.
     * 
     * @param config configuration used when validating client TLS X509Credentials, or null
     */
    public void setClientTLSValidationConfiguration(ClientTLSValidationConfiguration config) {
        clientTLSConfig = config;
    }
}