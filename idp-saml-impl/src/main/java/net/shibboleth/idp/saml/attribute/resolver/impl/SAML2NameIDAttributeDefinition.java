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

package net.shibboleth.idp.saml.attribute.resolver.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.XMLObjectAttributeValue;
import net.shibboleth.idp.attribute.resolver.AbstractAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.PluginDependencySupport;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An attribute definition that creates attributes whose values are {@link NameID}.
 * 
 * <p>When building the NameID the textual content of the NameID is the value of the source attribute. If a
 * {@link #nameIdQualifier} is provided that value is used as the NameID's NameQualifier otherwise the attribute
 * issuer's entity ID is used. If a {@link #nameIdSPQualifier} is provided then that valid is used as the NameID's
 * SPNameQualifier, otherwise the attribute recipient's entityID is used.</p>
 */

public class SAML2NameIDAttributeDefinition extends AbstractAttributeDefinition {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SAML2NameIDAttributeDefinition.class);

    /** The builder for the object represented inside this attribute. */
    @Nonnull private final SAMLObjectBuilder<NameID> nameIDBuilder;

    /** Format of the NameID. */
    private String nameIdFormat;

    /** Name qualifier for the NameID. */
    private String nameIdQualifier;

    /** SP name qualifier for the NameID. */
    private String nameIdSPQualifier;

    /**
     * Constructor.
     */
    public SAML2NameIDAttributeDefinition() {
        nameIDBuilder = (SAMLObjectBuilder<NameID>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<NameID>getBuilderOrThrow(
                        NameID.DEFAULT_ELEMENT_NAME);
        nameIdFormat = NameIDType.UNSPECIFIED;
    }

    /**
     * Gets the format for the NameID used as an attribute value.
     * 
     * @return format for the NameID used as an attribute value
     */
    @Nullable public String getNameIdFormat() {
        return nameIdFormat;
    }

    /**
     * Sets the format for the NameID used as an attribute value.
     * 
     * @param format format for the NameID used as an attribute value
     */
    public void setNameIdFormat(@Nullable final String format) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        nameIdFormat = format;
    }

    /**
     * Gets the NameQualifier for the NameID used as an attribute value.
     * 
     * @return NameQualifier for the NameID used as an attribute value
     */
    @Nullable public String getNameIdQualifier() {
        return nameIdQualifier;
    }

    /**
     * Sets the NameQualifier for the NameID used as an attribute value.
     * 
     * @param qualifier NameQualifier for the NameID used as an attribute value
     */
    public void setNameIdQualifier(@Nullable final String qualifier) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        nameIdQualifier = qualifier;
    }

    /**
     * Gets the SPNameQualifier for the NameID used as an attribute value.
     * 
     * @return SPNameQualifier for the NameID used as an attribute value
     */
    @Nullable public String getNameIdSPQualifier() {
        return nameIdSPQualifier;
    }

    /**
     * Sets the SPNameQualifier for the NameID used as an attribute value.
     * 
     * @param qualifier SPNameQualifier for the NameID used as an attribute value
     */
    public void setNameIdSPQualifier(@Nullable final String qualifier) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        nameIdSPQualifier = qualifier;
    }

    /**
     * Builds a name ID. The provided value is the textual content of the NameID. The NameQualifier and SPNameQualifier
     * are set according to the configuration, or to the local and requesting entityIDs respectively.
     * 
     * @param nameIdValue value of the NameID
     * @param resolutionContext current resolution context
     * 
     * @return the constructed NameID
     * @throws ResolutionException if the IdP Name is empty.
     */
    protected NameID buildNameId(@Nonnull @NotEmpty final String nameIdValue,
            @Nonnull final AttributeResolutionContext resolutionContext) throws ResolutionException {

        log.debug("{} building a SAML2 NameID with value of '{}'", getLogPrefix(), nameIdValue);

        final String attributeRecipientID =
                StringSupport.trimOrNull(resolutionContext.getAttributeRecipientID());

        final String attributeIssuerID = StringSupport.trimOrNull(resolutionContext.getAttributeIssuerID());

        final NameID nameId = nameIDBuilder.buildObject();
        nameId.setValue(nameIdValue);

        if (nameIdFormat != null) {
            log.debug("{} Format set to '{}'", getLogPrefix(), nameIdFormat);
            nameId.setFormat(nameIdFormat);
        }

        if (nameIdQualifier != null) {
            log.debug("{} NameQualifier set to '{}'", getLogPrefix(), nameIdQualifier);
            nameId.setNameQualifier(nameIdQualifier);
        } else if (null != attributeIssuerID) {
            log.debug("{} NameQualifier set to '{}'", getLogPrefix(), attributeIssuerID);
            nameId.setNameQualifier(attributeIssuerID);
        } else {
            throw new ResolutionException(getLogPrefix() + " provided attribute issuer ID  was empty");
        }

        if (nameIdSPQualifier != null) {
            log.debug("{} SPNameQualifier set to '{}'", getLogPrefix(), nameIdSPQualifier);
            nameId.setSPNameQualifier(nameIdSPQualifier);
        } else if (null != attributeRecipientID) {
            log.debug("{} SPNameQualifier set to '{}'", getLogPrefix(), attributeRecipientID);
            nameId.setSPNameQualifier(attributeRecipientID);
        } else {
            throw new ResolutionException(getLogPrefix() + " provided attribute recipient ID was empty");
        }

        return nameId;
    }

    /**
     * Worker function for doAttributeDefintionResolve. This returns an AttributeValue if the input value is appropriate
     * for encoding as a NameID.
     * 
     * @param theValue an arbitrary value.
     * @param resolutionContext the context to get the rest of the values from
     * @return null or an attributeValue.
     * @throws ResolutionException if the IdP Name is empty.
     */
    @Nullable private XMLObjectAttributeValue encodeOneValue(@Nonnull final IdPAttributeValue theValue,
            @Nonnull final AttributeResolutionContext resolutionContext) throws ResolutionException {
        if (theValue instanceof StringAttributeValue) {
            final String value = StringSupport.trimOrNull(((StringAttributeValue) theValue).getValue());
            if (value == null) {
                log.warn("{} Value was all whitespace", getLogPrefix());
                return null;
            }
            final NameID nid = buildNameId(value, resolutionContext);
            final XMLObjectAttributeValue val = new XMLObjectAttributeValue(nid);
            return val;
        }
        log.warn("{} Unsupported value type: {}", getLogPrefix(), theValue.getClass().getName());
        return null;
    }

    /** {@inheritDoc} */
    @Override @Nullable protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        List<IdPAttributeValue<?>> inputValues;
        List<? extends IdPAttributeValue<?>> outputValues = null;
        final IdPAttribute result = new IdPAttribute(getId());

        inputValues = PluginDependencySupport.getMergedAttributeValues(workContext, getDependencies(), getId());

        if (null != inputValues && !inputValues.isEmpty()) {

            if (1 == inputValues.size()) {
                final IdPAttributeValue<?> val = encodeOneValue(inputValues.iterator().next(), resolutionContext);
                if (null != val) {
                    outputValues = Collections.singletonList(val);
                }
            } else {
                // TODO Intermediate to solve typing issues.
                final List<XMLObjectAttributeValue> xmlVals = new ArrayList<>(inputValues.size());
                for (final IdPAttributeValue<?> theValue : inputValues) {
                    final XMLObjectAttributeValue val = encodeOneValue(theValue, resolutionContext);
                    if (null != val) {
                        xmlVals.add(val);
                    }
                }
                if (0 == xmlVals.size()) {
                    log.warn("{} No appropriate values", getLogPrefix());
                    return null;
                }
                outputValues = xmlVals;
            }
        }
        result.setValues(outputValues);

        return result;

    }

}