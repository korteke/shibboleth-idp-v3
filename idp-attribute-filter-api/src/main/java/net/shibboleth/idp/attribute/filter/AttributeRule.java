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

package net.shibboleth.idp.attribute.filter;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterWorkContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a value filtering rule for a particular attribute. <code>
     <element name="AttributeRule" type="afp:AttributeRuleType">
        <annotation>
            <documentation>A rule that describes how values of an attribute will be filtered.&lt;/documentation>
        &lt;/annotation>
    &lt;/element>
 </code>
 */
@ThreadSafe
public class AttributeRule extends AbstractIdentifiableInitializableComponent implements
        UnmodifiableComponent {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AttributeRule.class);

    /** Log prefix. */
    private String logPrefix;

    /**
     * Unique ID of the attribute this rule applies to. <code>
        <attribute name="attributeID" type="string" use="required">
            <annotation>
                <documentation>The ID of the attribute to which this rule applies.&lt;/documentation>
            &lt;/annotation>
        &lt;/attribute>
      </code>
     */
    private String attributeId;

    /**
     * Filter that permits the release of attribute values.
     */
    private Matcher matcher;

    /**
     * Filter that denies the release of attribute values.
     */
    private boolean isDenyRule = true;

    /**
     * Gets the ID of the attribute to which this rule applies.
     * 
     * @return ID of the attribute to which this rule applies
     */
    @NonnullAfterInit public String getAttributeId() {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        return attributeId;
    }

    /**
     * Sets the ID of the attribute to which this rule applies.
     * 
     * This property may not be changed after this component has been initialized.
     * 
     * @param id ID of the attribute to which this rule applies
     */
    public void setAttributeId(@Nonnull @NotEmpty String id) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        attributeId = StringSupport.trimOrNull(id);
    }

    /**
     * Gets the matcher used to determine the attribute values filtered by this rule.
     * 
     * @return matcher used to determine the attribute values filtered by this rule
     */
    @Nullable public Matcher getMatcher() {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        return matcher;
    }

    /**
     * Sets the rule used to determine permitted attribute values filtered by this rule.
     * 
     * @param theMatcher matcher used to determine permitted attribute values filtered by this rule
     */
    public void setMatcher(@Nonnull Matcher theMatcher) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        matcher = Constraint.isNotNull(theMatcher, "Rule can not be null");
    }

    /**
     * Gets whether the rule is a deny rule or not.
     * 
     * @return whether the rule is a deny rule or not.
     */
    public boolean getIsDenyRule() {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        return isDenyRule;
    }

    /**
     * Sets the rule used to determine denied attribute values filtered by this rule.
     * 
     * @param isDeny - whether the rule is deny or not.
     */
    public void setIsDenyRule(boolean isDeny) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        isDenyRule = isDeny;
    }

    /**
     * Applies this rule to the respective attribute in the filter context.
     * 
     * @param attribute attribute whose values will be filtered by this policy
     * @param filterContext current filter context
     * 
     */
    public void apply(@Nonnull final IdPAttribute attribute, @Nonnull final AttributeFilterContext filterContext) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        Constraint.isNotNull(attribute, "To-be-filtered attribute can not be null");
        Constraint.isNotNull(filterContext, "Attribute filter context can not be null");

        final AttributeFilterWorkContext filterWorkContext =
                filterContext.getSubcontext(AttributeFilterWorkContext.class, false);
        Constraint.isNotNull(filterWorkContext, "Attribute filter work context can not be null");

        log.debug("{} Filtering values for attribute '{}' which currently contains {} values", getLogPrefix(),
                getAttributeId(), attribute.getValues().size());

        final Set<IdPAttributeValue<?>> matchingValues = matcher.getMatchingValues(attribute, filterContext);

        if (!isDenyRule) {
            if (null == matchingValues) {
                log.warn("{} Filter failed. No values released for attribute '{}'", getLogPrefix(), getAttributeId());
            } else {
                log.debug("{} Filter has permitted the release of {} values for attribute '{}'", getLogPrefix(),
                        matchingValues.size(), attribute.getId());
                filterWorkContext.addPermittedIdPAttributeValues(attribute.getId(), matchingValues);
            }
        } else {
            if (null == matchingValues) {
                log.warn("{} Filter failed. All values denied for attribute '{}'", getLogPrefix(), getAttributeId());
                filterWorkContext.addDeniedIdPAttributeValues(attribute.getId(), attribute.getValues());
            } else {
                log.debug("{} Filter has denied the release of {} values for attribute '{}'", getLogPrefix(),
                        matchingValues.size(), attribute.getId());
                filterWorkContext.addDeniedIdPAttributeValues(attribute.getId(), matchingValues);
            }
        }
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        logPrefix = null;

        if (null == getAttributeId()) {
            throw new ComponentInitializationException(getLogPrefix()
                    + " No attribute specified for this attribute value filter policy");
        }

        if (matcher == null) {
            throw new ComponentInitializationException(getLogPrefix() + " Must have a permit rule or a deny rule");
        }
    }

    /**
     * Get the prefix for logging.
     * 
     * @return Returns the logPrefix.
     */
    protected String getLogPrefix() {
        String result;

        result = logPrefix;
        if (null == result) {
            result = new StringBuffer("Attribute filtering engine '").append(getId()).append("' ").toString();
            logPrefix = result;
        }
        return result;
    }

}