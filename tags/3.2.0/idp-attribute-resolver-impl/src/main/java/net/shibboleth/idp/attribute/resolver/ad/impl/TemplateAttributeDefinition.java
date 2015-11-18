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

package net.shibboleth.idp.attribute.resolver.ad.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.UnsupportedAttributeTypeException;
import net.shibboleth.idp.attribute.resolver.AbstractAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.PluginDependencySupport;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.ThreadSafeAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.collection.LazyMap;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.velocity.Template;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;

/**
 * An attribute definition that constructs its values based on the values of its dependencies using the Velocity
 * Template Language. Dependencies may have multiple values, however multiple dependencies must have the same number of
 * values. In the case of multi-valued dependencies, the template will be evaluated multiples times, iterating over each
 * dependency.
 * 
 * <p>The template is inserted into the engine with a unique name derived from this class and from the id supplied for
 * this attribute.</p>
 */
@ThreadSafeAfterInit
public class TemplateAttributeDefinition extends AbstractAttributeDefinition {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(TemplateAttributeDefinition.class);

    /** Template to be evaluated. */
    @NonnullAfterInit private Template template;

    /** Template (as Text) to be evaluated. */
    @NonnullAfterInit private String templateText;

    /** VelocityEngine. */
    @NonnullAfterInit private VelocityEngine engine;

    /** The names of the attributes we need. */
    @Nonnull @NonnullElements private List<String> sourceAttributes;
    
    /** Constructor. */
    public TemplateAttributeDefinition() {
        sourceAttributes = Collections.emptyList();
    }

    /**
     * Get the source attribute IDs.
     * 
     * @return the source attribute IDs
     */
    @Nonnull @Unmodifiable @NonnullElements public List<String> getSourceAttributes() {
        return Collections.unmodifiableList(sourceAttributes);
    }

    /**
     * Set the source attribute IDs.
     * 
     * @param newSourceAttributes the source attribute IDs
     */
    public void setSourceAttributes(@Nonnull @NullableElements final List<String> newSourceAttributes) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        Constraint.isNotNull(newSourceAttributes, "Source attribute list cannot be null");

