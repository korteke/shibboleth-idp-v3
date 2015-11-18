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

package net.shibboleth.idp.saml.profile.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.context.SpringRequestContext;
import net.shibboleth.idp.saml.binding.BindingDescriptor;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.ThreadSafeAfterInit;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.encoder.MessageEncoder;
import org.opensaml.profile.action.MessageEncoderFactory;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * A factory that returns the correct {@link MessageEncoder} to use based on an input collection of
 * descriptors that map to a Spring bean ID.
 */
@ThreadSafeAfterInit
public class SpringAwareMessageEncoderFactory extends AbstractInitializableComponent implements MessageEncoderFactory {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SpringAwareMessageEncoderFactory.class);
    
    /** Map of bindings to descriptors. */
    @Nonnull @NonnullElements private ListMultimap<String,BindingDescriptor> bindingMap;
    
    /** Constructor. */
    public SpringAwareMessageEncoderFactory() {
        bindingMap = ArrayListMultimap.create();
    }
    
    /**
     * Set the bindings to evaluate for use, in preference order.
     * 
     * @param bindings bindings to consider
     */
    public void setBindings(@Nonnull @NonnullElements final List<BindingDescriptor> bindings) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(bindings, "Binding descriptor list cannot be null");
        
        bindingMap.clear();
        for (final BindingDescriptor binding : bindings) {
            if (binding != null && binding.getId() != null) {
                bindingMap.put(binding.getId(), binding);
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    @Nullable public MessageEncoder getMessageEncoder(@Nonnull final ProfileRequestContext profileRequestContext) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        final SpringRequestContext springContext = profileRequestContext.getSubcontext(SpringRequestContext.class);
        if (springContext == null || springContext.getRequestContext() == null) {
            log.warn("No outbound message context, unable to lookup message encoder");
            return null;
        } else if (profileRequestContext.getOutboundMessageContext() == null) {
            log.warn("No outbound message context, unable to lookup message encoder");
            return null;
        }
        
        final SAMLBindingContext bindingContext =
                profileRequestContext.getOutboundMessageContext().getSubcontext(SAMLBindingContext.class);
        if (bindingContext == null || bindingContext.getBindingUri() == null) {
            log.warn("Binding URI was not available, unable to lookup message encoder");
            return null;
        }
        
        log.debug("Looking up message encoder based on binding URI: {}", bindingContext.getBindingUri());
    
        final List<BindingDescriptor> bindings = bindingMap.get(bindingContext.getBindingUri());
        for (final BindingDescriptor binding : bindings) {
            if (binding.getEncoderBeanId() != null) {
                try {
                    return springContext.getRequestContext().getActiveFlow().getApplicationContext().getBean(
                            binding.getEncoderBeanId(), MessageEncoder.class);
                } catch (final BeansException e) {
                    log.warn("Error instantiating message encoder from bean ID {}", binding.getEncoderBeanId(), e);
                }
            }
        }
        
        log.warn("Failed to find a message encoder based on binding URI: {}", bindingContext.getBindingUri());
        return null;
    }

}