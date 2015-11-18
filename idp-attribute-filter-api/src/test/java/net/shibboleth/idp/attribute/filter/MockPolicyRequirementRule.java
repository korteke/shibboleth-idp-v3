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

import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiedInitializableComponent;

/** A simple, mock implementation of {@link Matcher}. */
public class MockPolicyRequirementRule extends AbstractIdentifiedInitializableComponent implements
        PolicyRequirementRule {

    /** state variable */
    private boolean initialized;

    /** to return from matcher(). */
    private Tristate retVal;

    /** do we fail when validate is called? do we fail when we are called? */
    private boolean fails;

    /** what was passed to getMatchingValues(). */
    private AttributeFilterContext contextUsed;

    public MockPolicyRequirementRule() {
        setId("Mock");
    }


    /** {@inheritDoc} */
    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /** {@inheritDoc} */
    @Override
    public void doInitialize() {
        initialized = true;
    }

    public void setRetVal(Tristate what) {
        retVal = what;
    }

    public AttributeFilterContext getContextUsedAndReset() {
        AttributeFilterContext value = contextUsed;
        contextUsed = null;
        return value;
    }

    public void setFailValidate(boolean doFail) {
        fails = doFail;
    }

    /** {@inheritDoc} */
    @Override
    public Tristate matches(@Nonnull AttributeFilterContext filterContext) {
        if (fails) {
            return Tristate.FAIL;
        }
        contextUsed = filterContext;
        return retVal;
    }
}