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

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test that an {@link IdPAttributeValue} is a regexp match to the configured string. <br/>
 * If the value is not a {@link StringAttributeValue} string it is coerced into a string via the value's
 * {@link java.lang.Object#toString()} method.
 */
public class AttributeValueRegexpMatcher extends AbstractRegexpStringMatcher {

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(AttributeValueRegexpMatcher.class);

    /** {@inheritDoc} */
    public boolean compareAttributeValue(@Nullable final IdPAttributeValue value) {
        
        if (null == value) {
            return false;
        } else if (value instanceof EmptyAttributeValue) {
            return false;
        } else if (value instanceof StringAttributeValue) {
            return regexpCompare(((StringAttributeValue) value).getValue());

        } else {
            final String valueAsString = value.getValue().toString();
            log.warn("{} Object supplied to StringAttributeValue comparison"
                    + " was of class {}, not StringAttributeValue, comparing with {}", new Object[] {
                    getLogPrefix(), value.getClass().getName(), valueAsString,});
            return regexpCompare(valueAsString);
        } 
    }

}