        sourceAttributes = new ArrayList<>(newSourceAttributes.size());
        CollectionSupport.addIf(sourceAttributes, newSourceAttributes, Predicates.notNull());
    }

    /**
     * Get the template text to be evaluated.
     * 
     * @return the template
     */
    @NonnullAfterInit public Template getTemplate() {
        return template;
    }

    /**
     * Get the template text to be evaluated.
     * 
     * @return the template
     */
    @NonnullAfterInit public String getTemplateText() {
        return templateText;
    }

    /**
     * Set the literal text of the template to be evaluated.
     * 
     * @param velocityTemplate template to be evaluated
     */
    public void setTemplateText(@Nullable final String velocityTemplate) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        templateText = StringSupport.trimOrNull(velocityTemplate);
    }

    /**
     * Get the {@link VelocityEngine} to be used.
     * 
     * @return the template
     */
    @NonnullAfterInit public VelocityEngine getVelocityEngine() {
        return engine;
    }

    /**
     * Set the {@link VelocityEngine} to be used.
     * 
     * @param velocityEngine engine to be used
     */
    public void setVelocityEngine(@Nonnull final VelocityEngine velocityEngine) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        engine = Constraint.isNotNull(velocityEngine, "VelocityEngine cannot be null");
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
    
        if (getDependencies().isEmpty()) {
            throw new ComponentInitializationException(getLogPrefix() + " no dependencies were configured");
        }
    
        if (null == engine) {
            throw new ComponentInitializationException(getLogPrefix() + " no velocity engine was configured");
        }
    
        if (sourceAttributes.isEmpty()) {
            log.info("{} No Source Attributes supplied, was this intended?", getLogPrefix());
        }
    
        if (null == templateText) {
            // V2 compatibility - define our own template
            final StringBuffer defaultTemplate = new StringBuffer();
            for (final String id : sourceAttributes) {
                defaultTemplate.append("${").append(id).append("} ");
            }
            if (defaultTemplate.length() > 0) {
                templateText = defaultTemplate.toString();
            } else {
                throw new ComponentInitializationException(getLogPrefix()
                        + " no template and no source attributes were configured");
            }
            log.info("{} No template supplied. Default generated was '{}'", getLogPrefix(), templateText);
        }
    
        template = Template.fromTemplate(engine, templateText);
    }

    /** {@inheritDoc} */
    @Override @Nonnull protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {

        final IdPAttribute resultantAttribute = new IdPAttribute(getId());

        final Map<String,Iterator<IdPAttributeValue<?>>> sourceValues = new LazyMap<>();
        final int valueCount = setupSourceValues(workContext, sourceValues);

        final List<IdPAttributeValue<?>> valueList = new ArrayList<>(valueCount);

        for (int i = 0; i < valueCount; i++) {
            log.debug("{} Determing value {}", getLogPrefix(), i + 1);
            final VelocityContext velocityContext = new VelocityContext();

            // Build velocity context.
            for (final String attributeId : sourceValues.keySet()) {
                final IdPAttributeValue<?> value = sourceValues.get(attributeId).next();
                final String velocityValue;
                if (value instanceof EmptyAttributeValue) {
                    switch (((EmptyAttributeValue) value).getValue()) {
                        case NULL_VALUE:
                            velocityValue = null;
                            break;
                        case ZERO_LENGTH_VALUE:
                            velocityValue = "";
                            break;
                        default:
                            throw new ResolutionException(new UnsupportedAttributeTypeException(getLogPrefix()
                                    + "Unknown empty attribute value type " + value.getValue()));
                    }
                } else if (value instanceof StringAttributeValue) {
                    velocityValue = (String) value.getValue();
                } else {
                    throw new ResolutionException(new UnsupportedAttributeTypeException(getLogPrefix()
                            + "This attribute definition only supports attribute value types of "
                            + StringAttributeValue.class.getName() + " not values of type "
                            + value.getClass().getName()));
                }
                log.debug("{} Adding value '{}' for attribute '{}' to the template context", new Object[] {
                        getLogPrefix(), velocityValue, attributeId,});
                velocityContext.put(attributeId, velocityValue);
            }

            // Evaluate the context.
            try {
                log.debug("{} Evaluating template", getLogPrefix());
                final String templateResult = template.merge(velocityContext);
                log.debug("{} Result of template evaluating was '{}'", getLogPrefix(), templateResult);
                valueList.add(StringAttributeValue.valueOf(templateResult));
            } catch (final VelocityException e) {
                // uncovered path
                log.error("{} Unable to evaluate velocity template", getLogPrefix(), e);
                throw new ResolutionException("Unable to evaluate template", e);
            }
        }
        
        resultantAttribute.setValues(valueList);
        return resultantAttribute;
    }

    /**
     * Set up a map which can be used to populate the template. The key is the attribute name and the value is the
     * iterator to give all the names. We also return how deep the iteration will be and throw an exception if there is
     * a mismatch in number of elements in any attribute.
     * 
     * <p>Finally, the names of the source attributes is checked against the dependency attributes and if there is a
     * mismatch then a warning is emitted.</p>
     * 
     * @param workContext source for dependencies
     * @param sourceValues to populate with the attribute iterators
     * 
     * @return how many values in the attributes
     * @throws ResolutionException if there is a mismatched count of attributes
     */
    private int setupSourceValues(@Nonnull final AttributeResolverWorkContext workContext,
            @Nonnull @NonnullElements final Map<String,Iterator<IdPAttributeValue<?>>> sourceValues)
                    throws ResolutionException {

        final Map<String, List<IdPAttributeValue<?>>> dependencyAttributes =
                PluginDependencySupport.getAllAttributeValues(workContext, getDependencies());

        int valueCount = 0;
        boolean valueCountSet = false;

        for (final String attributeName : sourceAttributes) {

            List<IdPAttributeValue<?>> attributeValues = dependencyAttributes.get(attributeName);
            if (null == attributeValues) {
                attributeValues = Collections.emptyList();
            }

            if (!valueCountSet) {
                valueCount = attributeValues.size();
                valueCountSet = true;
            } else if (attributeValues.size() != valueCount) {
                final String msg = getLogPrefix() + " All source attributes used in"
                        + " TemplateAttributeDefinition must have the same number of values: '" + attributeName + "'" ;
                log.error(msg);
                throw new ResolutionException(msg);
            }

            sourceValues.put(attributeName, attributeValues.iterator());
        }

        return valueCount;
    }
    
}