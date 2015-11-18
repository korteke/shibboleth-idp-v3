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

package net.shibboleth.idp.saml.saml2.profile.delegation.impl;

import net.shibboleth.idp.profile.AbstractProfileAction;

import org.joda.time.DateTime;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLMessageInfoContext;
import org.opensaml.soap.wsaddressing.messaging.WSAddressingContext;
import org.opensaml.soap.wssecurity.messaging.WSSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Populate the outbound message context with data that is specific to the delegation flow.
 * 
 * @event {@link EventIds#INVALID_MSG_CTX}
 */
public class PopulateOutboundMessageContext extends AbstractProfileAction {
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(PopulateOutboundMessageContext.class);

    /** {@inheritDoc} */
    protected void doExecute(ProfileRequestContext profileRequestContext) {
        MessageContext inboundMessageContext = profileRequestContext.getInboundMessageContext();
        if (inboundMessageContext == null) {
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_MSG_CTX);
            return;
        }
        MessageContext outboundMessageContext = profileRequestContext.getOutboundMessageContext();
        if (outboundMessageContext == null) {
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_MSG_CTX);
            return;
        }
        
        // Set outbound wsa:RelatesTo based on inbound wsa:MessageID
        WSAddressingContext addressingInbound = inboundMessageContext.getSubcontext(WSAddressingContext.class);
        if (addressingInbound != null) {
            outboundMessageContext.getSubcontext(WSAddressingContext.class, true).setRelatesToURI(
                    addressingInbound.getMessageIDURI());
            log.debug("Set outbound WS-Addressing RelatesTo URL: {}", 
                    outboundMessageContext.getSubcontext(WSAddressingContext.class).getRelatesToURI());
        }
        
        // Set outbound WS-S wsu:Timestamp/wsu:Created based on either outbound SAML message issue instant (if present)
        // or current time.
        SAMLMessageInfoContext samlMsgInfoCtx = outboundMessageContext.getSubcontext(SAMLMessageInfoContext.class);
        if (samlMsgInfoCtx != null) {
            log.debug("Saw outbound SAMLMessageInfoContext with message issue instant: {}", 
                    samlMsgInfoCtx.getMessageIssueInstant());
            outboundMessageContext.getSubcontext(WSSecurityContext.class, true).setTimestampCreated(
                    samlMsgInfoCtx.getMessageIssueInstant());
        } else {
            outboundMessageContext.getSubcontext(WSSecurityContext.class, true).setTimestampCreated(new DateTime());
        }
        log.debug("Set outbound WS-Security Timestamp Created: {}", 
                outboundMessageContext.getSubcontext(WSSecurityContext.class).getTimestampCreated());
        
    }

}
