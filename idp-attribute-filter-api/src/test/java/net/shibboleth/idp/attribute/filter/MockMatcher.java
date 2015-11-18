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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiedInitializableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/** A simple, mock implementation of {@link Matcher}. */
public class MockMatcher extends AbstractIdentifiedInitializableComponent implements Matcher { 

    /** ID of the attribute to which this matcher applies. */
    private String matchingAttribute;

    /** Values, of the attribute, considered to match this matcher. */
    private Collection matchingValues;
    
    /** state variable */
    private boolean initialized;

   
    /** do we fail when validate is called? do we fail when we are called?*/
    private boolean fails;

    /** what was passed to getMatchingValues(). */
    private AttributeFilterContext contextUsed;

    public MockMatcher() {
        setId("Mock");
    }

    /**
     * Sets the ID of the attribute to which this matcher applies.
     * 
     * @param id ID of the attribute to which this matcher applies
     */
    public void setMatchingAttribute(String id) {
        matchingAttribute = Constraint.isNotNull(StringSupport.trimOrNull(id), "attribute ID can not be null or empty");
        if (!initialized) {
            setId("Mock " + id);
        }
    }

    /**
     * Sets the values, of the attribute, considered to match this matcher. If null then all attribute values are
     * considered to be matching.
     * 
     * @param values values, of the attribute, considered to match this matcher
     */
    public void setMatchingValues(Collection values) {
        matchingValues = values;
    }

    /** {@inheritDoc} */
    @Override
    public Set<IdPAttributeValue<?>> getMatchingValues(IdPAttribute attribute, AttributeFilterContext filterContext) {
        if (fails) {
            return null;
        }
        if (!Objects.equals(attribute.getId(), matchingAttribute)) {
            return Collections.emptySet();
        }

        if (matchingValues == null) {
            return ImmutableSet.copyOf(attribute.getValues());
        }

        HashSet<IdPAttributeValue<?>> values = new HashSet<>();
        for (IdPAttributeValue value : attribute.getValues()) {
            if (matchingValues.contains(value)) {
                values.add(value);
            }
        }

        return values;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /** {@inheritDoc} */
    @Override
    public void doInitialize()  {
        initialized = true;
    }


    public AttributeFilterContext getContextUsedAndReset() {
        AttributeFilterContext value = contextUsed;
        contextUsed = null;
        return value;
    }

    public void setFailValidate(boolean doFail) {
        fails = doFail;
    }
}