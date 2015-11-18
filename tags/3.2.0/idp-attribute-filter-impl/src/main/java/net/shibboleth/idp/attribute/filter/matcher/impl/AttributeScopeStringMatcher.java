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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test that the scope of a {@link ScopedStringAttributeValue} is a string match to the value configured. <br/>
 * If the value is not scoped return false (meaning that the value will not be included in the resulting set).
 */
public class AttributeScopeStringMatcher extends AbstractStringMatcher {

    /** Logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AttributeScopeStringMatcher.class);

    /** {@inheritDoc} */
    @Override
    public boolean compareAttributeValue(@Nullable final IdPAttributeValue value) {

        if (null == value) {
            return false;
        }

        if (value instanceof ScopedStringAttributeValue) {
            final ScopedStringAttributeValue scopedValue = (ScopedStringAttributeValue) value;
            return super.stringCompare(scopedValue.getScope());

        } else {
            log.warn("{} Object supplied to ScopedAttributeValue comparison"
                    + " was of class '{}', not ScopedAttributeValue", getLogPrefix(), value.getClass().getName());
            return false;
        }
    }

}
