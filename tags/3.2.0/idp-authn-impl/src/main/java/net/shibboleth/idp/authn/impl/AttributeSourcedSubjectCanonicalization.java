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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.AbstractSubjectCanonicalizationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.SubjectCanonicalizationException;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * An action that extracts a resolved {@link IdPAttribute} value from an {@link AttributeContext} child obtained via
 * lookup function (by default a child of the {@link SubjectCanonicalizationContext}), and uses it as the result
 * of subject canonicalization.
 * 
 * <p>This action operates on a set of previously resolved attributes that are presumed to have been generated based
 * in some fashion on the content of the {@link SubjectCanonicalizationContext}.</p>
 * 
 * <p>String and scoped attribute values are supported.</p>
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link AuthnEventIds#INVALID_SUBJECT}
 * @pre <pre>ProfileRequestContext.getSubcontext(SubjectCanonicalizationContext.class) != null</pre>
 * @post <pre>SubjectCanonicalizationContext.getPrincipalName() != null
 *  || SubjectCanonicalizationContext.getException() != null</pre>
 */
public class AttributeSourcedSubjectCanonicalization extends AbstractSubjectCanonicalizationAction {
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AttributeSourcedSubjectCanonicalization.class);

    /** Delimiter to use for scoped attribute serialization. */
    private char delimiter;
    
    /** Ordered list of attributes to look for and read from. */
    @Nonnull @NonnullElements private List<String> attributeSourceIds;
        
    /** Lookup strategy for {@link AttributeContext} to read from. */
    @Nonnull private Function<ProfileRequestContext,AttributeContext> attributeContextLookupStrategy;
    
    /** The context to read from. */
    @Nullable private AttributeContext attributeCtx;
    
    /** Constructor. */
    public AttributeSourcedSubjectCanonicalization() {
        delimiter = '@';
        attributeSourceIds = Collections.emptyList();
        
        attributeContextLookupStrategy = Functions.compose(new ChildContextLookup<>(AttributeContext.class),
                new ChildContextLookup<ProfileRequestContext,SubjectCanonicalizationContext>(
                        SubjectCanonicalizationContext.class));
    }
    
    /**
     * Set the delimiter to use for serializing scoped attribute values.
     * 
     * @param ch delimiter to use
     */
    public void setScopedDelimiter(final char ch) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        delimiter = ch;
    }
    
    /**
     * Set the attribute IDs to read from in order of preference.
     * 
     * @param ids   attribute IDs to read from
     */
    public void setAttributeSourceIds(@Nonnull @NonnullElements final List<String> ids) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        attributeSourceIds = new ArrayList<>(StringSupport.normalizeStringCollection(ids));
    }
    
    /**
     * Set the lookup strategy for the {@link AttributeContext} to read from.
     * 
     * @param strategy  lookup strategy
     */
    public void setAttributeContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,AttributeContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        attributeContextLookupStrategy = Constraint.isNotNull(strategy,
                "AttributeContext lookup strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (attributeSourceIds.isEmpty()) {
            throw new ComponentInitializationException("Attribute source ID list cannot be empty");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext, 
            @Nonnull final SubjectCanonicalizationContext c14nContext) {

        attributeCtx = attributeContextLookupStrategy.apply(profileRequestContext);
        if (attributeCtx == null || attributeCtx.getIdPAttributes().isEmpty()) {
            log.warn("{} No attributes found, canonicalization not possible", getLogPrefix());
            c14nContext.setException(new SubjectCanonicalizationException("No attributes were found"));
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT);
            return false;
        }
        
        return super.doPreExecute(profileRequestContext, c14nContext);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext, 
            @Nonnull final SubjectCanonicalizationContext c14nContext) {
        
        for (final String id : attributeSourceIds) {
            final IdPAttribute attr = attributeCtx.getIdPAttributes().get(id);
            if (attr == null) {
                continue;
            }
            for (final IdPAttributeValue val : attr.getValues()) {
                if (val instanceof StringAttributeValue) {
                    if (val.getValue() == null || ((StringAttributeValue) val).getValue().isEmpty()) {
                        log.debug("{} Ignoring null/empty string value", getLogPrefix());
                        continue;
                    }
                    log.debug("{} Using attribute {} string value {} as input to transforms", getLogPrefix(), id,
                            val.getValue());
                    c14nContext.setPrincipalName(applyTransforms(((StringAttributeValue) val).getValue()));
                    return;
                } else if (val instanceof ScopedStringAttributeValue) {
                    final ScopedStringAttributeValue scoped = (ScopedStringAttributeValue) val;
                    final String withScope = scoped.getValue() + delimiter + scoped.getScope();
                    log.debug("{} Using attribute {} scoped value {} as input to transforms", getLogPrefix(), id,
                            withScope);
                    c14nContext.setPrincipalName(applyTransforms(withScope));
                    return;
                } else {
                    log.warn("{} Unsupported attribute value type: {}", getLogPrefix(), val.getClass().getName());
                }
            }
        }
        
        log.info("{} Attribute sources {} did not produce a usable identifier", getLogPrefix(), attributeSourceIds);
        c14nContext.setException(new SubjectCanonicalizationException("No usable attribute values were found"));
        ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT);
    }

}