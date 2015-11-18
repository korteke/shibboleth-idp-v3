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

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.nameid.NameIdentifierAttributeEncoder;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.profile.NameIdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Legacy generator of name identifier objects that relies on resolved attributes having
 * {@link NameIdentifierAttributeEncoder}s attached.
 * 
 * <p>Provided for compatibility with V2 configurations.</p>
 * 
 * <p>While in principle this generator could be configured in the V3 manner by mapping Format(s)
 * to instances of the class, this would require extra configuration to guarantee compatibility,
 * so by design it works by relying on the Format value supplied at generation time to decide
 * which attribute encoders use, in the manner the V2 IdP does.</p>
 * 
 * @param <NameIdType> type of identifier object
 */
public class LegacyNameIdentifierGenerator<NameIdType extends SAMLObject>
        extends AbstractIdentifiableInitializableComponent implements NameIdentifierGenerator<NameIdType> {
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(LegacyNameIdentifierGenerator.class);

    /** A predicate indicating whether the component applies to a request. */
    @Nonnull private Predicate<ProfileRequestContext> activationCondition;
    
    /** Lookup strategy for {@link AttributeContext}. */
    @Nonnull private Function<ProfileRequestContext, AttributeContext> attributeContextLookupStrategy;
    
    /** Type of encoder to check for when looping. */
    @Nonnull private Class<? extends NameIdentifierAttributeEncoder> encoderType;
    
    /**
     * Constructor.
     * 
     * @param clazz encoder class type
     */
    protected LegacyNameIdentifierGenerator(@Nonnull Class<? extends NameIdentifierAttributeEncoder> clazz) {
        activationCondition = Predicates.alwaysTrue();
        encoderType = Constraint.isNotNull(clazz, "Encoder class type cannot be null");

        // ProfileRequestContext -> RelyingPartyContext -> AttributeContext
        attributeContextLookupStrategy = Functions.compose(new ChildContextLookup<>(AttributeContext.class),
                new ChildContextLookup<ProfileRequestContext,RelyingPartyContext>(RelyingPartyContext.class));
    }

    /**
     * Set an activation condition that determines whether to run or not.
     * 
     * @param condition an activation condition
     */
    public void setActivationCondition(@Nonnull final Predicate<ProfileRequestContext> condition) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        activationCondition = Constraint.isNotNull(condition, "Predicate cannot be null");
    }
    
    /**
     * Set the lookup strategy to locate the {@link AttributeContext} to pull from.
     * 
     * @param strategy lookup strategy
     */
    public void setAttributeContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, AttributeContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        attributeContextLookupStrategy = Constraint.isNotNull(strategy,
                "AttributeContext lookup strategy cannot be null");
    }
    
// Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override
    @Nullable public NameIdType generate(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull @NotEmpty final String format) throws SAMLException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        Constraint.isNotNull(format, "Format cannot be null or empty");
        
        if (!activationCondition.apply(profileRequestContext)) {
            return null;
        }
        
        final AttributeContext attributeContext = attributeContextLookupStrategy.apply(profileRequestContext);
        if (attributeContext == null || attributeContext.getIdPAttributes().isEmpty()) {
            log.debug("No AttributeContext or resolved IdPAttributes found, nothing to do");
            return null;
        }
        
        for (final IdPAttribute idpAttribute : attributeContext.getIdPAttributes().values()) {
            if (idpAttribute.getValues().isEmpty()) {
                continue;
            }
            for (final AttributeEncoder encoder : idpAttribute.getEncoders()) {
                if (encoderType.isInstance(encoder) && ((NameIdentifierAttributeEncoder) encoder).apply(format)
                        && encoder.getActivationCondition().apply(profileRequestContext)) {
                    try {
                        // The encoders throw unless they return an object.
                        final NameIdType nameId = (NameIdType) encoder.encode(idpAttribute);
                        log.debug("Encoded attribute {} into name identifier with format {}", idpAttribute.getId(),
                                format);
                        return nameId;
                    } catch (AttributeEncodingException e) {
                        log.error("Error encoding IdPAttribute into name identifier", e);
                    }
                }
            }
        }
        
        log.debug("Unable to obtain name identifier from legacy attribute encoders");
        return null;
    }
// Checkstyle: CyclomaticComplexity ON

}