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

package net.shibboleth.idp.test.flows.saml2;

import java.net.MalformedURLException;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.test.flows.AbstractFlowTest;
import net.shibboleth.utilities.java.support.net.SimpleURLCanonicalizer;
import net.shibboleth.utilities.java.support.net.URLBuilder;

import org.joda.time.DateTime;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.saml.saml2.encryption.Encrypter.KeyPlacement;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.encryption.support.DataEncryptionParameters;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import org.opensaml.xmlsec.encryption.support.KeyEncryptionParameters;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract SAML 2 SSO flow test.
 */
public abstract class AbstractSAML2SSOFlowTest extends AbstractSAML2FlowTest {

    /** Class logger. */
    @Nonnull protected final Logger log = LoggerFactory.getLogger(AbstractSAML2SSOFlowTest.class);

    public String getDestinationRedirect(HttpServletRequest servletRequest) {
        // TODO servlet context
        String destinationPath = "/idp/profile/SAML2/Redirect/SSO";
        try {
            String baseUrl = SimpleURLCanonicalizer.canonicalize(getBaseUrl(servletRequest));
            URLBuilder urlBuilder = new URLBuilder(baseUrl);
            urlBuilder.setPath(destinationPath);
            return urlBuilder.buildURL();
        } catch (MalformedURLException e) {
            log.error("Couldn't parse base URL, reverting to internal default destination");
            return "http://localhost:8080" + destinationPath;
        }
    }

    public String getDestinationPost(HttpServletRequest servletRequest) {
        // TODO servlet context
        String destinationPath = "/idp/profile/SAML2/POST/SSO";
        String baseUrl = getBaseUrl(servletRequest);
        try {
            URLBuilder urlBuilder = new URLBuilder(baseUrl);
            urlBuilder.setPath(destinationPath);
            return urlBuilder.buildURL();
        } catch (MalformedURLException e) {
            log.error("Couldn't parse base URL, reverting to internal default destination: {}", baseUrl);
            return "http://localhost:8080" + destinationPath;
        }
    }

    public String getDestinationPostSimpleSign(HttpServletRequest servletRequest) {
        // TODO servlet context
        String destinationPath = "/idp/profile/SAML2/POST-SimpleSign/SSO";
        String baseUrl = getBaseUrl(servletRequest);
        try {
            URLBuilder urlBuilder = new URLBuilder(baseUrl);
            urlBuilder.setPath(destinationPath);
            return urlBuilder.buildURL();
        } catch (MalformedURLException e) {
            log.error("Couldn't parse base URL, reverting to internal default destination: {}", baseUrl);
            return "http://localhost:8080" + destinationPath;
        }
    }

    public AuthnRequest buildAuthnRequest(HttpServletRequest servletRequest) throws EncryptionException {
        final AuthnRequest authnRequest =
                (AuthnRequest) builderFactory.getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME).buildObject(
                        AuthnRequest.DEFAULT_ELEMENT_NAME);

