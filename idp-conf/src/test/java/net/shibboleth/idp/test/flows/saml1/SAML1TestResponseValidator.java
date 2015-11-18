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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.saml.xml.SAMLConstants;

import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.saml.saml1.core.Attribute;
import org.opensaml.saml.saml1.core.AttributeStatement;
import org.opensaml.saml.saml1.core.Audience;
import org.opensaml.saml.saml1.core.AudienceRestrictionCondition;
import org.opensaml.saml.saml1.core.AuthenticationStatement;
import org.opensaml.saml.saml1.core.Conditions;
import org.opensaml.saml.saml1.core.ConfirmationMethod;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml1.core.Response;
import org.opensaml.saml.saml1.core.Status;
import org.opensaml.saml.saml1.core.StatusCode;
import org.opensaml.saml.saml1.core.Subject;
import org.opensaml.saml.saml1.core.SubjectConfirmation;
import org.opensaml.saml.saml1.core.impl.NameIdentifierBuilder;
import org.testng.Assert;

/**
 * Validate a test SAML 1 Response.
 */
public class SAML1TestResponseValidator {

    /** Expected IdP entity ID. */
    @Nonnull public String idpEntityID = "https://idp.example.org";

    /** Expected SP entity ID. */
    @Nonnull public String spEntityID = "https://sp.example.org";

    /** Expected authentication method. */
    @Nonnull public String authenticationMethod = AuthenticationStatement.PASSWORD_AUTHN_METHOD;

    /** Expected confirmation method. */
    @Nonnull public String confirmationMethod = ConfirmationMethod.METHOD_BEARER;

    /** Expected name identifier. */
    @Nonnull public NameIdentifier nameIdentifier;

    /** Expected status code. */
    @Nonnull public QName statusCode = StatusCode.SUCCESS;

    /** Expected status message when an error occurs. */
    @Nonnull public String statusMessage = "An error occurred.";

    /** Whether authentication statements should be validated. */
    public boolean validateAuthenticationStatements = true;
    
    /** Whether attributes were limited by designators. */
    public boolean usedAttributeDesignators = false;

    /** Constructor. */
    public SAML1TestResponseValidator() {
        nameIdentifier = new NameIdentifierBuilder().buildObject();
        nameIdentifier.setFormat(SAMLConstants.SAML1_NAMEID_TRANSIENT);
        nameIdentifier.setNameQualifier(idpEntityID);
    }

    /**
     * Validate the response.
     * 
     * Calls validate methods :
     * <ul>
     * <li>{@link #validateConditions(Assertion)}</li>
     * <li>{@link #validateAuthenticationStatements(Assertion)}</li>
     * <li>{@link #validateAttributeStatements(Assertion)}</li>
     * </ul>
     * Calls assert methods :
     * <ul>
     * <li>{@link #assertResponse(Response)}</li>
     * <li>{@link #assertStatus(Status)}</li>
     * <li>{@link #assertAssertions(List)}</li>
     * <li>{@link #assertAssertion(Assertion)}</li>
     * </ul>
     * 
     * @param response the SAML response
     */
    public void validateResponse(@Nullable final Response response) {

        assertResponse(response);

        assertStatus(response.getStatus());

        // short circuit validation upon error
        if (statusCode != StatusCode.SUCCESS) {
            return;
        }

        final List<Assertion> assertions = response.getAssertions();
        assertAssertions(assertions);

        final Assertion assertion = assertions.get(0);
        assertAssertion(assertion);

        validateConditions(assertion);

        if (validateAuthenticationStatements) {
            validateAuthenticationStatements(assertion);
        }

        validateAttributeStatements(assertion);
    }

    /**
     * Validate the assertion conditions.
     * <p>
     * Calls assert methods :
     * <ul>
     * <li>{@link #assertConditions(Conditions)}</li>
     * <li>{@link #assertAudienceRestrictionConditions(List)}</li>
     * <li>{@link #assertAudiences(List)}</li>
     * </ul>
     * 
     * @param assertion the assertion
     */
    public void validateConditions(@Nullable final Assertion assertion) {
        Assert.assertNotNull(assertion);

        final Conditions conditions = assertion.getConditions();
        assertConditions(conditions);

        final List<AudienceRestrictionCondition> audienceRestrictionConditions =
                conditions.getAudienceRestrictionConditions();
        assertAudienceRestrictionConditions(audienceRestrictionConditions);

        final List<Audience> audiences = audienceRestrictionConditions.get(0).getAudiences();
        assertAudiences(audiences);
    }

