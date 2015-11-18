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

package edu.internet2.middleware.shibboleth.common.attribute.provider;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

/**
 * Emulation code for Scripted Attributes.
 */
public class V2SAMLProfileRequestContext {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(V2SAMLProfileRequestContext.class);

    /** The Attribute Resolution Context, used to local the Principal. */
    @Nonnull private final AttributeResolutionContext resolutionContext;

    /** Attribute Id being resolved, if any. */
    @Nullable private final String id;

    /**
     * Constructor.
     * 
     * @param attributeResolutionContext the resolution context.
     * @param attributeId the id of the attribute being resolved.
     */
    public V2SAMLProfileRequestContext(@Nonnull final AttributeResolutionContext attributeResolutionContext,
            @Nullable final String attributeId) {

        resolutionContext = Constraint.isNotNull(attributeResolutionContext, "Attribute Resolution Context was null");
        id = StringSupport.trimOrNull(attributeId);
    }

    /**
     * Get the attribute ID being resolved, if available.
     * 
     * @return attribute ID
     */
    @Nullable protected String getId() {
        return id;
    }

    /**
     * Get the name of the principal associated with this resolution.
     * 
     * @return the Principal.
     */
    public String getPrincipalName() {
        return resolutionContext.getPrincipal();
    }

    /**
     * Get the Entity Id associate with this attribute issuer.
     * 
     * @return the entityId.
     */
    public String getPeerEntityId() {
        return resolutionContext.getAttributeRecipientID();
    }

    /**
     * Get the Entity Id associate with this attribute consumer.
     * 
     * @return the entityId.
     */
    public String getLocalEntityId() {
        return resolutionContext.getAttributeIssuerID();
    }

