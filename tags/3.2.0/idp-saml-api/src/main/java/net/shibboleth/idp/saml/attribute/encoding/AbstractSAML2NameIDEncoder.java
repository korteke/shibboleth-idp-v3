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

package net.shibboleth.idp.saml.attribute.encoding;

import javax.annotation.Nonnull;

import net.shibboleth.idp.saml.nameid.SAML2NameIDAttributeEncoder;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.xml.SAMLConstants;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/** Base class for {@link SAML2NameIDAttributeEncoder}s. */
public abstract class AbstractSAML2NameIDEncoder extends AbstractInitializableComponent
        implements SAML2NameIDAttributeEncoder {

    /** Condition for use of this encoder. */
    @Nonnull private Predicate<ProfileRequestContext> activationCondition;
    
    /** Constructor. */
    public AbstractSAML2NameIDEncoder() {
        activationCondition = Predicates.alwaysTrue();
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull public Predicate<ProfileRequestContext> getActivationCondition() {
        return activationCondition;
    }
    
    /**
     * Set the activation condition for this encoder.
     * 
     * @param condition condition to set
     */
    public void setActivationCondition(@Nonnull final Predicate<ProfileRequestContext> condition) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        activationCondition = Constraint.isNotNull(condition, "Activation condition cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull public final String getProtocol() {
        return SAMLConstants.SAML20P_NS;
    }
    
}