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

package net.shibboleth.idp.attribute;

import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Mock implementation of {@link AttributeEncoder}.
 * 
 * @param <ValueType> type of value produced by this encoder
 */
public class MockEncoder<ValueType> implements AttributeEncoder<ValueType> {

    /** Static protocol string for this encoder. */
    private String proto;

    /** Static encoded attribute value for this encoder. */
    private ValueType encodedValue;

    /** Constructor. */
    public MockEncoder() {

    }

    /**
     * Constructor.
     * 
     * @param protocol static value to bereturned from {@link #getProtocol()}
     * @param encodedAttributeValue static value to be returned from {@link #encode(IdPAttribute)}
     */
    public MockEncoder(String protocol, ValueType encodedAttributeValue) {
        proto = protocol;
        encodedValue = encodedAttributeValue;
    }

    /** {@inheritDoc} */
    @Override
    public String getProtocol() {
        return proto;
    }

    /** {@inheritDoc} */
    @Override
    public ValueType encode(IdPAttribute attribute) throws AttributeEncodingException {
        return encodedValue;
    }
    
    /** {@inheritDoc} */
    @Override
    public Predicate<ProfileRequestContext> getActivationCondition() {
        return Predicates.alwaysTrue();
    }
    
}