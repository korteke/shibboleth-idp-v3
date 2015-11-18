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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.OutboundMessageContextLookup;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * Action that prepares an outbound {@link MessageContext} and related SAML contexts
 * in the event that they are not already prepared, to allow error responses to be
 * generated in the case of synchronous bindings (i.e., SOAP).
 * 
 * <p>This is a "make-up" action that fills in missing information that may not have
 * been populated in the course of normal processing, if an error occurs early in
 * profile processing. It does nothing if an outbound message context already
 * exists.</p>
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#INVALID_MSG_CTX}
 * 
 * @post <pre>ProfileRequestContext.getOutboundMessageContext() != null</pre>
 */
public class InitializeOutboundMessageContextForError extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(InitializeOutboundMessageContextForError.class);

    /** Strategy function for access to {@link SAMLBindingContext} to populate. */
    @Nonnull private Function<ProfileRequestContext,SAMLBindingContext> bindingContextLookupStrategy;
    
    /** Relying party context lookup strategy. */
    @Nonnull private Function<ProfileRequestContext,RelyingPartyContext> relyingPartyContextLookupStrategy;
    
    /** Outbound binding to use. */
    @NonnullAfterInit private String outboundBinding;
    
    /** The {@link SAMLPeerEntityContext} to base the outbound context on, if any. */
    @Nullable private SAMLPeerEntityContext peerEntityCtx;
    
    /** Constructor. */
    public InitializeOutboundMessageContextForError() {
        
        // Default: outbound msg context -> SAMLBindingContext
        bindingContextLookupStrategy = Functions.compose(
                new ChildContextLookup<>(SAMLBindingContext.class, true), new OutboundMessageContextLookup());
        
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
    }
    
    /**
     * Set lookup strategy for {@link SAMLBindingContext} to populate.
     * 
     * @param strategy  lookup strategy
     */
    public void setBindingContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,SAMLBindingContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        bindingContextLookupStrategy = Constraint.isNotNull(strategy,
                "SAMLBindingContext lookup strategy cannot be null");
    }
    
    /**
     * Set the relying party context lookup strategy.
     * 
     * @param strategy lookup strategy
     */
    public void setRelyingPartyContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,RelyingPartyContext> strategy) {
        relyingPartyContextLookupStrategy =
                Constraint.isNotNull(strategy, "RelyingPartyContext lookup strategy cannot be null");
    }
    
    /**
     * Set the outbound binding to use for the error response.
     * 
     * @param binding   binding identifier
     */
    public void setOutboundBinding(@Nonnull @NotEmpty final String binding) {
        outboundBinding = Constraint.isNotNull(StringSupport.trimOrNull(binding),
                "Outbound binding URI cannot be null or empty");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (outboundBinding == null) {
            throw new ComponentInitializationException("Outbound binding URI cannot be null");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        
        if (profileRequestContext.getOutboundMessageContext() != null) {
            log.debug("{} Outbound message context already exists, nothing to do", getLogPrefix());
            return false;
        }
        
        final RelyingPartyContext relyingPartyCtx = relyingPartyContextLookupStrategy.apply(profileRequestContext);
        if (relyingPartyCtx != null) {
            final BaseContext identifyingCtx = relyingPartyCtx.getRelyingPartyIdContextTree();
            if (identifyingCtx != null && identifyingCtx instanceof SAMLPeerEntityContext) {
                peerEntityCtx = (SAMLPeerEntityContext) identifyingCtx;
            }
        }
        
        return super.doPreExecute(profileRequestContext);
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        // Create outbound context if it doesn't exist.
        final MessageContext msgCtx = new MessageContext();
        profileRequestContext.setOutboundMessageContext(msgCtx);

        // Locate/create the binding context to populate
        final SAMLBindingContext bindingCtx = bindingContextLookupStrategy.apply(profileRequestContext);
        if (bindingCtx == null) {
            log.error("{} Unable to locate/create SAMLBindingContext to populate", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_MSG_CTX);
            return;
        }
        
        if (profileRequestContext.getInboundMessageContext() != null) {
            bindingCtx.setRelayState(SAMLBindingSupport.getRelayState(
                    profileRequestContext.getInboundMessageContext()));
        }
        bindingCtx.setBindingUri(outboundBinding);
                
        // Copy SAML peer context and metadata if it exists.
        if (peerEntityCtx != null) {
            final SAMLPeerEntityContext peerContext = msgCtx.getSubcontext(SAMLPeerEntityContext.class, true);
            peerContext.setEntityId(peerEntityCtx.getEntityId());
            
            final SAMLMetadataContext inboundMetadataCtx = peerEntityCtx.getSubcontext(SAMLMetadataContext.class);
            if (inboundMetadataCtx != null) {
                final SAMLMetadataContext outboundMetadataCtx =
                        peerContext.getSubcontext(SAMLMetadataContext.class, true);
                outboundMetadataCtx.setEntityDescriptor(inboundMetadataCtx.getEntityDescriptor());
                outboundMetadataCtx.setRoleDescriptor(inboundMetadataCtx.getRoleDescriptor());
            }
        }

        log.debug("{} Initialized outbound message context for error delivery", getLogPrefix());
    }

}