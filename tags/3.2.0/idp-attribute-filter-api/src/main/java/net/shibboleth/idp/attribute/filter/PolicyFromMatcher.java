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
import net.shibboleth.utilities.java.support.component.DestructableComponent;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridging class to go from a {@link Matcher} to a {@link PolicyRequirementRule}.
 * <p>
 * 
 * If any value of any attribute matches then this is true, otherwise false.
 */
public class PolicyFromMatcher extends BaseBridgingClass implements PolicyRequirementRule,
        IdentifiedComponent, DestructableComponent {

    /** The rule we are shadowing. */
    private final Matcher matcher;
    
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(PolicyFromMatcher.class);
    
    /**
     * Constructor.
     * @param theMatcher the class we are bridging to
     */
    public PolicyFromMatcher(@Nonnull Matcher theMatcher) {
        super(theMatcher);
        matcher = theMatcher;
    }
    
    /** Testing support.  Get the embedded matcher.
     * @return the embedded matcher.
     */
    @Nonnull public Matcher getMatcher() {
        return matcher;
    }

    /** {@inheritDoc} */
    @Override
    public Tristate matches(@Nonnull AttributeFilterContext context) {
        
        log.debug("{} Applying matcher supplied as policy to all values of all attributes", getLogPrefix());

        for (IdPAttribute attribute : context.getPrefilteredIdPAttributes().values()) {
            Set<IdPAttributeValue<?>> result = matcher.getMatchingValues(attribute, context);
            if (null == result) {
                log.warn("{} Matcher returned null, returning FAIL", getLogPrefix());
                return Tristate.FAIL;
            } else if (!result.isEmpty()) {
                log.debug("{} Matcher returned some values.  Return TRUE", getLogPrefix());
                return Tristate.TRUE;
            }
        }
        log.debug("{} Matcher returned no values for any attribute.  Return FALSE", getLogPrefix());
        return Tristate.FALSE;
    }
}

