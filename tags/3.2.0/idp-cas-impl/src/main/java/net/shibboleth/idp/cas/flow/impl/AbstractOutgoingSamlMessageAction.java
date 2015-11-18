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

package net.shibboleth.idp.cas.flow.impl;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.idp.cas.protocol.ProtocolError;
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.idp.cas.protocol.TicketValidationResponse;
import net.shibboleth.idp.profile.ActionSupport;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml1.core.Response;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.util.SOAPConstants;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Base class for all actions that build SAML {@link Response} messages for output.
 *
 * @author Marvin S. Addison
 */
public abstract class AbstractOutgoingSamlMessageAction extends
        AbstractCASProtocolAction<TicketValidationRequest, TicketValidationResponse> {

    /** SOAP envelope needed for old/broken CAS clients. */
    private QName ENVELOPE_NAME = new QName(SOAPConstants.SOAP11_NS, Envelope.DEFAULT_ELEMENT_LOCAL_NAME, "SOAP-ENV");

    /** SOAP body needed for old/broken CAS clients. */
    private QName BODY_NAME = new QName(SOAPConstants.SOAP11_NS, Body.DEFAULT_ELEMENT_LOCAL_NAME, "SOAP-ENV");

    /** CAS namespace. */
    protected static final String NAMESPACE = "http://www.ja-sig.org/products/cas/";

    protected static <T extends SAMLObject> T newSAMLObject(final Class<T> type, final QName elementName) {
        final SAMLObjectBuilder<T> builder = (SAMLObjectBuilder<T>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<T>getBuilderOrThrow(elementName);
        return builder.buildObject();
    }

    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {

        final MessageContext<SAMLObject> msgContext = new MessageContext<>();
        try {
            msgContext.setMessage(buildSamlResponse(springRequestContext, profileRequestContext));
        } catch (IllegalStateException e) {
            return ProtocolError.IllegalState.event(this);
        }
        final SAMLBindingContext bindingContext = new SAMLBindingContext();
        bindingContext.setBindingUri(SAMLConstants.SAML1_SOAP11_BINDING_URI);
        msgContext.addSubcontext(bindingContext);

        // Ensure message uses SOAP-ENV ns prefix required by old/broken CAS clients
        final Envelope envelope = (Envelope) XMLObjectSupport.buildXMLObject(ENVELOPE_NAME);
        envelope.setBody((Body) XMLObjectSupport.buildXMLObject(BODY_NAME));
        final SOAP11Context soapCtx = new SOAP11Context();
        soapCtx.setEnvelope(envelope);
        msgContext.addSubcontext(soapCtx);

        profileRequestContext.setOutboundMessageContext(msgContext);

        return ActionSupport.buildProceedEvent(this);
    }

    protected abstract Response buildSamlResponse(
            @Nonnull RequestContext springRequestContext,
            @Nonnull ProfileRequestContext<SAMLObject, SAMLObject> profileRequestContext);
}
