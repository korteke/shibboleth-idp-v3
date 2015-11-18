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

package net.shibboleth.idp.attribute.filter.policyrule.filtercontext.impl;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.policyrule.impl.AbstractRegexpPolicyRule;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compare the principal name for this resolution with the provided regexp.
 */
public class PrincipalNameRegexpPolicyRule extends AbstractRegexpPolicyRule {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PrincipalNameRegexpPolicyRule.class);

    /**
     * Compare the principal name for this resolution with the provided regexp.
     * 
     * @param filterContext the context
     * @return whether it matches {@inheritDoc}
     */
    @Override public Tristate matches(@Nonnull AttributeFilterContext filterContext) {

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        final String principal = filterContext.getPrincipal();
        if (null == principal) {
            log.warn("{} No principal found for comparison", getLogPrefix());
            return Tristate.FAIL;
        }
        log.debug("{} Found principal: {}", getLogPrefix(), principal);

        return regexpCompare(principal);
    }
}