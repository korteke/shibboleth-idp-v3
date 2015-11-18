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

package net.shibboleth.idp.profile.spring.factory;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.cryptacular.util.CertUtil;
import org.cryptacular.util.KeyPairUtil;
import org.opensaml.security.x509.BasicX509Credential;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

/**
 * Spring bean factory for producing a {@link BasicX509Credential} from {@link Resource}s.
 * 
 * <p>This factory bean supports DER and PEM encoded certificate resources and
 * encrypted and non-encrypted PKCS8, DER, or PEM encoded private key resources.</p>
 */
public class BasicX509CredentialFactoryBean implements FactoryBean<BasicX509Credential> {

    /** Private key resource. */
    @Nullable private Resource keyResource;

    /** Password for the private key. */
    @Nullable private String keyPass;

    /** Certificate resource. */
    @Nullable private Resource certResource;
    
    /** Credential entityID. */
    @Nullable private String entityID;

    /** The singleton instance of the credential produced by this factory. */
    @Nullable private BasicX509Credential credential;

    /**
     * Set the resource containing the private key.
     * 
     * @param res private key resource, never <code>null</code>
     */
    public void setPrivateKeyResource(@Nonnull final Resource res) {
        keyResource = Constraint.isNotNull(res, "Private key resource cannot be null");
    }

    /**
     * Set the password for the private key.
     * 
     * @param password password for the private key, may be null if the key is not encrypted
     */
    public void setPrivateKeyPassword(@Nullable final String password) {
        keyPass = password;
    }
    
    /**
     * Set the certificate resource.
     * 
     * @param res certificate resource
     */
    public void setCertificateResource(@Nonnull final Resource res) {
        certResource = Constraint.isNotNull(res, "Certificate resource cannot be null");
    }

    /**
     * Set the entityID for the credential.
     * 
     * @param id entityID
     */
    public void setEntityId(@Nullable final String id) {
        entityID = StringSupport.trimOrNull(id);
    }

    /** {@inheritDoc} */
    @Override
    public BasicX509Credential getObject() throws Exception {
        if (credential == null) {
            if (certResource == null) {
                throw new BeanCreationException("Certificate resource must be provided in order to use this factory.");
            }

            X509Certificate certificate;
            try (final InputStream is = certResource.getInputStream()) {
                certificate = CertUtil.readCertificate(is);
            }
            
            if (keyResource == null) {
                throw new BeanCreationException("Private key resource must be provided in order to use this factory.");
            }

            PrivateKey key;
            try (final InputStream is = keyResource.getInputStream()) {
                if (keyPass == null) {
                    key = KeyPairUtil.readPrivateKey(is);
                } else {
                    key = KeyPairUtil.readPrivateKey(is, keyPass.toCharArray());
                }
            }
            
            credential = new BasicX509Credential(certificate, key);
            credential.setEntityId(entityID);
        }

        return credential;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public Class<?> getObjectType() {
        return BasicX509Credential.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSingleton() {
        return true;
    }
}