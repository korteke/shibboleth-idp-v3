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

import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.component.DestructableComponent;
import net.shibboleth.utilities.java.support.component.IdentifiableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Base class for all classes which bridge between {@link PolicyRequirementRule} and {@link Matcher} (in either
 * direction).
 * <p>
 * 
 * This code takes care of the mundane aspects of #getId() and initialize, validate and destroy.
 */
public abstract class BaseBridgingClass extends AbstractIdentifiableInitializableComponent implements
        IdentifiableComponent, DestructableComponent {

    /** The object we are bridging to. */
    private final Object bridgedObject;

    /** Log prefix. */
    private String logPrefix;

    /**
     * Constructor.
     * @param base the object we are bridging to.
     */
    public BaseBridgingClass(@Nonnull Object base) {
        bridgedObject = Constraint.isNotNull(base, "base rule can not be null");
    }

    /** {@inheritDoc} */
    @Override
    protected void doDestroy() {
        ComponentSupport.destroy(bridgedObject);
        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        ComponentSupport.initialize(bridgedObject);
        super.doInitialize();
        logPrefix = null;
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
            result = new StringBuffer("Bridging for class '").append(getId()).append("' ").toString();
            logPrefix = result;
        }
        return result;
    }

    
}
