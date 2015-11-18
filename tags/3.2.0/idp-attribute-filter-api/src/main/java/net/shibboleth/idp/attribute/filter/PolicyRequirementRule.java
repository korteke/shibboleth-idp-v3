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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;

/**
 * Java definition of PolicyRequirementRule.
 * 
 * This is a specific mapping of a MatchFunctor as used in an {@link AttributeFilterPolicy}
 * 
 * All function can return {@link Tristate#TRUE} or {@link Tristate#FALSE} (as expected), but if something odd happens
 * during enumeration (like not being able to find something in the context) then they return {@link Tristate#FAIL}.
 */
@ThreadSafe
public interface PolicyRequirementRule extends IdentifiedComponent {

    /**
     * Representation of the three outcomes of a PolicyRequirementRule.
     */
    public enum Tristate {
        /** Match. */
        TRUE,
        /** No match. */
        FALSE,
        /** Match operation failed. */
        FAIL
    };

    /** A {@link PolicyRequirementRule} that returns true matched. */
    public static final PolicyRequirementRule MATCHES_ALL = new PolicyRequirementRule() {

        @Override public Tristate matches(@Nonnull AttributeFilterContext filterContext) {
            return Tristate.TRUE;
        }

        @Override @Nullable public String getId() {
            return "MATCHES_ALL";
        }

    };

    /** A {@link PolicyRequirementRule} that returns false as matched. */
    public static final PolicyRequirementRule MATCHES_NONE = new PolicyRequirementRule() {

        @Override public Tristate matches(@Nonnull AttributeFilterContext filterContext) {
            return Tristate.FALSE;
        }

        @Override @Nullable public String getId() {
            return "MATCHES_NONE";
        }

    };

    /** A {@link PolicyRequirementRule} that returns failed. */
    public static final PolicyRequirementRule REQUIREMENT_RULE_FAILS = new PolicyRequirementRule() {

        @Override public Tristate matches(@Nonnull AttributeFilterContext filterContext) {
            return Tristate.FAIL;
        }

        @Override @Nullable public String getId() {
            return "REQUIREMENT_RULE_FAILS";
        }

    };

    /**
     * Evaluate what this rule means.
     * 
     * @param filterContext the context.
     * @return whether the rule holds
     */
    Tristate matches(@Nonnull final AttributeFilterContext filterContext);

}