        authnRequest.setID(idGenerator.generateIdentifier());
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setAssertionConsumerServiceURL(getAcsUrl(servletRequest));
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);

        final Issuer issuer =
                (Issuer) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME)
                        .buildObject(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(AbstractFlowTest.SP_ENTITY_ID);
        authnRequest.setIssuer(issuer);

        final NameIDPolicy nameIDPolicy =
                (NameIDPolicy) builderFactory.getBuilder(NameIDPolicy.DEFAULT_ELEMENT_NAME).buildObject(
                        NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIDPolicy.setAllowCreate(true);
        authnRequest.setNameIDPolicy(nameIDPolicy);

        final NameID nameID =
                (NameID) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME)
                        .buildObject(NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue("jdoe");

        final Subject subject =
                (Subject) builderFactory.getBuilder(Subject.DEFAULT_ELEMENT_NAME).buildObject(
                        Subject.DEFAULT_ELEMENT_NAME);
        subject.setEncryptedID(getEncrypter().encrypt(nameID));
        authnRequest.setSubject(subject);

        final RequestedAuthnContext reqAC =
                (RequestedAuthnContext) builderFactory.getBuilder(RequestedAuthnContext.DEFAULT_ELEMENT_NAME).buildObject(
                        RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        final AuthnContextClassRef ac =
                (AuthnContextClassRef) builderFactory.getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME).buildObject(
                        AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        ac.setAuthnContextClassRef(AuthnContext.UNSPECIFIED_AUTHN_CTX);
        reqAC.getAuthnContextClassRefs().add(ac);
        authnRequest.setRequestedAuthnContext(reqAC);
        
        return authnRequest;
    }

    public Encrypter getEncrypter() {
        final DataEncryptionParameters encParams = new DataEncryptionParameters();
        encParams.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
        final KeyEncryptionParameters kencParams = new KeyEncryptionParameters();
        kencParams.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
        kencParams.setEncryptionCredential(idpCredential);
        final X509KeyInfoGeneratorFactory generator = new X509KeyInfoGeneratorFactory();
        generator.setEmitEntityCertificate(true);
        kencParams.setKeyInfoGenerator(generator.newInstance());
        final Encrypter encrypter = new Encrypter(encParams, kencParams);
        encrypter.setKeyPlacement(KeyPlacement.PEER);
        return encrypter;
    }

    public String getAcsUrl(HttpServletRequest servletRequest) {
        // TODO servlet context
        String acsPath = "/sp/SAML2/POST/ACS";
        String baseUrl = getBaseUrl(servletRequest);
        try {
            URLBuilder urlBuilder = new URLBuilder(SimpleURLCanonicalizer.canonicalize(baseUrl));
            urlBuilder.setPath(acsPath);
            return urlBuilder.buildURL();
        } catch (MalformedURLException e) {
            log.error("Couldn't parse base URL, reverting to internal default ACS: {}", baseUrl);
            return "http://localhost:8080" + acsPath;
        }
    }

    public SingleSignOnService buildIdpSsoEndpoint(String binding, String destination) {
        SingleSignOnService ssoEndpoint =
                (SingleSignOnService) builderFactory.getBuilder(SingleSignOnService.DEFAULT_ELEMENT_NAME).buildObject(
                        SingleSignOnService.DEFAULT_ELEMENT_NAME);
        ssoEndpoint.setBinding(binding);
        ssoEndpoint.setLocation(destination);
        return ssoEndpoint;
    }

    public String getBaseUrl(HttpServletRequest servletRequest) {
        // TODO servlet context
        String requestUrl = servletRequest.getRequestURL().toString();
        try {
            URLBuilder urlBuilder = new URLBuilder(requestUrl);
            urlBuilder.setUsername(null);
            urlBuilder.setPassword(null);
            urlBuilder.setPath(null);
            urlBuilder.getQueryParams().clear();
            urlBuilder.setFragment(null);
            return urlBuilder.buildURL();
        } catch (MalformedURLException e) {
            log.error("Couldn't parse request URL, reverting to internal default base URL: {}", requestUrl);
            return "http://localhost:8080";
        }

    }

    public MessageContext<SAMLObject> buildOutboundMessageContext(AuthnRequest authnRequest, String bindingUri) {
        MessageContext<SAMLObject> messageContext = new MessageContext<>();
        messageContext.setMessage(authnRequest);

        SAMLPeerEntityContext peerContext = messageContext.getSubcontext(SAMLPeerEntityContext.class, true);
        peerContext.setEntityId(AbstractFlowTest.IDP_ENTITY_ID);

        SAMLEndpointContext endpointContext = peerContext.getSubcontext(SAMLEndpointContext.class, true);
        endpointContext.setEndpoint(buildIdpSsoEndpoint(bindingUri, authnRequest.getDestination()));

        SignatureSigningParameters signingParameters = new SignatureSigningParameters();
        signingParameters.setSigningCredential(spCredential);
        SecurityParametersContext secParamsContext =
                messageContext.getSubcontext(SecurityParametersContext.class, true);
        secParamsContext.setSignatureSigningParameters(signingParameters);

        return messageContext;
    }

}