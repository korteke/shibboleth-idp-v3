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

package net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl;

import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.ext.spring.factory.AbstractComponentAwareFactoryBean;

import org.opensaml.security.x509.impl.BasicPKIXValidationInformation;

/**
 * A factory bean to collect information to do with a {@link BasicPKIXValidationInformation}.
 */
public abstract class AbstractBasicPKIXValidationInfoFactoryBean extends
        AbstractComponentAwareFactoryBean<BasicPKIXValidationInformation> {

    /** Verification depth. */
    @Nullable private Integer verifyDepth;

    /** The description of the file with the configuration us. */
    @Nonnull private String configDescription;

    /**
     * Get the verify Depth.
     * 
     * @return Returns the depth.
     */
    public int getVerifyDepth() {
        return verifyDepth;
    }

    /**
     * Set the verify Depth.
     * 
     * @param depth The value to set.
     */
    public void setVerifyDepth(final int depth) {
        verifyDepth = depth;
    }

    /**
     * For logging, get the description of the resource that defined this bean.
     * 
     * @return Returns the description.
     */
    public String getConfigDescription() {
        return configDescription;
    }

    /**
     * For logging, set the description of the resource that defined this bean.
     * 
     * @param desc what to set.
     */
    public void setConfigDescription(@Nonnull String desc) {
        configDescription = desc;
    }

    /** {@inheritDoc} */
    @Override public Class<?> getObjectType() {
        return BasicPKIXValidationInformation.class;
    }

    /** {@inheritDoc} */
    @Override protected BasicPKIXValidationInformation doCreateInstance() throws Exception {
        return new BasicPKIXValidationInformation(getCertificates(), getCRLs(), verifyDepth);
    }

    /**
     * Get the configured certificates.
     * 
     * @return the certificates.
     */
    @Nullable protected abstract List<X509Certificate> getCertificates();

    /**
     * Get the configured CRL list.
     * 
     * @return the crls or null
     */
    @Nullable protected abstract List<X509CRL> getCRLs();

}
