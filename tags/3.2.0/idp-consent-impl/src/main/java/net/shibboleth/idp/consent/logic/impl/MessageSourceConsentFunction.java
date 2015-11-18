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

package net.shibboleth.idp.consent.logic.impl;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.consent.flow.impl.ConsentFlowDescriptor;
import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.idp.profile.context.navigate.RelyingPartyIdLookupFunction;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;

import com.google.common.base.Function;

/**
 * Function that returns a consent object whose id and value are resolved from a lookup function
 * and {@link MessageSource}.
 * 
 * <p>The lookup function's return value is a master key, mapped via the {@link MessageSource} to an ID.
 * A suffix is attached to the ID to obtain the message key of the value.</p>
 */
public class MessageSourceConsentFunction extends AbstractInitializableComponent
        implements Function<ProfileRequestContext,Map<String,Consent>>, MessageSourceAware {

    /** Lookup strategy for the consent message key to use. */
    @Nonnull private Function<ProfileRequestContext,String> consentKeyLookupStrategy;

    /** Message code suffix used to resolve the consent value. */
    @Nonnull @NotEmpty private String consentValueMessageCodeSuffix;

    /** Consent flow descriptor lookup strategy. */
    @Nonnull private Function<ProfileRequestContext,ConsentFlowDescriptor> consentFlowDescriptorLookupStrategy;

    /** Function used to create a hash of the consent value. */
    @Nonnull private Function<String,String> hashFunction;

    /** Locale lookup strategy. */
    @Nonnull private Function<ProfileRequestContext,Locale> localeLookupStrategy;

    /** MessageSource injected by Spring, typically the parent ApplicationContext itself. */
    @Nonnull private MessageSource messageSource;

    /** Constructor. */
    public MessageSourceConsentFunction() {
        consentKeyLookupStrategy = new RelyingPartyIdLookupFunction();
        consentValueMessageCodeSuffix = ".text";
        consentFlowDescriptorLookupStrategy =
                new FlowDescriptorLookupFunction<>(ConsentFlowDescriptor.class);
        hashFunction = new HashFunction();
        localeLookupStrategy = new LocaleLookupFunction();
    }

    /** {@inheritDoc} */
    @Override
    public void setMessageSource(MessageSource source) {
        messageSource = source;
    }

    /**
     * Set the lookup strategy for the consent message key.
     * 
     * @param strategy lookup strategy
     */
    public void setConsentKeyLookupStrategy(@Nonnull final Function<ProfileRequestContext,String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        consentKeyLookupStrategy = Constraint.isNotNull(strategy, "Consent key lookup strategy cannot be null");
    }

    /**
     * Set the consent value message code suffix.
     * 
     * @param suffix suffix of message code for the consent value
     */
    public void setConsentValueMessageCodeSuffix(@Nonnull @NotEmpty final String suffix) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        consentValueMessageCodeSuffix =
                Constraint.isNotNull(StringSupport.trimOrNull(suffix),
                        "Consent value message code suffix cannot be null nor empty");
    }

    /**
     * Set the consent flow descriptor lookup strategy.
     * 
     * @param strategy consent flow descriptor lookup strategy
     */
    public void setConsentFlowDescriptorLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,ConsentFlowDescriptor> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        consentFlowDescriptorLookupStrategy =
                Constraint.isNotNull(strategy, "Consent flow descriptor lookup strategy cannot be null");
    }

    /**
     * Set the hash function.
     * 
     * @param function hash function
     */
    public void setHashFunction(@Nonnull final Function<String,String> function) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        hashFunction = Constraint.isNotNull(function, "Hash function cannot be null");
    }

    /**
     * Set the locale lookup strategy.
     * 
     * @param strategy The localeLookupStrategy to set.
     */
    public void setLocaleLookupStrategy(@Nonnull final Function<ProfileRequestContext,Locale> strategy) {
        localeLookupStrategy = Constraint.isNotNull(strategy, "Locale lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public Map<String,Consent> apply(@Nullable final ProfileRequestContext input) {
        if (input == null) {
            return null;
        }
    
        final Locale locale = getLocale(input);
        final String id = getConsentId(input, locale);
        if (id != null) {
            final Consent consent = new Consent();
            consent.setId(id);
            if (isCompareValues(input)) {
                final String value = getConsentValueHash(id, locale);
                if (value != null) {
                    consent.setValue(value);
                }
            }
    
            return Collections.singletonMap(id, consent);
        } else {
            return Collections.emptyMap();
        }
    }
    

    /**
     * Get the consent id.
     * 
     * @param profileRequestContext profile request context
     * @param locale locale to use
     * 
     * @return consent id
     */
    @Nullable protected String getConsentId(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final Locale locale) {
        try {
            final String key = consentKeyLookupStrategy.apply(profileRequestContext);
            if (key != null) {
                return messageSource.getMessage(key, null, locale);
            }
        } catch (final NoSuchMessageException e) {
            
        }
        
        return null;
    }

    /**
     * Get the consent value.
     * 
     * @param consentId consent ID
     * @param locale locale to use
     * 
     * @return consent value
     */
    @Nullable protected String getConsentValue(@Nonnull @NotEmpty final String consentId,
            @Nonnull final Locale locale) {
        try {
            return messageSource.getMessage(consentId + consentValueMessageCodeSuffix, null, locale);
        } catch (final NoSuchMessageException e) {
            return null;
        }
    }

    /**
     * Get the consent value hash.
     * 
     * @param consentId consent ID
     * @param locale locale to use
     * 
     * @return consent value hash
     */
    @Nullable protected String getConsentValueHash(@Nonnull @NotEmpty final String consentId,
            @Nonnull final Locale locale) {
        return hashFunction.apply(getConsentValue(consentId, locale));
    }

    /**
     * Get the locale.
     * 
     * @param profileRequestContext profile request context
     * 
     * @return locale
     */
    @Nullable protected Locale getLocale(@Nonnull final ProfileRequestContext profileRequestContext) {
        return localeLookupStrategy.apply(profileRequestContext);
    }

    /**
     * Whether consent equality includes comparing consent values.
     * 
     * @param profileRequestContext profile request context
     * @return true if consent equality includes comparing consent values
     */
    protected boolean isCompareValues(@Nonnull final ProfileRequestContext profileRequestContext) {
        final ConsentFlowDescriptor consentFlowDescriptor =
                consentFlowDescriptorLookupStrategy.apply(profileRequestContext);
        if (consentFlowDescriptor != null) {
            return consentFlowDescriptor.compareValues();
        }

        return false;
    }
    
}