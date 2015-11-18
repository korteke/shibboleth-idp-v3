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

package net.shibboleth.idp.authn.impl;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.AbstractExtractionAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.UsernamePasswordContext;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.utilities.java.support.collection.Pair;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;
import org.opensaml.soap.wssecurity.Password;
import org.opensaml.soap.wssecurity.Security;
import org.opensaml.soap.wssecurity.Username;
import org.opensaml.soap.wssecurity.UsernameToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO(lajoie) should we support nonce and created checks?  probably

/**
 * An authentication stage that extracts a username/password from the WSS Username/Password attached to a SOAP message.
 * As should be obvious, this assumes that the inbound message is a SOAP {@link Envelope}.
 */
public class ExtractUsernamePasswordFromWSSToken extends AbstractExtractionAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ExtractUsernamePasswordFromWSSToken.class);

    /** Inbound message to operate on. */
    @Nullable private Envelope inboundMessage;

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        final MessageContext inCtx = profileRequestContext.getInboundMessageContext();
        if (inCtx == null || !(inCtx.getMessage() instanceof Envelope)) {
            log.debug("{} Inbound message context missing or doesn't contain a SOAP Envelope", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return false;
        }
        
        inboundMessage = (Envelope) inCtx.getMessage();
        return super.doPreExecute(profileRequestContext, authenticationContext);
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        final Pair<String, String> usernamePassword = extractUsernamePassword(inboundMessage);
        if (usernamePassword == null) {
            log.debug("{} inbound message does not contain a username and password", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return;
        }

        authenticationContext.getSubcontext(UsernamePasswordContext.class, true)
                .setUsername(usernamePassword.getFirst()).setPassword(usernamePassword.getSecond());
    }

    /**
     * Extracts a username/password from the inbound message.
     * 
     * @param message the inbound message
     * 
     * @return the username and password
     */
    @Nullable private Pair<String,String> extractUsernamePassword(@Nonnull final Envelope message) {
        final UsernameToken usernameToken = getUsernameToken(message);
        if (usernameToken == null) {
            return null;
        }

        final Username username = usernameToken.getUsername();
        if (username == null) {
            log.debug("{} <UsernameToken> does not contain a <Username>", getLogPrefix());
            return null;
        }

        final List<XMLObject> passwords = usernameToken.getUnknownXMLObjects(Password.ELEMENT_NAME);
        if (passwords == null || passwords.size() == 0) {
            log.debug("{} <UsernameToken> does not contain a <Password>", getLogPrefix());
            return null;
        }

        final Iterator<XMLObject> passwordsItr = passwords.iterator();
        Password password = null;
        while (passwordsItr.hasNext()) {
            password = (Password) passwordsItr.next();
            if (password.getType() != null && !password.getType().equals(Password.TYPE_PASSWORD_TEXT)) {
                log.debug("{} Skipping password with unsupported type {}", getLogPrefix(), password.getType());
                password = null;
            }
        }

        if (password == null) {
            log.debug("{} <UsernameToken> does not contain a support <Password>", getLogPrefix());
            return null;
        }
        return new Pair<>(username.getValue(), password.getValue());
    }

    /**
     * Extracts the {@link UsernameToken} from the given {@link Envelope}.
     * 
     * @param message the message from which the token should be extracted
     * 
     * @return the extracted token
     */
    @Nullable private UsernameToken getUsernameToken(@Nonnull final Envelope message) {
        final Header header = message.getHeader();

        final List<XMLObject> securityHeaders = header.getUnknownXMLObjects(Security.ELEMENT_NAME);
        if (securityHeaders == null || securityHeaders.size() == 0) {
            log.debug("{} Inbound message does not contain <Security>", getLogPrefix());
            return null;
        }

        final List<XMLObject> usernameTokens =
                ((Security) securityHeaders.get(0)).getUnknownXMLObjects(UsernameToken.ELEMENT_NAME);
        if (usernameTokens == null || usernameTokens.size() == 0) {
            log.debug("{} Inbound message security header does not contain <UsernameToken>", getLogPrefix());
            return null;
        }

        return (UsernameToken) usernameTokens.get(0);
    }
}