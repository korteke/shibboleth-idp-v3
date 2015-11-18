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

package net.shibboleth.idp.attribute.filter.policyrule.impl;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

/**
 * General {@link PolicyRequirementRule} for {@link String} comparison of strings in Attribute Filters.   
 */
public abstract class AbstractStringPolicyRule extends AbstractPolicyRule implements PolicyRequirementRule {

    /** String to match for a positive evaluation. */
    private String matchString;

    /** Whether the match evaluation is case sensitive. */
    private boolean ignoreCase;

    /**
     * Gets the string to match for a positive evaluation.
     * 
     * @return string to match for a positive evaluation
     */
    @Nullable public String getMatchString() {
        return matchString;
    }

    /**
     * Sets the string to match for a positive evaluation.
     * 
     * @param match string to match for a positive evaluation
     */
    public void setMatchString(@Nullable final String match) {
        matchString = match;
    }

    /**
     * Gets whether the match evaluation is case insensitive.
     * 
     * @return whether the match evaluation is case insensitive
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * Sets whether the match evaluation is case sensitive.
     * 
     * @param isIgnoreCase whether the match evaluation is case sensitive
     */
    public void setIgnoreCase(boolean isIgnoreCase) {
        ignoreCase = isIgnoreCase;
    }

    /**
     * Matches the given value against the provided match string. 
     * 
     * @param value the value to evaluate
     * 
     * @return true if the value matches the given match string, false if not
     */
    protected Tristate stringCompare(@Nullable final String value) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        boolean result;
        if (value == null) {
            result = matchString == null;
        } else if (ignoreCase) {
            result = value.equalsIgnoreCase(matchString);
        } else {
            result = value.equals(matchString);
        }
        if (result) {
            return Tristate.TRUE;
        }
        return Tristate.FALSE;
    }
}
