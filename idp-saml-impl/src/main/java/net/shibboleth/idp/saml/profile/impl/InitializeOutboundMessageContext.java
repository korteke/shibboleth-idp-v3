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
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.ResponderIdLookupFunction;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.AttributeConsumingServiceContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Action that adds an outbound {@link MessageContext} and related SAML contexts to the {@link ProfileRequestContext}
 * based on the identity of a relying party accessed via a lookup strategy, by default an immediate child of the profile
 * request context.
 * 
 * <p>
 * A {@link SAMLSelfEntityContext} is created based on the identity of the IdP, as derived by a lookup strategy. A
 * {@link SAMLPeerEntityContext} and {@link SAMLMetadataContext} are created based on the {@link SAMLPeerEntityContext}
 * that underlies the {@link RelyingPartyContext}.
 * </p>
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link IdPEventIds#INVALID_RELYING_PARTY_CTX}
 */
public class InitializeOutboundMessageContext extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(InitializeOutboundMessageContext.class);

    /** Relying party context lookup strategy. */
    @Nonnull private Function<ProfileRequestContext, RelyingPartyContext> relyingPartyContextLookupStrategy;

    /** Strategy used to obtain the self identity value. */
    @Nullable private Function<ProfileRequestContext, String> selfIdentityLookupStrategy;

    /** The {@link SAMLPeerEntityContext} to base the outbound context on. */
    @Nullable private SAMLPeerEntityContext peerEntityCtx;

    /** Constructor. */
    public InitializeOutboundMessageContext() {
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
        selfIdentityLookupStrategy = new ResponderIdLookupFunction();
    }

    /**
     * Set the relying party context lookup strategy.
     * 
     * @param strategy lookup strategy
     */
    public void setRelyingPartyContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, RelyingPartyContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        relyingPartyContextLookupStrategy =
                Constraint.isNotNull(strategy, "RelyingPartyContext lookup strategy cannot be null");
    }

    /**
     * Set the strategy used to locate the self identity value to use.
     * 
     * @param strategy lookup strategy
     */
    public void setSelfIdentityLookupStrategy(@Nonnull final Function<ProfileRequestContext, String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        selfIdentityLookupStrategy = Constraint.isNotNull(strategy, "Self identity lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        final RelyingPartyContext relyingPartyCtx = relyingPartyContextLookupStrategy.apply(profileRequestContext);
        if (relyingPartyCtx == null) {
            log.debug("{} No relying party context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_RELYING_PARTY_CTX);
            return false;
        }

        final BaseContext identifyingCtx = relyingPartyCtx.getRelyingPartyIdContextTree();
        if (identifyingCtx == null || !(identifyingCtx instanceof SAMLPeerEntityContext)) {
            log.debug("{} No SAML peer entity context found via relying party context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_RELYING_PARTY_CTX);
            return false;
        }

        peerEntityCtx = (SAMLPeerEntityContext) identifyingCtx;

        return super.doPreExecute(profileRequestContext);
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        final MessageContext msgCtx = new MessageContext();
        profileRequestContext.setOutboundMessageContext(msgCtx);

        final SAMLSelfEntityContext selfContext = msgCtx.getSubcontext(SAMLSelfEntityContext.class, true);
        selfContext.setEntityId(selfIdentityLookupStrategy.apply(profileRequestContext));

        final SAMLPeerEntityContext peerContext = msgCtx.getSubcontext(SAMLPeerEntityContext.class, true);
        peerContext.setEntityId(peerEntityCtx.getEntityId());

        final SAMLMetadataContext inboundMetadataCtx = peerEntityCtx.getSubcontext(SAMLMetadataContext.class);
        if (inboundMetadataCtx != null) {
            final SAMLMetadataContext outboundMetadataCtx = peerContext.getSubcontext(SAMLMetadataContext.class, true);
            outboundMetadataCtx.setEntityDescriptor(inboundMetadataCtx.getEntityDescriptor());
            outboundMetadataCtx.setRoleDescriptor(inboundMetadataCtx.getRoleDescriptor());
            final AttributeConsumingServiceContext acsCtx =
                    inboundMetadataCtx.getSubcontext(AttributeConsumingServiceContext.class);
            if (null != acsCtx) {
                outboundMetadataCtx.getSubcontext(AttributeConsumingServiceContext.class, true)
                        .setAttributeConsumingService(acsCtx.getAttributeConsumingService());
            }
        }

        log.debug("{} Initialized outbound message context", getLogPrefix());
    }
}