    /**
     * Validate the assertion authentication statements.
     * <p>
     * Calls assert methods :
     * <ul>
     * <li>{@link #assertAuthenticationStatement(AuthenticationStatement)}</li>
     * <li>{@link #assertAttributeStatement(AttributeStatement)}</li>
     * <li>{@link #assertSubject(Subject)}</li>
     * <li>{@link #assertAuthenticationMethod(String)}</li>
     * </ul>
     * 
     * @param assertion the assertion
     */
    public void validateAuthenticationStatements(@Nullable final Assertion assertion) {
        Assert.assertNotNull(assertion);

        final List<AuthenticationStatement> authenticationStatements = assertion.getAuthenticationStatements();
        assertAuthenticationStatements(authenticationStatements);

        final AuthenticationStatement authenticationStatement = authenticationStatements.get(0);
        assertAuthenticationStatement(authenticationStatement);

        final Subject authenticationStatementSubject = authenticationStatement.getSubject();
        assertSubject(authenticationStatementSubject);

        assertAuthenticationMethod(authenticationStatement.getAuthenticationMethod());
    }

    /**
     * Validate the assertion attribute statements.
     * <p>
     * Calls assert methods :
     * <ul>
     * <li>{@link #assertAuthenticationStatements(List)}</li>
     * <li>{@link #assertAttributeStatement(AttributeStatement)}</li>
     * <li>{@link #assertSubject(Subject)}</li>
     * <li>{@link #assertNameIdentifier(NameIdentifier)}</li>
     * <li>{@link #assertSubjectConfirmation(SubjectConfirmation)}</li>
     * <li>{@link #assertConfirmationMethods(List)}</li>
     * <li>{@link #assertAttributes(List)}</li>
     * </ul>
     * 
     * @param assertion the assertion
     */
    public void validateAttributeStatements(@Nullable final Assertion assertion) {
        Assert.assertNotNull(assertion);

        final List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        assertAttributeStatements(attributeStatements);

        final AttributeStatement attributeStatement = attributeStatements.get(0);
        assertAttributeStatement(attributeStatement);

        final Subject attributeStatementSubject = attributeStatement.getSubject();
        assertSubject(attributeStatementSubject);

        final NameIdentifier nameIdentifier = attributeStatementSubject.getNameIdentifier();
        assertNameIdentifier(nameIdentifier);

        final SubjectConfirmation subjectConfirmation = attributeStatementSubject.getSubjectConfirmation();
        assertSubjectConfirmation(subjectConfirmation);

        final List<ConfirmationMethod> confirmationMethods = subjectConfirmation.getConfirmationMethods();
        assertConfirmationMethods(confirmationMethods);

        final List<Attribute> attributes = attributeStatement.getAttributes();
        assertAttributes(attributes);
    }

