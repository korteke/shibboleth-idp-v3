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

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.DestructableComponent;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridging class to go from a {@link Matcher} to a {@link PolicyRequirementRule}.
 * <p>
 * 
 * If the value of the supplied attribute matches then this is true, otherwise false.
 */
public class PolicyFromMatcherId extends BaseBridgingClass implements PolicyRequirementRule,
        IdentifiedComponent, DestructableComponent {

    /** The rule we are shadowing. */
    private final Matcher theMatcher;
    
    /** The attribute Id we care about. */
    private final String attributeId;
    
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(PolicyFromMatcherId.class);
    
    /**
     * Constructor.
     * @param matcher the class we are bridging to
     * @param attribute the Id of the attribute in question.
     */
    public PolicyFromMatcherId(@Nonnull Matcher matcher, @Nonnull @NotEmpty String attribute) {
        super(matcher);
        theMatcher = matcher;
        attributeId = Constraint.isNotNull(StringSupport.trimOrNull(attribute), "attribute must not be null or empty");
    }
    
    /** Gets the Id of the attribute in question. 
     * @return the id.
     */
    @Nonnull public String getAttributeId() {
        return attributeId; 
    }

    /** Testing support.  Get the embedded matcher.
     * @return the embedded matcher.
     */
    @Nonnull public Matcher getMatcher() {
        return theMatcher;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public Tristate matches(@Nonnull AttributeFilterContext context) {
        
        log.debug("{} Applying matcher supplied as policy to all values of attribute {}", getLogPrefix(), attributeId);

        final IdPAttribute attribute = context.getPrefilteredIdPAttributes().get(attributeId);
        
        if (null == attribute) {
            log.debug("{} No attribute found with Id of {}", getLogPrefix(), attributeId);
            return Tristate.FALSE;
        }
        final Set<IdPAttributeValue<?>> result = theMatcher.getMatchingValues(attribute, context);

        if (null == result) {
            log.warn("{} Matcher returned null, returning FAIL", getLogPrefix());
            return Tristate.FAIL;
        } else if (!result.isEmpty()) {
            log.debug("{} Matcher returned some values.  Return TRUE", getLogPrefix());
            return Tristate.TRUE;
        } else {
            log.debug("{} Matcher returned no values for the attribute {}.  Return FALSE", getLogPrefix(), attributeId);
            return Tristate.FALSE;
        }
    }
}

