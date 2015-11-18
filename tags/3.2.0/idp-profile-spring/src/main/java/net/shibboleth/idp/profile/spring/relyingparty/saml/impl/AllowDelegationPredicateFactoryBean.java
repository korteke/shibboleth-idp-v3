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

package net.shibboleth.idp.profile.spring.relyingparty.saml.impl;

import javax.annotation.Nullable;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Factory bean for producing a delegation {@link Predicate} from multiple inputs.
 */
public class AllowDelegationPredicateFactoryBean implements FactoryBean<Predicate<ProfileRequestContext>> {
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(AllowDelegationPredicateFactoryBean.class);
    
    /** Delegation boolean attribute value. */
    private Boolean allowDelegation;
    
    /** Delegation predicate attribute bean ref. */
    private Predicate<ProfileRequestContext> allowDelegationPredicate;

    /**
     * Set the delegation boolean attribute value.
     * 
     * @param flag delegation attribute value
     */
    public void setAllowDelegation(@Nullable final Boolean flag) {
        allowDelegation = flag;
    }

    /**
     * Set the delegation predicate attribute bean ref.
     * 
     * @param predicate delegation predicate
     */
    public void setAllowDelegationPredicate(@Nullable final Predicate<ProfileRequestContext> predicate) {
        allowDelegationPredicate = predicate;
    }

    /** {@inheritDoc} */
    public Class<?> getObjectType() {
        return Predicate.class;
    }

    /** {@inheritDoc} */
    public boolean isSingleton() {
        return false;
    }
    
    /** {@inheritDoc} */
    public Predicate<ProfileRequestContext> getObject() throws Exception {
        if (allowDelegationPredicate != null) {
            if (allowDelegation != null) {
                log.warn("Attribute 'allowDelegation' is being ignored in favor of 'allowDelegationPredicateRef'");
            }
            return allowDelegationPredicate;
        } else if (allowDelegation != null) {
            if (allowDelegation) {
                return Predicates.alwaysTrue();
            } else {
                return Predicates.alwaysFalse();
            }
        } else {
            return Predicates.alwaysFalse();
        }
    }

}
