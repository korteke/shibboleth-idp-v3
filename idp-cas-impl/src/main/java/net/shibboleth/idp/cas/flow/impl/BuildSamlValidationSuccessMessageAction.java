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

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.idp.cas.protocol.TicketValidationResponse;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;

import org.joda.time.DateTime;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml1.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

/**
 * Creates the SAML response message for successful ticket validation at the <code>/samlValidate</code> URI.
 *
 * @author Marvin S. Addison
 */
public class BuildSamlValidationSuccessMessageAction extends AbstractOutgoingSamlMessageAction {

    /** Attribute namespace. */
    private static final String NAMESPACE = "http://www.ja-sig.org/products/cas/";

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(BuildSamlValidationSuccessMessageAction.class);

    /** Attribute value node builder. */
    private final XSStringBuilder attrValueBuilder = new XSStringBuilder();

    /** SAML identifier generation strategy. */
    @Nonnull
    private final IdentifierGenerationStrategy identifierGenerationStrategy;

    /** IdP entity ID used to set issuer field of generated assertions. */
    @Nonnull
    private final String entityID;


    /**
     * Creates a new instance with required parameters.
     *
     * @param strategy SAML identifier generation strategy.
     * @param id IdP entity ID.
     */
    public BuildSamlValidationSuccessMessageAction(final IdentifierGenerationStrategy strategy, final String id) {
        Constraint.isNotNull(strategy, "IdentifierGenerationStrategy cannot be null");
        identifierGenerationStrategy = strategy;
        entityID = Constraint.isNotNull(StringSupport.trimOrNull(id), "EntityID cannot be null");
    }


    @Nonnull
    @Override
    protected Response buildSamlResponse(
            @Nonnull final RequestContext springRequestContext,
            @Nonnull final ProfileRequestContext<SAMLObject, SAMLObject> profileRequestContext) {

        final DateTime now = DateTime.now();

        final TicketValidationRequest request = getCASRequest(profileRequestContext);
        final TicketValidationResponse ticketResponse = getCASResponse(profileRequestContext);
        final IdPSession session = getIdPSession(profileRequestContext);
        log.debug("Building SAML response for {} in IdP session {}", request.getService(), session.getId());

        final Response response = newSAMLObject(Response.class, Response.DEFAULT_ELEMENT_NAME);
        response.setID(request.getTicket());
        response.setIssueInstant(DateTime.now());
        final Status status = newSAMLObject(Status.class, Status.DEFAULT_ELEMENT_NAME);
        final StatusCode code = newSAMLObject(StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
        code.setValue(StatusCode.SUCCESS);
        status.setStatusCode(code);
        response.setStatus(status);

        final Assertion assertion = newSAMLObject(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID(identifierGenerationStrategy.generateIdentifier());
        assertion.setIssueInstant(now);
        assertion.setVersion(SAMLVersion.VERSION_11);
        assertion.setIssuer(entityID);

        final Conditions conditions = newSAMLObject(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore(now);
        conditions.setNotOnOrAfter(now.plusSeconds(60));
        final AudienceRestrictionCondition audienceRestriction = newSAMLObject(
                AudienceRestrictionCondition.class, AudienceRestrictionCondition.DEFAULT_ELEMENT_NAME);
        final Audience audience = newSAMLObject(Audience.class, Audience.DEFAULT_ELEMENT_NAME);
        audience.setUri(request.getService());
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictionConditions().add(audienceRestriction);
        assertion.setConditions(conditions);

        // Create an AuthenticationStatement for every authentication bound to the IdP session
        // Use flow ID for authentication method
        for (AuthenticationResult result : session.getAuthenticationResults()) {
            assertion.getAuthenticationStatements().add(
                    newAuthenticationStatement(now, result.getAuthenticationFlowId(), session.getPrincipalName()));
        }

        final AttributeStatement attrStatement = newSAMLObject(
                AttributeStatement.class, AttributeStatement.DEFAULT_ELEMENT_NAME);
        attrStatement.setSubject(newSubject(session.getPrincipalName()));
        for (final String attrName : ticketResponse.getAttributes().keySet()) {
            final Attribute attribute = newSAMLObject(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
            attribute.setAttributeName(attrName);
            attribute.setAttributeNamespace(NAMESPACE);
            for (String value : ticketResponse.getAttributes().get(attrName)) {
                attribute.getAttributeValues().add(newAttributeValue(value));
            }
            attrStatement.getAttributes().add(attribute);
        }
        assertion.getAttributeStatements().add(attrStatement);

        response.getAssertions().add(assertion);
        return response;
    }

    private Subject newSubject(final String identifier) {
        final SubjectConfirmation confirmation = newSAMLObject(
                SubjectConfirmation.class, SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        final ConfirmationMethod method = newSAMLObject(
                ConfirmationMethod.class, ConfirmationMethod.DEFAULT_ELEMENT_NAME);
        method.setConfirmationMethod(ConfirmationMethod.METHOD_ARTIFACT);
        confirmation.getConfirmationMethods().add(method);
        final NameIdentifier nameIdentifier = newSAMLObject(NameIdentifier.class, NameIdentifier.DEFAULT_ELEMENT_NAME);
        nameIdentifier.setValue(identifier);
        final Subject subject = newSAMLObject(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
        subject.setNameIdentifier(nameIdentifier);
        subject.setSubjectConfirmation(confirmation);
        return subject;
    }

    private AuthenticationStatement newAuthenticationStatement(
            final DateTime authnInstant, final String authnMethod, final String principal) {
        final AuthenticationStatement authnStatement = newSAMLObject(
                AuthenticationStatement.class, AuthenticationStatement.DEFAULT_ELEMENT_NAME);
        authnStatement.setAuthenticationInstant(authnInstant);
        authnStatement.setAuthenticationMethod(authnMethod);
        authnStatement.setSubject(newSubject(principal));
        return authnStatement;
    }

    private XSString newAttributeValue(final String value) {
        final XSString stringValue = this.attrValueBuilder.buildObject(
                AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        stringValue.setValue(value);
        return stringValue;
    }
}
