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

import java.util.Collections;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.AbstractAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.saml.nameid.impl.TransientIdGenerationStrategy;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.saml.common.SAMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * An attribute definition that generates random identifiers useful for transient subject IDs.
 * 
 * <p>
 * The generation in devolved to the supplied {@link TransientIdGenerationStrategy}, which will be a
 * {@link net.shibboleth.idp.saml.nameid.impl.StoredTransientIdGenerationStrategy} for the Transient and
 * {@link net.shibboleth.idp.saml.nameid.impl.CryptoTransientIdGenerationStrategy} for a CryptoTransient.
 */
public class TransientIdAttributeDefinition extends AbstractAttributeDefinition {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(TransientIdAttributeDefinition.class);

    /** The actual implementation of the transient generation process. */
    @Nonnull private final TransientIdGenerationStrategy idGenerator;

    /**
     * Constructor.
     * 
     * @param generator the (crypto or transient) generator to use
     */
    public TransientIdAttributeDefinition(@Nonnull final TransientIdGenerationStrategy generator) {
        idGenerator = Constraint.isNotNull(generator, "Id generator must be non null");
    }
    
    /** return the id generator being used.  This is primarily used in testing.
     * @return the generator strategy;
     */
    @Nonnull public TransientIdGenerationStrategy getTransientIdGenerationStrategy() {
        return idGenerator;
    }

    /** {@inheritDoc} */
    @Override @Nonnull protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        final String attributeRecipientID = getAttributeRecipientID(resolutionContext);

        final String principalName = getPrincipal(resolutionContext);

        try {
            final String transientId = idGenerator.generate(attributeRecipientID, principalName);
            log.debug("{} creating new transient ID '{}'", new Object[] {getLogPrefix(), transientId,});

            final IdPAttribute result = new IdPAttribute(getId());
            result.setValues(Collections.singletonList(new StringAttributeValue(transientId)));
            return result;
        } catch (final SAMLException e) {
            throw new ResolutionException(e);
        }
    }

    /**
     * Police and get the AttributeRecipientID.
     * 
     * @param resolutionContext where to look
     * @return the AttributeRecipientID
     * @throws ResolutionException if it was non null
     */
    @Nonnull @NotEmpty private String getAttributeRecipientID(
            @Nonnull final AttributeResolutionContext resolutionContext) throws ResolutionException {
        final String attributeRecipientID = resolutionContext.getAttributeRecipientID();
        if (Strings.isNullOrEmpty(attributeRecipientID)) {
            throw new ResolutionException(getLogPrefix() + " provided attribute recipient ID was empty");
        }
        return attributeRecipientID;
    }

    /**
     * Police and get the Principal.
     * 
     * @param context where to look
     * @return the Principal
     * @throws ResolutionException if it was non null
     */
    @Nonnull @NotEmpty private String getPrincipal(@Nonnull final AttributeResolutionContext context)
            throws ResolutionException {
        final String principalName = context.getPrincipal();
        if (Strings.isNullOrEmpty(principalName)) {
            throw new ResolutionException(getLogPrefix() + " provided prinicipal name was empty");
        }

        return principalName;
    }

}