    /**
     * Assert that :
     * <ul>
     * <li>the response ID is not null nor empty</li>
     * <li>the response IssueInstant is not null</li>
     * <li>the response version is {@link SAMLVersion#VERSION_11}</li>
     * </ul>
     * 
     * @param response the SAML 1 response
     */
    public void assertResponse(@Nullable final Response response) {
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getID());
        Assert.assertFalse(response.getID().isEmpty());
        Assert.assertNotNull(response.getIssueInstant());
        Assert.assertEquals(response.getVersion(), SAMLVersion.VERSION_11);
    }

    /**
     * Assert that :
     * <ul>
     * <li>the status is not null</li>
     * <li>the status code is not null</li>
     * <li>the status code is the expected status code</li>
     * <li>the status message is the expected status message if the status code is not success</li>
     * </ul>
     * 
     * @param status the status
     */
    public void assertStatus(@Nullable final Status status) {
        Assert.assertNotNull(status);
        Assert.assertNotNull(status.getStatusCode());
        Assert.assertEquals(status.getStatusCode().getValue(), statusCode);
        if (statusCode != StatusCode.SUCCESS) {
            Assert.assertEquals(status.getStatusMessage().getMessage(), statusMessage);
        }
    }

    /**
     * Assert that a single assertion is present.
     * 
     * @param assertions the assertions
     */
    public void assertAssertions(@Nullable final List<Assertion> assertions) {
        Assert.assertNotNull(assertions);
        Assert.assertFalse(assertions.isEmpty());
        Assert.assertEquals(assertions.size(), 1);
        Assert.assertNotNull(assertions.get(0));
    }

    /**
     * Assert that :
     * <ul>
     * <li>the assertion ID is not null nor empty</li>
     * <li>the assertion issue instant is not null</li>
     * <li>the assertion version is {@link SAMLVersion#VERSION_11}</li>
     * <li>the assertion issuer is the expected IdP entity ID</li>
     * </ul>
     * 
     * @param assertion the assertion
     */
    public void assertAssertion(@Nullable final Assertion assertion) {
        Assert.assertNotNull(assertion);
        Assert.assertNotNull(assertion.getID());
        Assert.assertFalse(assertion.getID().isEmpty());
        Assert.assertNotNull(assertion.getIssueInstant());
        Assert.assertEquals(assertion.getMajorVersion(), SAMLVersion.VERSION_11.getMajorVersion());
        Assert.assertEquals(assertion.getMinorVersion(), SAMLVersion.VERSION_11.getMinorVersion());
        Assert.assertEquals(assertion.getIssuer(), idpEntityID);
    }

    /**
     * Assert that a single authentication statement is present.
     * 
     * @param authenticationStatements the authentication statements
     */
    public void assertAuthenticationStatements(@Nullable final List<AuthenticationStatement> authenticationStatements) {
        Assert.assertNotNull(authenticationStatements);
        Assert.assertFalse(authenticationStatements.isEmpty());
        Assert.assertEquals(authenticationStatements.size(), 1);
        Assert.assertNotNull(authenticationStatements.get(0));
    }

    /**
     * Assert that :
     * <ul>
     * <li>the authentication statement has a subject</li>
     * <li>the authentication instant is not null</li>
     * <li>the authentication method is not null nor empty</li>
     * </ul>
     * 
     * @param authenticationStatement the authentication statement
     */
    public void assertAuthenticationStatement(@Nullable final AuthenticationStatement authenticationStatement) {
        Assert.assertNotNull(authenticationStatement);
        Assert.assertNotNull(authenticationStatement.getSubject());
        Assert.assertNotNull(authenticationStatement.getAuthenticationInstant());
        Assert.assertNotNull(authenticationStatement.getAuthenticationMethod());
        Assert.assertFalse(authenticationStatement.getAuthenticationMethod().isEmpty());
    }

    /**
     * Assert that the authentication method is the expected authentication method.
     * 
     * @param method the authentication method
     */
    public void assertAuthenticationMethod(@Nullable final String method) {
        Assert.assertNotNull(method);
        Assert.assertEquals(method, authenticationMethod);
    }

    /**
     * Assert that a single audience restriction condition is present.
     * 
     * @param audienceRestrictionConditions the audience restriction conditions
     */
    public void assertAudienceRestrictionConditions(
            @Nullable final List<AudienceRestrictionCondition> audienceRestrictionConditions) {
        Assert.assertNotNull(audienceRestrictionConditions);
        Assert.assertEquals(audienceRestrictionConditions.size(), 1);
    }

    /**
     * Assert that a single audience is present whose URI is the expected SP entity ID.
     * 
     * @param audiences the audiences
     */
    public void assertAudiences(@Nullable final List<Audience> audiences) {
        Assert.assertNotNull(audiences);
        Assert.assertEquals(audiences.size(), 1);
        Assert.assertEquals(audiences.get(0).getUri(), spEntityID);
    }

    /**
     * Assert that the conditions has a NotBefore and NotOnOrAfter attribute, and that a single audience restriction
     * conditions is present.
     * 
     * @param conditions the conditions
     */
    public void assertConditions(@Nullable final Conditions conditions) {
        Assert.assertNotNull(conditions);
        Assert.assertNotNull(conditions.getNotBefore());
        Assert.assertNotNull(conditions.getNotOnOrAfter());
        // TODO check time via some range ?
        Assert.assertNotNull(conditions.getAudienceRestrictionConditions());
        Assert.assertEquals(conditions.getAudienceRestrictionConditions().size(), 1);
    }

    /**
     * Assert that a single attribute statement is present.
     * 
     * @param attributeStatements the attribute statements
     */
    public void assertAttributeStatements(@Nullable final List<AttributeStatement> attributeStatements) {
        Assert.assertNotNull(attributeStatements);
        Assert.assertFalse(attributeStatements.isEmpty());
        Assert.assertEquals(attributeStatements.size(), 1);
        Assert.assertNotNull(attributeStatements.get(0));
    }

    /**
     * Assert that the attribute statement has a subject and attributes.
     * 
     * @param attributeStatement the attribute statement
     */
    public void assertAttributeStatement(@Nullable final AttributeStatement attributeStatement) {
        Assert.assertNotNull(attributeStatement);
        Assert.assertNotNull(attributeStatement.getSubject());
        Assert.assertNotNull(attributeStatement.getAttributes());
    }

    /**
     * Assert that the subject has a name identifier and subject confirmation.
     * 
     * @param subject the subject
     */
    public void assertSubject(@Nullable final Subject subject) {
        Assert.assertNotNull(subject);
        Assert.assertNotNull(subject.getNameIdentifier());
        Assert.assertNotNull(subject.getSubjectConfirmation());
    }

    /**
     * Assert that :
     * <ul>
     * <li>the name identifier is not null</li>
     * <li>the name identifier value is not null</li>
     * <li>the name identifier value is the expected value if the format is not transient</li>
     * <li>the name format is the expected name format/li>
     * <li>the name qualifier is the expected name qualifier</li>
     * <ul>
     * 
     * @param identifier the name identifier
     */
    public void assertNameIdentifier(@Nullable final NameIdentifier identifier) {
        Assert.assertNotNull(identifier);
        Assert.assertNotNull(identifier.getValue());
        if (nameIdentifier.getFormat() != null
                && !nameIdentifier.getFormat().equals(SAMLConstants.SAML1_NAMEID_TRANSIENT)) {
            Assert.assertEquals(identifier.getValue(), nameIdentifier.getValue());
        }
        Assert.assertEquals(identifier.getFormat(), nameIdentifier.getFormat());
        Assert.assertEquals(identifier.getNameQualifier(), nameIdentifier.getNameQualifier());
    }

    /**
     * Assert that the subject confirmation has a single confirmation method.
     * 
     * @param subjectConfirmation the subject confirmation
     */
    public void assertSubjectConfirmation(@Nullable final SubjectConfirmation subjectConfirmation) {
        Assert.assertNotNull(subjectConfirmation);
        Assert.assertEquals(subjectConfirmation.getConfirmationMethods().size(), 1);
    }

    /**
     * Assert that a single confirmation method is present.
     * 
     * Calls {@link #assertConfirmationMethod(ConfirmationMethod)}.
     * 
     * @param confirmationMethods the confirmation methods
     */
    public void assertConfirmationMethods(@Nullable final List<ConfirmationMethod> confirmationMethods) {
        Assert.assertNotNull(confirmationMethods);
        Assert.assertFalse(confirmationMethods.isEmpty());
        Assert.assertEquals(confirmationMethods.size(), 1);
        Assert.assertNotNull(confirmationMethods.get(0));
        assertConfirmationMethod(confirmationMethods.get(0));
    }

    /**
     * Assert the confirmation method.
     * 
     * @param method the confirmation method
     */
    public void assertConfirmationMethod(@Nullable final ConfirmationMethod method) {
        Assert.assertNotNull(method);
        Assert.assertEquals(method.getConfirmationMethod(), confirmationMethod);
    }

    /**
     * Assert that two attributes are present.
     * <p>
     * The first attribute is
     * <ul>
     * <li>name : urn:mace:dir:attribute-def:eduPersonAffiliation</li>
     * <li>namespace : {@link SAMLConstants#SAML1_ATTR_NAMESPACE_URI}</li>
     * <li>value : member</li>
     * </ul>
     * <p>
     * The second attribute is
     * <ul>
     * <li>name : urn:oid:0.9.2342.19200300.100.1.3</li>
     * <li>namespace : {@link SAMLConstants#SAML1_ATTR_NAMESPACE_URI}</li>
     * <li>value : jdoe@shibboleth.net</li>
     * </ul>
     * 
     * Calls assert methods :
     * <ul>
     * <li>{@link #assertAttribute(Attribute, String, String)}</li>
     * </ul>
     * 
     * @param attributes the attributes
     */
    public void assertAttributes(@Nullable final List<Attribute> attributes) {
        Assert.assertNotNull(attributes);
        Assert.assertFalse(attributes.isEmpty());
        Assert.assertEquals(attributes.size(), usedAttributeDesignators ? 2 : 4);

        // Ignore attribute ordering
        final Map<String, Attribute> actualAttributes = new HashMap<>();
        for (final Attribute attribute : attributes) {
            actualAttributes.put(attribute.getAttributeName(), attribute);
        }

        if (usedAttributeDesignators) {
            final Attribute actualMailAttribute = actualAttributes.get("urn:mace:dir:attribute-def:mail");
            Assert.assertNotNull(actualMailAttribute);
            assertAttribute(actualMailAttribute, "urn:mace:dir:attribute-def:mail", "jdoe@example.org");

            // The scope here is in a separate XML attribute, so not in the element content.
            final Attribute actualEPSAAttribute =
                    actualAttributes.get("urn:mace:dir:attribute-def:eduPersonScopedAffiliation");
            Assert.assertNotNull(actualEPSAAttribute);
            assertAttribute(actualEPSAAttribute, "urn:mace:dir:attribute-def:eduPersonScopedAffiliation", "member");
        } else {
            final Attribute actualUidAttribute = actualAttributes.get("urn:mace:dir:attribute-def:uid");
            Assert.assertNotNull(actualUidAttribute);
            assertAttribute(actualUidAttribute, "urn:mace:dir:attribute-def:uid", "jdoe");

            final Attribute actualMailAttribute = actualAttributes.get("urn:mace:dir:attribute-def:mail");
            Assert.assertNotNull(actualMailAttribute);
            assertAttribute(actualMailAttribute, "urn:mace:dir:attribute-def:mail", "jdoe@example.org");

            // The scope here is in a separate XML attribute, so not in the element content.
            final Attribute actualEPPNAttribute =
                    actualAttributes.get("urn:mace:dir:attribute-def:eduPersonPrincipalName");
            Assert.assertNotNull(actualEPPNAttribute);
            assertAttribute(actualEPPNAttribute, "urn:mace:dir:attribute-def:eduPersonPrincipalName", "jdoe");

            // The scope here is in a separate XML attribute, so not in the element content.
            final Attribute actualEPSAAttribute =
                    actualAttributes.get("urn:mace:dir:attribute-def:eduPersonScopedAffiliation");
            Assert.assertNotNull(actualEPSAAttribute);
            assertAttribute(actualEPSAAttribute, "urn:mace:dir:attribute-def:eduPersonScopedAffiliation", "member");
        }
    }

    /**
     * Assert that the attribute namespace is {@link SAMLConstants#SAML1_ATTR_NAMESPACE_URI}, the attribute name is the
     * supplied name, and the attribute value is the single supplied String value.
     * 
     * @param attribute the attribute
     * @param attributeName the attribute name
     * @param attributeValue the attribute value
     */
    public void assertAttribute(@Nullable final Attribute attribute, @Nonnull final String attributeName,
            @Nonnull final String attributeValue) {
        Assert.assertNotNull(attribute);
        Assert.assertEquals(attribute.getAttributeName(), attributeName);
        Assert.assertEquals(attribute.getAttributeNamespace(), SAMLConstants.SAML1_ATTR_NAMESPACE_URI);
        Assert.assertEquals(attribute.getAttributeValues().size(), 1);
        Assert.assertTrue(attribute.getAttributeValues().get(0) instanceof XSAny);
        Assert.assertEquals(((XSAny) attribute.getAttributeValues().get(0)).getTextContent(), attributeValue);
    }
}
