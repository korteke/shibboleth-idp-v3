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

package net.shibboleth.idp.attribute.filter.matcher.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the bases of all implementations of {@link Matcher} which do some sort of comparison.<br/>
 * <br/>
 */

public abstract class AbstractMatcher extends AbstractIdentifiableInitializableComponent implements Matcher {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractMatcher.class);

    /** The String used to prefix log message. */
    private String logPrefix;

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        // Id is now definitive, reset log prefix
        logPrefix = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override @Nonnull @NonnullElements @Unmodifiable public Set<IdPAttributeValue<?>> getMatchingValues(
            @Nonnull final IdPAttribute attribute, @Nonnull final AttributeFilterContext filterContext) {

        final HashSet matchedValues = new HashSet();

        log.debug("{} Applying value comparison to all values of Attribute '{}'", getLogPrefix(), attribute.getId());

        for (IdPAttributeValue value : attribute.getValues()) {
            if (compareAttributeValue(value)) {
                matchedValues.add(value);
            }
        }

        return Collections.unmodifiableSet(matchedValues);
    }

    /**
     * Given a value do we match?
     * 
     * @param value the value to look at
     * @return yes if we do, otherwise no.
     */
    protected abstract boolean compareAttributeValue(IdPAttributeValue value);

    /**
     * Return a string which is to be prepended to all log messages.
     * 
     * @return "Attribute Filter '<filterID>' :"
     */
    protected String getLogPrefix() {
        // local cache of cached entry to allow unsynchronised clearing.
        String prefix = logPrefix;
        if (null == prefix) {
            StringBuilder builder = new StringBuilder("Attribute Filter '").append(getId()).append("':");
            prefix = builder.toString();
            if (null == logPrefix) {
                logPrefix = prefix;
            }
        }
        return prefix;
    }

}