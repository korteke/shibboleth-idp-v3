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

package net.shibboleth.idp.attribute.resolver;

import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;

/** Mock implementation of {@link AttributeEncoder}. */
public class MockAttributeEncoder extends AbstractInitializableComponent implements AttributeEncoder<String> {

    /** Static protocol value for this encoder. */
    private final String staticProtocol;

    /** Static value for this encoder. */
    private final String staticValue;

    /**
     * Constructor.
     * 
     * @param protocol protocol value returned by {@link #getProtocol()}
     * @param value value returned by {@link #encode(IdPAttribute)}
     */
    public MockAttributeEncoder(String protocol, String value) {
        staticProtocol = protocol;
        staticValue = value;
    }

    /** {@inheritDoc} */
    @Override
    public String getProtocol() {
        return staticProtocol;
    }

    /** {@inheritDoc} */
    @Override
    public String encode(IdPAttribute attribute) throws AttributeEncodingException {
        return staticValue;
    }

    /** {@inheritDoc} */
    @Override
    public Predicate<ProfileRequestContext> getActivationCondition() {
        return Predicates.alwaysTrue();
    }

}