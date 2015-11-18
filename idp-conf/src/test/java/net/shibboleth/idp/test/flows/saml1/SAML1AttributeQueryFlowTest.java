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

package net.shibboleth.idp.test.flows.saml1;

import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.saml.xml.SAMLConstants;
import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

import org.joda.time.DateTime;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml1.core.AttributeDesignator;
import org.opensaml.saml.saml1.core.ConfirmationMethod;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml1.core.Request;
import org.opensaml.saml.saml1.core.StatusCode;
import org.opensaml.saml.saml1.core.Subject;
import org.opensaml.saml.saml1.core.impl.NameIdentifierBuilder;
import org.opensaml.saml.saml1.profile.SAML1ActionTestingSupport;
import org.opensaml.security.messaging.ServletRequestX509CredentialAdapter;
import org.opensaml.soap.soap11.Envelope;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * SAML 1 attribute query flow test.
 */
public class SAML1AttributeQueryFlowTest extends AbstractSAML1FlowTest {

    /** Flow id. */
    @Nonnull public final static String FLOW_ID = "SAML1/SOAP/AttributeQuery";

    /** SAML 1 Response validator. */
    @Nullable private SAML1TestResponseValidator validator;

    /** Initialize the SAML 1 Response validator. */
    @BeforeClass void setupValidator() {

        final NameIdentifier nameIdentifier = new NameIdentifierBuilder().buildObject();
        nameIdentifier.setValue("jdoe");
        nameIdentifier.setFormat(null);
        nameIdentifier.setNameQualifier(null);

        validator = new SAML1TestResponseValidator();
        validator.validateAuthenticationStatements = false;
        validator.nameIdentifier = nameIdentifier;
        validator.confirmationMethod = ConfirmationMethod.METHOD_SENDER_VOUCHES;
    }

    /**
     * Test the SAML1 Attribute Query flow.
     * 
     * @throws Exception if an error occurs
     */
    @Test public void testSAML1AttributeQueryFlow() throws Exception {

        buildRequest(false);

        request.setAttribute(ServletRequestX509CredentialAdapter.X509_CERT_REQUEST_ATTRIBUTE,
                new X509Certificate[] {certFactoryBean.getObject()});

        overrideEndStateOutput(FLOW_ID);

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        validator.statusCode = StatusCode.SUCCESS;
        validator.usedAttributeDesignators = false;

        validateResult(result, FLOW_ID, validator);
    }

    /**
     * Test the SAML1 Attribute Query flow with designators included.
     * 
     * @throws Exception if an error occurs
     */
    @Test public void testSAML1AttributeQueryFlowWithDesignators() throws Exception {

        buildRequest(true);

        request.setAttribute(ServletRequestX509CredentialAdapter.X509_CERT_REQUEST_ATTRIBUTE,
                new X509Certificate[] {certFactoryBean.getObject()});

        overrideEndStateOutput(FLOW_ID);

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        validator.statusCode = StatusCode.SUCCESS;
        validator.usedAttributeDesignators = true;

        validateResult(result, FLOW_ID, validator);
    }

    /**
     * Test the SAML1 Attribute Query flow without an SP credential.
     * 
     * @throws Exception if an error occurs
     */
    @Test public void testSAML1AttributeQueryFlowNoCredential() throws Exception {

        buildRequest(false);

        overrideEndStateOutput(FLOW_ID);

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        validator.statusCode = StatusCode.REQUESTER;
        validator.usedAttributeDesignators = false;

        validateResult(result, FLOW_ID, validator);
    }

    /**
     * Build the {@link MockHttpServletRequest}.
     * 
     * @throws Exception if an error occurs
     */
    public void buildRequest(final boolean includeDesignators) throws Exception {
        final Subject subject = SAML1ActionTestingSupport.buildSubject("jdoe");

        final Request attributeQuery = SAML1ActionTestingSupport.buildAttributeQueryRequest(subject);
        attributeQuery.setIssueInstant(new DateTime());
        attributeQuery.getAttributeQuery().setResource(SP_ENTITY_ID);
        attributeQuery.setID(new SecureRandomIdentifierGenerationStrategy().generateIdentifier());
        
        if (includeDesignators) {
            final SAMLObjectBuilder<AttributeDesignator> designatorBuilder = (SAMLObjectBuilder<AttributeDesignator>)
                    XMLObjectProviderRegistrySupport.getBuilderFactory().<AttributeDesignator>getBuilderOrThrow(
                            AttributeDesignator.DEFAULT_ELEMENT_NAME);
            
            AttributeDesignator designator = designatorBuilder.buildObject();
            designator.setAttributeNamespace(SAMLConstants.SAML1_ATTR_NAMESPACE_URI);
            designator.setAttributeName("urn:mace:dir:attribute-def:eduPersonScopedAffiliation");
            attributeQuery.getAttributeQuery().getAttributeDesignators().add(designator);
    
            designator = designatorBuilder.buildObject();
            designator.setAttributeNamespace(SAMLConstants.SAML1_ATTR_NAMESPACE_URI);
            designator.setAttributeName("urn:mace:dir:attribute-def:mail");
            attributeQuery.getAttributeQuery().getAttributeDesignators().add(designator);
    
            designator = designatorBuilder.buildObject();
            designator.setAttributeNamespace(SAMLConstants.SAML1_ATTR_NAMESPACE_URI);
            designator.setAttributeName("urn:mace:dir:attribute-def:foo");
            attributeQuery.getAttributeQuery().getAttributeDesignators().add(designator);
        }

        final Envelope envelope = buildSOAP11Envelope(attributeQuery);

        final String requestContent =
                SerializeSupport.nodeToString(marshallerFactory.getMarshaller(envelope).marshall(envelope,
                        parserPool.newDocument()));

        request.setMethod("POST");
        request.setContent(requestContent.getBytes("UTF-8"));
    }

}
