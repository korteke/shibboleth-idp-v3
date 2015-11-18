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

package net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl;

import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.impl.ResourceBackedMetadataProviderParser;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.cryptacular.util.KeyPairUtil;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.UsageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;

/**
 * A factory bean to collect information to do with a {@link BasicCredential}.
 */
public abstract class AbstractBasicCredentialFactoryBean extends AbstractCredentialFactoryBean<BasicCredential> {
    
    /** Form of encoding for SecretKey info. */
    public enum SecretKeyEncoding {
       /** Raw binary encoding. */
       binary,
       /** Hexidecimal encoding. */
       hex,
       /** Base64 encoding. */
       base64
    }
    
    /** Log. */
    private final Logger log = LoggerFactory.getLogger(ResourceBackedMetadataProviderParser.class);

    /** The SecretKey algorithm. */
    @Nullable private String secretKeyAlgorithm;
    
    /** The SecretKey encoding used. */
    @Nullable private SecretKeyEncoding secretKeyEncoding = SecretKeyEncoding.base64;
    
    /**
     * Decode the SecretKey data, based on the specified encoding.
     * 
     * @param data the Secret key data
     * @return the decoded SecretKey byte array
     */
    protected byte[] decodeSecretKey(String data) {
        Constraint.isNotNull(data, "SecretKey data was null");
        switch (getSecretKeyEncoding()) {
            case binary:
                // This sort of doesn't make sense for the String input, but just assume it's UTF-8
                try {
                    return data.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // Can't actually happen, UTF-8 always supported.
                    return null;
                }
            case hex:
                return Hex.decode(data);
            case base64:
                return Base64.decodeBase64(data);
            default:
                throw new IllegalArgumentException("Saw unsupported encoding: " + getSecretKeyEncoding());
            
        }
    }

    /**
     * Decode the SecretKey data, based on the specified encoding.
     * 
     * @param data the Secret key data
     * @return the decoded SecretKey byte array
     */
    protected byte[] decodeSecretKey(byte[] data) {
        Constraint.isNotNull(data, "SecretKey data was null");
        switch (getSecretKeyEncoding()) {
            case binary:
                return data;
            case hex:
                return Hex.decode(data);
            case base64:
                return Base64.decodeBase64(data);
            default:
                throw new IllegalArgumentException("Saw unsupported encoding: " + getSecretKeyEncoding());
            
        }
    }

    /** {@inheritDoc} */
    @Override protected BasicCredential doCreateInstance() throws Exception {

        PrivateKey privateKey = getPrivateKey();
        PublicKey publicKey = getPublicKey();
        SecretKey secretKey = getSecretKey();
        final BasicCredential credential;
        
        // Asymmetric credential
        if (null != publicKey) {
            if (null == privateKey) {
                credential = new BasicCredential(publicKey);
            } else {
                if (!KeyPairUtil.isKeyPair(publicKey, privateKey)) {
                    log.error("{}: Public and private keys do not match", getConfigDescription());
                    throw new BeanCreationException("Public and private keys do not match");
                }
                credential = new BasicCredential(publicKey, privateKey);
            }
        // Symmetric credential
        } else if (null != secretKey) {
            credential = new BasicCredential(secretKey);
        } else {
            throw new BeanCreationException("Neither public key nor secret key specified");
        }
        
        if (null != getUsageType()) {
            credential.setUsageType(UsageType.valueOf(getUsageType()));
        }
        return credential;
    }

    /** {@inheritDoc} */
    @Override public Class<?> getObjectType() {
        return BasicCredential.class;
    }
    
    /**
     * Get the algorithm for the SecretKey.
     * 
     * @return Returns the SecretKey algorithm
     */
    @Nullable public String getSecretKeyAlgorithm() {
        return secretKeyAlgorithm;
    }

    /**
     * Set the algorithm for the SecretKey.
     * 
     * @param algorithm The algorithm to set.
     */
    public void setSecretKeyAlgorithm(@Nonnull String algorithm) {
        secretKeyAlgorithm = Constraint.isNotNull(StringSupport.trimOrNull(algorithm), 
                "SecretKey algorithm may not be null");
    }

    /**
     * Get the SecretKey encoding. Defaults to: base64.
     * 
     * @return the encoding
     */
    public SecretKeyEncoding getSecretKeyEncoding() {
        return secretKeyEncoding;
    }

    /**
     * Set the SecretKey encoding. Defaults to: base64
     * 
     * @param encoding the new encoding
     */
    public void setSecretKeyEncoding(@Nonnull final SecretKeyEncoding encoding) {
        secretKeyEncoding = Constraint.isNotNull(encoding, "SecretKey encoding may not be null");
    }

    /**
     * return the configured Public Key. 
     * 
     * @return the key, or none if not configured.
     */
    @Nullable protected abstract PublicKey getPublicKey();

    /**
     * Get the configured Private key.
     * 
     * @return the key or null if non configured
     */
    @Nullable protected abstract PrivateKey getPrivateKey();

    /**
     * return the configured Secret Key. 
     * 
     * @return the key, or none if not configured.
     */
    @Nullable protected abstract SecretKey getSecretKey();

}
