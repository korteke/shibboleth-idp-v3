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

import java.io.IOException;
import java.security.KeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.annotation.Nullable;
import javax.crypto.SecretKey;

import org.cryptacular.util.KeyPairUtil;
import org.opensaml.security.crypto.KeySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.Resource;

import com.google.common.io.Files;

/**
 * Factory bean for BasicFilesystem & BasicResourceBacked Credentials.
 */
public class BasicResourceCredentialFactoryBean extends AbstractBasicCredentialFactoryBean {

    /** log. */
    private final Logger log = LoggerFactory.getLogger(BasicResourceCredentialFactoryBean.class);

    /** Configured public key Info. */
    @Nullable private Resource publicKeyInfo;

    /** Configured private key Info. */
    @Nullable private Resource privateKeyInfo;

    /** Configured secret key Info. */
    @Nullable private Resource secretKeyInfo;

    /**
     * Get the information used to generate the public key.
     * 
     * @return Returns the info.
     */
    @Nullable public Resource getPublicKeyInfo() {
        return publicKeyInfo;
    }

    /**
     * Set the information used to generate the public key.
     * 
     * @param info The info to set.
     */
    public void setPublicKeyInfo(@Nullable Resource info) {
        publicKeyInfo = info;
    }

    /**
     * Get the information used to generate the private key.
     * 
     * @return Returns the info.
     */
    @Nullable public Resource getPrivateKeyInfo() {
        return privateKeyInfo;
    }

    /**
     * Set the information used to generate the private key.
     * 
     * @param info The info to set.
     */
    public void setPrivateKeyInfo(@Nullable Resource info) {
        privateKeyInfo = info;
    }

    /**
     * Get the information used to generate the secret key.
     * 
     * @return Returns the info.
     */
    @Nullable public Resource getSecretKeyInfo() {
        return secretKeyInfo;
    }

    /**
     * Set the information used to generate the secret key.
     * 
     * @param info The info to set.
     */
    public void setSecretKeyInfo(@Nullable Resource info) {
        secretKeyInfo = info;
    }

    /** {@inheritDoc} */
    @Override @Nullable protected PublicKey getPublicKey() {
        if (null == getPublicKeyInfo()) {
            return null;
        }
        try {
            return KeyPairUtil.readPublicKey(getPublicKeyInfo().getFile());
        } catch (IOException e) {
            log.error("{}: Could not decode public key", getConfigDescription(), e);
            throw new FatalBeanException("Could not decode public key", e);
        }
    }

    /** {@inheritDoc} */
    @Override @Nullable protected PrivateKey getPrivateKey() {
        if (null == getPrivateKeyInfo()) {
            return null;
        }
        try {
            return KeySupport.decodePrivateKey(getPrivateKeyInfo().getFile(), getPrivateKeyPassword());
        } catch (KeyException | IOException e) {
            log.error("{}: Could not decode private key", getConfigDescription(), e);
            throw new BeanCreationException("Could not decode private key", getConfigDescription(), e);
        }
    }

    /** {@inheritDoc} */
    @Override @Nullable protected SecretKey getSecretKey() {
        if (null == getSecretKeyInfo()) {
            return null;
        }
        try {
            return KeySupport.decodeSecretKey(decodeSecretKey(Files.toByteArray(getSecretKeyInfo().getFile())), 
                    getSecretKeyAlgorithm());
        } catch (KeyException | IOException e) {
            log.error("{}: Could not decode secret key", getConfigDescription(), e);
            throw new BeanCreationException("Could not decode secret key", e);
        }
    }
}