    // All other methods are stubs

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public SAMLObject getInboundSAMLMessage() {
        unsupportedMethod("getInboundSAMLMessage");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getInboundSAMLMessageId() {
        unsupportedMethod("getInboundSAMLMessageId");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public DateTime getInboundSAMLMessageIssueInstant() {
        unsupportedMethod("getInboundSAMLMessageIssueInstant");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getInboundSAMLProtocol() {
        unsupportedMethod("getInboundSAMLProtocol");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public EntityDescriptor getLocalEntityMetadata() {
        unsupportedMethod("getLocalEntityMetadata");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public QName getLocalEntityRole() {
        unsupportedMethod("getLocalEntityRole");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public RoleDescriptor getLocalEntityRoleMetadata() {
        unsupportedMethod("getLocalEntityRoleMetadata");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public MetadataResolver getMetadataResolver() {
        unsupportedMethod("getMetadataResolver");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getOuboundSAMLMessageSigningCredential() {
        unsupportedMethod("getOuboundSAMLMessageSigningCredential");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public byte[] getOutboundMessageArtifactType() {
        unsupportedMethod("getOutboundMessageArtifactType");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public SAMLObject getOutboundSAMLMessage() {
        unsupportedMethod("getOutboundSAMLMessage");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getOutboundSAMLMessageId() {
        unsupportedMethod("getOutboundSAMLMessageId");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public DateTime getOutboundSAMLMessageIssueInstant() {
        unsupportedMethod("getOutboundSAMLMessageIssueInstant");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getOutboundSAMLProtocol() {
        unsupportedMethod("getOutboundSAMLProtocol");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Endpoint getPeerEntityEndpoint() {
        unsupportedMethod("getPeerEntityEndpoint");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public EntityDescriptor getPeerEntityMetadata() {
        unsupportedMethod("getPeerEntityMetadata");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public QName getPeerEntityRole() {
        unsupportedMethod("getPeerEntityRole");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public RoleDescriptor getPeerEntityRoleMetadata() {
        unsupportedMethod("getPeerEntityRoleMetadata");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getRelayState() {
        unsupportedMethod("getRelayState");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public SAMLObject getSubjectNameIdentifier() {
        unsupportedMethod("getSubjectNameIdentifier");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public boolean isInboundSAMLMessageAuthenticated() {
        unsupportedMethod("isInboundSAMLMessageAuthenticated");
        return false;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundSAMLMessage(SAMLObject param) {
        unsupportedMethod("setInboundSAMLMessage");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundSAMLMessageAuthenticated(boolean param) {
        unsupportedMethod("setInboundSAMLMessageAuthenticated");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundSAMLMessageId(String param) {
        unsupportedMethod("setInboundSAMLMessageId");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundSAMLMessageIssueInstant(DateTime param) {
        unsupportedMethod("setInboundSAMLMessageIssueInstant");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundSAMLProtocol(String param) {
        unsupportedMethod("setInboundSAMLProtocol");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setLocalEntityId(String param) {
        unsupportedMethod("setLocalEntityId");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setLocalEntityMetadata(EntityDescriptor param) {
        unsupportedMethod("setLocalEntityMetadata");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setLocalEntityRole(QName param) {
        unsupportedMethod("setLocalEntityRole");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setLocalEntityRoleMetadata(RoleDescriptor param) {
        unsupportedMethod("setLocalEntityRoleMetadata");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setMetadataResolver(Object param) {
        unsupportedMethod("setMetadataResolver");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundMessageArtifactType(byte[] param) {
        unsupportedMethod("setOutboundMessageArtifactType");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundSAMLMessage(SAMLObject param) {
        unsupportedMethod("setOutboundSAMLMessage");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundSAMLMessageId(String param) {
        unsupportedMethod("setOutboundSAMLMessageId");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundSAMLMessageIssueInstant(DateTime param) {
        unsupportedMethod("setOutboundSAMLMessageIssueInstant");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundSAMLMessageSigningCredential(Object param) {
        unsupportedMethod("setOutboundSAMLMessageSigningCredential");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundSAMLProtocol(String param) {
        unsupportedMethod("setOutboundSAMLProtocol");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPeerEntityEndpoint(Endpoint param) {
        unsupportedMethod("setPeerEntityEndpoint");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPeerEntityId(String param) {
        unsupportedMethod("setPeerEntityId");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPeerEntityMetadata(EntityDescriptor param) {
        unsupportedMethod("setPeerEntityMetadata");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPeerEntityRole(QName param) {
        unsupportedMethod("setPeerEntityRole");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPeerEntityRoleMetadata(RoleDescriptor param) {
        unsupportedMethod("setPeerEntityRoleMetadata");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setRelayState(String param) {
        unsupportedMethod("setRelayState");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setSubjectNameIdentifier(SAMLObject param) {
        unsupportedMethod("setSubjectNameIdentifier");

    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getCommunicationProfileId() {
        unsupportedMethod("getCommunicationProfileId");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public XMLObject getInboundMessage() {
        unsupportedMethod("getInboundMessage");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getInboundMessageIssuer() {
        unsupportedMethod("getInboundMessageIssuer");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getInboundMessageTransport() {
        unsupportedMethod("getInboundMessageTransport");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public XMLObject getOutboundMessage() {
        unsupportedMethod("getOutboundMessage");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getOutboundMessageIssuer() {
        unsupportedMethod("getOutboundMessageIssuer");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getOutboundMessageTransport() {
        unsupportedMethod("getOutboundMessageTransport");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getSecurityPolicyResolver() {
        unsupportedMethod("getSecurityPolicyResolver");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public boolean isIssuerAuthenticated() {
        unsupportedMethod("isIssuerAuthenticated");
        return false;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setCommunicationProfileId(String param) {
        unsupportedMethod("setCommunicationProfileId");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundMessage(XMLObject param) {
        unsupportedMethod("setInboundMessage");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundMessageIssuer(String param) {
        unsupportedMethod("setInboundMessageIssuer");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundMessageTransport(Object param) {
        unsupportedMethod("setInboundMessageTransport");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundMessage(XMLObject param) {
        unsupportedMethod("setOutboundMessage");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundMessageIssuer(String param) {
        unsupportedMethod("setOutboundMessageIssuer");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundMessageTransport(Object param) {
        unsupportedMethod("setOutboundMessageTransport");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setSecurityPolicyResolver(Object param) {
        unsupportedMethod("setSecurityPolicyResolver");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getPreSecurityInboundHandlerChainResolver() {
        unsupportedMethod("getPreSecurityInboundHandlerChainResolver");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPreSecurityInboundHandlerChainResolver(Object param) {
        unsupportedMethod("setPreSecurityInboundHandlerChainResolver");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getPostSecurityInboundHandlerChainResolver() {
        unsupportedMethod("getPostSecurityInboundHandlerChainResolver");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPostSecurityInboundHandlerChainResolver(Object param) {
        unsupportedMethod("setPostSecurityInboundHandlerChainResolver");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getOutboundHandlerChainResolver() {
        unsupportedMethod("getOutboundHandlerChainResolver");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundHandlerChainResolver(Object param) {
        unsupportedMethod("setOutboundHandlerChainResolver");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getProfileConfiguration() {
        unsupportedMethod("getProfileConfiguration");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getRelyingPartyConfiguration() {
        unsupportedMethod("getRelyingPartyConfiguration");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getUserSession() {
        unsupportedMethod("getUserSession");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setProfileConfiguration(Object param) {
        unsupportedMethod("setProfileConfiguration");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setRelyingPartyConfiguration(Object param) {
        unsupportedMethod("setRelyingPartyConfiguration");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setUserSession(Object param) {
        unsupportedMethod("setUserSession");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Collection getReleasedAttributes() {
        unsupportedMethod("getReleasedAttributes");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setReleasedAttributes(Collection param) {
        unsupportedMethod("setReleasedAttributes");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Collection<String> getRequestedAttributesIds() {
        unsupportedMethod("getRequestedAttributesIds");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setRequestedAttributes(Collection<String> param) {
        unsupportedMethod("setRequestedAttributes");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Map<String, Object> getAttributes() {
        unsupportedMethod("getAttributes");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setAttributes(Map<String, Object> param) {
        unsupportedMethod("setAttributes");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getPrincipalAuthenticationMethod() {
        unsupportedMethod("getPrincipalAuthenticationMethod");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPrincipalAuthenticationMethod(String param) {
        unsupportedMethod("setPrincipalAuthenticationMethod");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPrincipalName(String param) {
        unsupportedMethod("setPrincipalName");

    }

    /**
     * Emit an appropriate message when an unsupported method is called.
     * 
     * @param method the method
     */
    protected void unsupportedMethod(@Nonnull final String method) {
        if (null == getId()) {
            log.error("Template definition referenced unsupported method {}", method);
        } else {
            log.error("AttributeDefinition: '{}' called unsupported method {}", getId(), method);
        }
    }

    /** {@inheritDoc}. */
    @Override public String toString() {
        return MoreObjects.toStringHelper(V2SAMLProfileRequestContext.class).add("Id", getId())
                .add("PrincipalName", getPrincipalName()).add("PeerEntityId", getPeerEntityId())
                .add("LocalEntityId", getLocalEntityId()).toString();
    }
}
