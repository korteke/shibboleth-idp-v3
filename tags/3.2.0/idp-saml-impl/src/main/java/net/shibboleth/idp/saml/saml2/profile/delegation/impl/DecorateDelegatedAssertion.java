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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.saml2.profile.delegation.DelegationContext;
import net.shibboleth.utilities.java.support.annotation.Prototype;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.openliberty.xmltooling.disco.MetadataAbstract;
import org.openliberty.xmltooling.disco.ProviderID;
import org.openliberty.xmltooling.disco.SecurityContext;
import org.openliberty.xmltooling.disco.SecurityMechID;
import org.openliberty.xmltooling.disco.ServiceType;
import org.openliberty.xmltooling.security.Token;
import org.openliberty.xmltooling.soapbinding.Framework;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventException;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.KeyInfoConfirmationDataType;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.profile.SAML2ActionSupport;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.soap.wsaddressing.Address;
import org.opensaml.soap.wsaddressing.EndpointReference;
import org.opensaml.soap.wsaddressing.Metadata;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorManager;
import org.opensaml.xmlsec.keyinfo.NamedKeyInfoGeneratorManager;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * A profile action which decorates instances of {@link Assertion} appropriately for use as delegation tokens.
 * 
 * <p>
 * An instance of {@link DelegationContext} is resolved via the strategy set via 
 * {@link #setDelegationContextLookupStrategy(Function)}.  If no delegation context is found
 * or if {@link DelegationContext#isIssuingDelegatedAssertion()} is false, then no decoration
 * occurs.
 * </p>
 * 
 * <p>
 * The decoration consists of 3 primary parts:
 * <ol>
 * <li>
 * A holder-of-key {@link SubjectConfirmation} is added to the assertion's {@link Subject}. The credentials used
 * are taken from {@link DelegationContext#getSubjectConfirmationCredentials()}.
 * </li>
 * <li>
 * An additional {@link Audience} is added to the assertion condition {@link AudienceRestriction}, indicating
 * the IdP's own entityID as an acceptable audience.  The IdP entityID is resolved from the active
 * {@link RelyingPartyContext}, which is resolved via the strategy set by 
 * {@link #setRelyingPartyContextLookupStrategy(Function)}.
 * </li>
 * <li>
 * An additional {@link Attribute} is added to the assertion's {@link AttributeStatement} containing an
 * {@link EndpointReference}, indicating the location and other info necessary for the recipient to present
 * the delegated assertion at the IdP for delegated SSO.  The attribute name is a URI type with name
 * {@link LibertyConstants#SERVICE_TYPE_SSOS}. The endpoint URL is either set directly on this action via
 * {@link #setLibertySSOSEndpointURL(String)}, or is resolved via the strategy 
 * {@link #setLibertySSOSEndpointURLLookupStrategy(Function)}.
 * </li>
 * </ol>
 * </p>
 * 
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 */
@Prototype
public class DecorateDelegatedAssertion extends AbstractProfileAction {
    
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DecorateDelegatedAssertion.class);
    
    // Configured data
    
    /** The URL at which the IdP will accept Liberty ID-WSF SSOS requests. */
    private String libertySSOSEndpointURL;
    
    /** The strategy used to resolve the URL at which the IdP will accept Liberty ID-WSF SSOS requests. */
    @Nullable private Function<Pair<ProfileRequestContext, HttpServletRequest>,String> 
        libertySSOSEndpointURLLookupStrategy;
    
    /** Strategy used to lookup the RelyingPartyContext. */
    @Nonnull private Function<ProfileRequestContext,RelyingPartyContext> relyingPartyContextLookupStrategy;
    
    /** Strategy used to lookup the {@link DelegationContext. */
    @Nonnull private Function<ProfileRequestContext, DelegationContext> delegationContextLookupStrategy;
    
    /** Strategy used to locate the {@link Assertion}s on which to operate. */
    @Nonnull private Function<ProfileRequestContext,List<Assertion>> assertionLookupStrategy;
    
    /** The manager used to generate KeyInfo instances from Credentials. */
    @Nonnull private NamedKeyInfoGeneratorManager keyInfoGeneratorManager;
    
    
    // Runtime data
    
    /** The delegation context instance to be populated. */
    private DelegationContext delegationContext;
    
    /** The list of assertions on which to operate. */
    private List<Assertion> assertions;
    
    /** The current RelyingPartyContext. */
    private RelyingPartyContext relyingPartyContext;
    
    /** The entityID of the local responder entity. */
    private String responderId;
    
    /** The entityID of the SAML relying party. */
    private String relyingPartyId;
    
    /** Constructor. */
    public DecorateDelegatedAssertion() {
        super();
        
        libertySSOSEndpointURLLookupStrategy = new LibertySSOSEndpointURLStrategy();
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
        delegationContextLookupStrategy = new ChildContextLookup<>(DelegationContext.class);
        assertionLookupStrategy = new AssertionStrategy();
        
    }
    
    /**
     * Set the statically-configured URL at which the IdP will accept Liberty ID-WSF SSOS requests. 
     * 
     * @param url the Liberty ID-WSF SSOS endpoint URL, or null
     */
    public void setLibertySSOSEndpointURL(@Nullable final String url) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        libertySSOSEndpointURL = StringSupport.trimOrNull(url);
    }
    
    /**
     * Set strategy used to resolve the URL at which the IdP will accept Liberty ID-WSF SSOS requests. 
     * 
     * @param strategy the Liberty ID-WSF SSOS endpoint URL lookup strategy, or null
     */
    public void setLibertySSOSEndpointURLLookupStrategy(
            @Nullable final Function<Pair<ProfileRequestContext, HttpServletRequest>,String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        libertySSOSEndpointURLLookupStrategy = strategy;
    }
    
    /**
     * Set the strategy used to locate the current {@link RelyingPartyContext}.
     * 
     * @param strategy strategy used to locate the current {@link RelyingPartyContext}
     */
    public void setRelyingPartyContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, RelyingPartyContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        relyingPartyContextLookupStrategy = Constraint.isNotNull(strategy, 
                "RelyingPartyContext lookup strategy may not be null");
    }
    
    /**
     * Set the strategy used to locate the current {@link DelegationContext}.
     * 
     * @param strategy strategy used to locate the current {@link DelegationContext}
     */
    public void setDelegationContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, DelegationContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        delegationContextLookupStrategy = Constraint.isNotNull(strategy, 
                "DelegationContext lookup strategy may not be null");
    }
    
    /**
     * Set the strategy used to locate the {@link Assertion} to operate on.
     * 
     * @param strategy strategy used to locate the {@link Assertion} to operate on
     */
    public void setAssertionLookupStrategy(@Nonnull final Function<ProfileRequestContext,List<Assertion>> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        assertionLookupStrategy = Constraint.isNotNull(strategy, "Assertion lookup strategy may not be null");
    }
    
    /**
     * Set the {@link KeyInfoGeneratorManager} instance used to generate {@link KeyInfo}
     * from {@link Credential}.
     * 
     * @param manager the manager instance to use
     */
    public void setKeyInfoGeneratorManager(@Nonnull final NamedKeyInfoGeneratorManager manager) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        keyInfoGeneratorManager = Constraint.isNotNull(manager, "NamedKeyInfoGeneratorManager may not be null");
    }
    
    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (keyInfoGeneratorManager == null) {
            throw new ComponentInitializationException("KeyInfoGeneratorManager may not be null");
        }
        if (libertySSOSEndpointURL == null && libertySSOSEndpointURLLookupStrategy == null) {
            throw new ComponentInitializationException("Either Liberty SSOS endpoint URL " 
                    + "or its lookup strategy must be non-null");
        }
    }

    /** {@inheritDoc} */
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        // Eval the activation condition first.  Don't bother with the rest if false, esp since
        // could terminate with a fatal error unnecessarily.
        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        assertions = assertionLookupStrategy.apply(profileRequestContext);
        if (assertions == null || assertions.isEmpty()) {
            log.debug("No Assertions found to decorate, skipping further processing");
            return false;
        }
        
        if (!doPreExecuteDelegationInfo(profileRequestContext)) {
            return false;
        }
        
        if (!doPreExecuteRelyingParty(profileRequestContext)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Pre-execute actions on the delegation-specific info.
     * 
     * @param profileRequestContext the current profile request context
     * @return true iff {@link #doExecute(ProfileRequestContext)} should proceed
     */
    protected boolean doPreExecuteDelegationInfo(@Nonnull ProfileRequestContext profileRequestContext) {
        delegationContext = delegationContextLookupStrategy.apply(profileRequestContext);
        if (delegationContext == null || !delegationContext.isIssuingDelegatedAssertion()) {
            log.debug("Issuance of delegated was not indicated, skipping assertion decoration");
            return false;
        }
        
        if (delegationContext.getSubjectConfirmationCredentials() == null 
                || delegationContext.getSubjectConfirmationCredentials().isEmpty()) {
            log.warn("No subject confirmation credentials available in delegation context");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false; 
        }
        
        resolveLibertySSOSEndpointURL(profileRequestContext);
        if (libertySSOSEndpointURL == null) {
            log.warn("No Liberty SSOS endpoint URL was available");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false; 
        }
        
        return true;
    }
    
    /**
     * Pre-execute actions on the relying party context info.
     * 
     * @param profileRequestContext the current profile request context
     * @return true iff {@link #doExecute(ProfileRequestContext)} should proceed
     */
    protected boolean doPreExecuteRelyingParty(@Nonnull ProfileRequestContext profileRequestContext) {
        relyingPartyContext = relyingPartyContextLookupStrategy.apply(profileRequestContext);
        if (relyingPartyContext == null) {
            log.warn("No RelyingPartyContext was available");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false; 
        }
        
        relyingPartyId = relyingPartyContext.getRelyingPartyId();
        if (relyingPartyId == null) {
            log.warn("No relying party ID was available");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false; 
        }
        
        // This is @Nonnull
        responderId = relyingPartyContext.getConfiguration().getResponderId();
        
        return true;
    }

    /** {@inheritDoc} */
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        try {
            log.debug("Decorating assertion for use as delegated token");
            decorateDelegatedAssertion(profileRequestContext);
        } catch (EventException e) {
            if (Objects.equals(EventIds.PROCEED_EVENT_ID, e.getEventID())) {
                log.debug("Decoration of Assertion for delegation terminated with explicit proceed signal");
            } else {
                log.warn("Decoration of Assertion for delegation terminated with explicit non-proceed signal", e);
                ActionSupport.buildEvent(profileRequestContext, e.getEventID());
            }
        }
    }
    
    /**
     * Resolve and store the effective Liberty SSOS endpoint URL to use.
     * 
     * @param profileRequestContext  the current request context
     * 
     */
    private void resolveLibertySSOSEndpointURL(ProfileRequestContext profileRequestContext) {
        if (libertySSOSEndpointURL != null) {
            log.debug("Using explicitly configured Liberty SSOS endpoint URL: {}", libertySSOSEndpointURL);
            return;
        }
        if (libertySSOSEndpointURLLookupStrategy != null) {
            libertySSOSEndpointURL = libertySSOSEndpointURLLookupStrategy.apply(
                    new Pair<>(profileRequestContext, getHttpServletRequest()));
            if (libertySSOSEndpointURL != null) {
                log.debug("Using Liberty SSOS endpoint URL resolved via strategy: {}", libertySSOSEndpointURL);
                return;
            } else {
                log.debug("Liberty SSOS endpoint URL strategy was unable to resolve a value");
            }
        }
        log.debug("No effective Liberty SSOS endpoint URL could be determined");
    }
    
    /**
     * Decorate the Assertion to allow use as a delegated security token by the SAML requester.
     * 
     * @param requestContext the current request context
     */
    private void decorateDelegatedAssertion(@Nonnull final ProfileRequestContext requestContext) {
        for (Assertion assertion : assertions) {
            addSAMLPeerSubjectConfirmation(requestContext, assertion);
            addIdPAudienceRestriction(requestContext, assertion);
            addLibertySSOSEPRAttribute(requestContext, assertion);
        }
    }

    /**
     * Add Liberty SSOS service Endpoint Reference (EPR) attribute to Assertion's AttributeStatement.
     * 
     * @param requestContext the current request context
     * @param assertion the delegated assertion being issued
     */
    private void addLibertySSOSEPRAttribute(@Nonnull final ProfileRequestContext requestContext, 
            @Nonnull final Assertion assertion) {
        Attribute attribute = (Attribute) XMLObjectSupport.buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(LibertyConstants.SERVICE_TYPE_SSOS);
        attribute.setNameFormat(Attribute.URI_REFERENCE);
        attribute.getAttributeValues().add(buildLibertSSOSEPRAttributeValue(requestContext, assertion));
        
        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        AttributeStatement attributeStatement = null;
        if (attributeStatements.isEmpty()) {
            attributeStatement = 
                    (AttributeStatement) XMLObjectSupport.buildXMLObject(AttributeStatement.DEFAULT_ELEMENT_NAME);
            assertion.getAttributeStatements().add(attributeStatement);
        } else {
            attributeStatement = attributeStatements.get(0);
        }
        attributeStatement.getAttributes().add(attribute);
    }

    /**
     * Build the Liberty SSOS EPR AttributeValue object.
     * 
     * @param requestContext the current request context
     * @param assertion the delegated assertion being issued
     * 
     * @return the AttributeValue object containing the EPR
     */
    @Nonnull private XMLObject buildLibertSSOSEPRAttributeValue(@Nonnull final ProfileRequestContext requestContext, 
            @Nonnull final Assertion assertion) {
        
        Address address = (Address) XMLObjectSupport.buildXMLObject(Address.ELEMENT_NAME);
        address.setValue(libertySSOSEndpointURL);
        
        MetadataAbstract libertyAbstract = (MetadataAbstract) XMLObjectSupport.buildXMLObject(
                LibertyConstants.DISCO_ABSTRACT_ELEMENT_NAME);
        libertyAbstract.setValue(LibertyConstants.SSOS_EPR_METADATA_ABSTRACT);
        
        ServiceType serviceType = (ServiceType) XMLObjectSupport.buildXMLObject(
                LibertyConstants.DISCO_SERVICE_TYPE_ELEMENT_NAME);
        serviceType.setValue(LibertyConstants.SERVICE_TYPE_SSOS);
        
        ProviderID providerID = (ProviderID) XMLObjectSupport.buildXMLObject(
                LibertyConstants.DISCO_PROVIDERID_ELEMENT_NAME);
        providerID.setValue(responderId);
        
        Framework framework = (Framework) XMLObjectSupport.buildXMLObject(Framework.DEFAULT_ELEMENT_NAME);
        framework.setVersion("2.0");
        
        SecurityMechID securityMechID  = (SecurityMechID) XMLObjectSupport.buildXMLObject(
                LibertyConstants.DISCO_SECURITY_MECH_ID_ELEMENT_NAME);
        securityMechID.setValue(LibertyConstants.SECURITY_MECH_ID_CLIENT_TLS_PEER_SAML_V2);
        
        Token token = (Token) XMLObjectSupport.buildXMLObject(LibertyConstants.SECURITY_TOKEN_ELEMENT_NAME);
        token.setUsage(LibertyConstants.TOKEN_USAGE_SECURITY_TOKEN);
        token.setRef("#" + assertion.getID());
        
        SecurityContext securityContext = (SecurityContext) XMLObjectSupport.buildXMLObject(
                LibertyConstants.DISCO_SECURITY_CONTEXT_ELEMENT_NAME);
        securityContext.getSecurityMechIDs().add(securityMechID);
        securityContext.getTokens().add(token);
        
        Metadata metadata = (Metadata) XMLObjectSupport.buildXMLObject(Metadata.ELEMENT_NAME);
        metadata.getUnknownXMLObjects().add(libertyAbstract);
        metadata.getUnknownXMLObjects().add(serviceType);
        metadata.getUnknownXMLObjects().add(providerID);
        metadata.getUnknownXMLObjects().add(framework);
        metadata.getUnknownXMLObjects().add(securityContext);
        
        EndpointReference epr = (EndpointReference) XMLObjectSupport.buildXMLObject(EndpointReference.ELEMENT_NAME);
        epr.setAddress(address);
        epr.setMetadata(metadata);
        
        XMLObjectBuilder<XSAny> xsAnyBuilder = (XMLObjectBuilder<XSAny>) XMLObjectSupport.getBuilder(XSAny.TYPE_NAME);
        XSAny attributeValue = xsAnyBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        attributeValue.getUnknownXMLObjects().add(epr);
        
        return attributeValue;
    }

    /**
     * An an AudienceRestriction condition indicating the IdP as an acceptable Audience.
     * 
     * @param requestContext the current request context
     * @param assertion the assertion being isued
     */
    private void addIdPAudienceRestriction(@Nonnull final ProfileRequestContext requestContext, 
            @Nonnull final Assertion assertion) {
        
        SAML2ActionSupport.addConditionsToAssertion(this, assertion);
        
        List<AudienceRestriction> audienceRestrictions = assertion.getConditions().getAudienceRestrictions();
        AudienceRestriction audienceRestriction = null;
        if (audienceRestrictions.isEmpty()) {
            audienceRestriction = (AudienceRestriction) XMLObjectSupport.buildXMLObject(
                    AudienceRestriction.DEFAULT_ELEMENT_NAME);
            assertion.getConditions().getAudienceRestrictions().add(audienceRestriction);
        } else {
            audienceRestriction = audienceRestrictions.get(0);
        }
        
        // Sanity check that IdP audience has not already been added by other code.
        for (Audience audience : audienceRestriction.getAudiences()) {
            if (Objects.equals(responderId, StringSupport.trimOrNull(audience.getAudienceURI()))) {
                log.debug("Local entity ID '{}' already present in assertion AudienceRestriction set, skipping",
                        responderId);
                return;
            }
        }
        
        Audience idpAudience = (Audience) XMLObjectSupport.buildXMLObject(Audience.DEFAULT_ELEMENT_NAME);
        idpAudience.setAudienceURI(responderId);
        audienceRestriction.getAudiences().add(idpAudience);
    }

    /**
     * Add SubjectConfirmation to the Assertion Subject to allow confirmation when wielded by the SAML requester.
     * 
     * @param requestContext the current request context
     * @param assertion the assertion being issued
     */
    private void addSAMLPeerSubjectConfirmation(@Nonnull final ProfileRequestContext requestContext,
            @Nonnull final Assertion assertion) {
        
        KeyInfoConfirmationDataType scData = 
                (KeyInfoConfirmationDataType) XMLObjectSupport.getBuilder(KeyInfoConfirmationDataType.TYPE_NAME)
                .buildObject(SubjectConfirmationData.DEFAULT_ELEMENT_NAME, KeyInfoConfirmationDataType.TYPE_NAME);
        
        //TODO could support some strategy for using different named managers, rather than always the default manager.
        KeyInfoGeneratorManager kigm = keyInfoGeneratorManager.getDefaultManager();
        
        for (Credential cred : delegationContext.getSubjectConfirmationCredentials()) {
            KeyInfoGeneratorFactory kigf = kigm.getFactory(cred);
            KeyInfoGenerator kig = kigf.newInstance();
            try {
                KeyInfo keyInfo = kig.generate(cred);
                scData.getKeyInfos().add(keyInfo);
            } catch (SecurityException e) {
                log.warn("Error generating KeyInfo from peer credential: {}", e.getMessage());
                throw new EventException(EventIds.MESSAGE_PROC_ERROR, "Error generating KeyInfo from credential", e);
            }
        }
        
        NameID nameID = (NameID) XMLObjectSupport.buildXMLObject(NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue(relyingPartyId);
        nameID.setFormat(NameID.ENTITY);
        
        SubjectConfirmation sc = (SubjectConfirmation) XMLObjectSupport.buildXMLObject(
                SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        sc.setMethod(SubjectConfirmation.METHOD_HOLDER_OF_KEY);
        sc.setNameID(nameID);
        sc.setSubjectConfirmationData(scData);
        
        Subject subject = assertion.getSubject();
        if (subject==null) {
            subject = (Subject) XMLObjectSupport.buildXMLObject(Subject.DEFAULT_ELEMENT_NAME);
            assertion.setSubject(subject);
        }
        subject.getSubjectConfirmations().add(sc);
    }
    
    /**
     * Default strategy for obtaining assertion to modify.
     * 
     * <p>If the outbound context is empty, a new assertion is created and stored there. If the outbound
     * message is already an assertion, it's returned. If the outbound message is a response, then either
     * an existing or new assertion in the response is returned, depending on the action setting. If the
     * outbound message is anything else, null is returned.</p>
     */
    private class AssertionStrategy implements Function<ProfileRequestContext,List<Assertion>> {

        /** {@inheritDoc} */
        @Override
        @Nullable public List<Assertion> apply(@Nullable final ProfileRequestContext input) {
            if (input != null && input.getOutboundMessageContext() != null) {
                final Object outboundMessage = input.getOutboundMessageContext().getMessage();
                if (outboundMessage == null) {
                    log.debug("No outbound message found, nothing to decorate");
                    return Collections.emptyList();
                } else if (outboundMessage instanceof Assertion) {
                    log.debug("Found Assertion to decorate as outbound message");
                    return Collections.singletonList((Assertion) outboundMessage);
                } else if (outboundMessage instanceof Response) {
                    Response response = (Response) outboundMessage;
                    if (response.getAssertions().isEmpty()) {
                        log.debug("Outbound Response contained no Assertions, nothing to decorate");
                        return Collections.emptyList();
                    } else { 
                        for (Assertion assertion : response.getAssertions()) {
                            if (!assertion.getAuthnStatements().isEmpty()) {
                                log.debug("Found Assertion with AuthnStatement to decorate in outbound Response");
                                return Collections.singletonList(assertion);
                            }
                        }
                        log.debug("Found no Assertion with AuthnStatement in outbound Response, returning first");
                        return Collections.singletonList(response.getAssertions().get(0));
                    }
                } else {
                    log.debug("Found no Assertion to decorate");
                    return null;
                }
            } else {
                log.debug("Input ProfileRequestContext or outbound MessageContext was null");
                return null;
            }
        }
        
    }
    
    /** Strategy that builds the SSOS endpoint URL based on the current HTTP request
     * using default values for scheme, port and URI path suffix. */
    public static class LibertySSOSEndpointURLStrategy 
        implements Function<Pair<ProfileRequestContext,HttpServletRequest>, String> {
        
        /** Logger. */
        private Logger log = LoggerFactory.getLogger(LibertySSOSEndpointURLStrategy.class);

        /** {@inheritDoc} */
        @Nullable public String apply(@Nullable Pair<ProfileRequestContext, HttpServletRequest> input) {
            if (input == null) {
                log.debug("Input Pair<ProfileRequestContext,HttpServletRequest> was null");
                return null;
            }
            if (input.getSecond() != null) {
                HttpServletRequest request = input.getSecond();
                return String.format("https://%s:%s%s", request.getServerName(), 
                        LibertyConstants.DEFAULT_SSOS_ENDPOINT_URL_PORT,
                        request.getServletContext().getContextPath() 
                            + LibertyConstants.DEFAULT_SSOS_ENDPOINT_URL_RELATIVE_PATH);
            } else {
                log.debug("Input HttpServletRequest was null");
                return null;
            }
        }
    }
}
