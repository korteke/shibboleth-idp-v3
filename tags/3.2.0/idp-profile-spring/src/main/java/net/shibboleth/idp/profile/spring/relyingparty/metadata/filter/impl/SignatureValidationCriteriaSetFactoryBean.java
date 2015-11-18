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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.Criterion;

import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.SignatureValidationConfiguration;
import org.opensaml.xmlsec.SignatureValidationParameters;
import org.opensaml.xmlsec.SignatureValidationParametersResolver;
import org.opensaml.xmlsec.criterion.SignatureValidationConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureValidationParametersResolver;
import org.opensaml.xmlsec.signature.support.SignatureValidationParametersCriterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean impl for producing a {@link CriteriaSet} instance specialized for signature validation use
 * cases, such as input to the {@link org.opensaml.saml.metadata.resolver.filter.impl.SignatureValidationFilter}.
 * 
 * <p>
 * The supplied list of {@link SignatureValidationConfiguration} will be resolved into
 * an instance of {@link SignatureValidationParameters} and returned in the criteria set.
 * If {@link #includeOpenSAMLGlobalConfig} is true, then the OpenSAML global configuration supplied
 * from {@link SecurityConfigurationSupport#getGlobalSignatureValidationConfiguration()} will
 * be effectively added at the lowest order of precedence.
 * Resolution will be performed using the supplied instance of {@link SignatureValidationParametersResolver},
 * or if not supplied then an instance of {@link BasicSignatureValidationParametersResolver}.
 * </p>
 * 
 */
public class SignatureValidationCriteriaSetFactoryBean implements FactoryBean<CriteriaSet> {
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(SignatureValidationCriteriaSetFactoryBean.class);
    
    /** Ordered collection of {@link SignatureValidationConfiguration}. */
    @Nullable private List<SignatureValidationConfiguration> signatureValidationConfigs;
    
    /** The optional parameters resolver to use. */
    @Nullable private SignatureValidationParametersResolver signatureValidationResolver;
    
    /** Other supplied criteria. */
    @Nullable private Collection<Criterion> otherCriteria;
    
    /** Flag whether to include the OpenSAML global library-wide SignatureValidationConfiguration by default. */
    private boolean includeOpenSAMLGlobalConfig = true;

    /**
     * Set the list of signature validation configuration.
     * @param newConfigs the list of configurations
     */
    public void setSignatureValidationConfigurations(
            @Nullable final List<SignatureValidationConfiguration> newConfigs) {
        signatureValidationConfigs = newConfigs;
    }

    /**
     * Set the parameters resolver instance to use.
     * 
     * <p>If not supplied, an instance of {@link BasicSignatureValidationParametersResolver} will be used.<p>
     * 
     * @param newResolver the parameters resolver
     */
    public void setSignatureValidationParametersResolver(
            @Nullable final SignatureValidationParametersResolver newResolver) {
        signatureValidationResolver = newResolver;
    }

    /**
     * Other optional criteria to add to the returned set.
     * @param newCriteria the collection of criteria
     */
    public void setOtherCriteria(@Nullable final Collection<Criterion> newCriteria) {
        otherCriteria = newCriteria;
    }

    /**
     * Flag whether to effectively include the OpenSAML library-wide default SignatureValidationConfiguration in 
     * the list of configurations to process. If true, the config will be added at the lowest order of precedence.
     * 
     * @param flag the flag value
     */
    public void setIncludeOpenSAMLGlobalConfig(boolean flag) {
        includeOpenSAMLGlobalConfig = flag;
    }

    /** {@inheritDoc} */
    public Class<?> getObjectType() {
        return CriteriaSet.class;
    }

    /** {@inheritDoc} */
    public boolean isSingleton() {
        return false;
    }
    
    /** {@inheritDoc} */
    public CriteriaSet getObject() throws Exception {
        log.debug("Building CriteriaSet based on factory bean inputs");
        CriteriaSet criteriaSet = new CriteriaSet();
        
        if (otherCriteria != null && !otherCriteria.isEmpty()) {
            log.debug("Added generic collection of other criteria");
            criteriaSet.addAll(otherCriteria);
        }
        
        List<SignatureValidationConfiguration> sigConfigs = new ArrayList<>();
        if (signatureValidationConfigs != null && !signatureValidationConfigs.isEmpty()) {
            sigConfigs.addAll(signatureValidationConfigs);
        }
        if (includeOpenSAMLGlobalConfig) {
            sigConfigs.add(SecurityConfigurationSupport.getGlobalSignatureValidationConfiguration());
        }
        
        if (!sigConfigs.isEmpty()) {
            log.debug("Resolving SignatureValidationParameters from supplied SignatureValidationConfigurations");
            if (criteriaSet.contains(SignatureValidationParametersCriterion.class)) {
                log.warn("Supplied criteria contained already an instance of " 
                        + "SignatureValidationParametersCriterion, " 
                        + "it will be replaced by the one to be resolved");
            }
            
            SignatureValidationParametersResolver paramsResolver = signatureValidationResolver;
            if (paramsResolver == null) {
                paramsResolver = new BasicSignatureValidationParametersResolver();
            }
            
            CriteriaSet configCriteria = new CriteriaSet(
                    new SignatureValidationConfigurationCriterion(sigConfigs));
            SignatureValidationParameters params = paramsResolver.resolveSingle(configCriteria);
            
            if (params != null) {
                criteriaSet.add(new SignatureValidationParametersCriterion(params), true);
            }
        }
        
        return criteriaSet;
    }

}
