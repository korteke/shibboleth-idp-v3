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

import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.saml.impl.TestSources;

import org.opensaml.messaging.context.BaseContext;

/**
 * Strings and such used for testing.
 */
public abstract class DataSources {

    public final static String TEST_STRING = "nibbleahappywarthog";

    public final static String TEST_STRING_UPPER = TEST_STRING.toUpperCase();

    public final static String NON_MATCH_STRING = "ThisIsADifferentString";

    public final static String TEST_REGEX = "^n.*g";

    public final static StringAttributeValue STRING_VALUE = new StringAttributeValue(TEST_STRING);

    public final static StringAttributeValue NON_MATCH_STRING_VALUE = new StringAttributeValue(NON_MATCH_STRING);

    public final static ScopedStringAttributeValue SCOPED_VALUE_VALUE_MATCH = new ScopedStringAttributeValue(
            TEST_STRING, NON_MATCH_STRING);

    public final static ScopedStringAttributeValue SCOPED_VALUE_SCOPE_MATCH = new ScopedStringAttributeValue(
            NON_MATCH_STRING, TEST_STRING);

    public final static ByteAttributeValue BYTE_ATTRIBUTE_VALUE = new ByteAttributeValue(TEST_STRING.getBytes());

    public final static IdPAttributeValue OTHER_VALUE = new IdPAttributeValue() {

        @Override @Nonnull public Object getValue() {
            return TEST_STRING;
        }
        public String getDisplayValue() {
            return TEST_STRING;
        }
    };

    public static AttributeFilterContext populatedFilterContext(String principal, String issuerID, String recipientId) {

        BaseContext parent = new BaseContext() {};
        parent.addSubcontext(TestSources.createResolutionContext(principal, issuerID, recipientId));
        AttributeFilterContext retVal = parent.getSubcontext(AttributeFilterContext.class, true);
        retVal.setPrincipal(principal);
        retVal.setAttributeIssuerID(issuerID);
        retVal.setAttributeRecipientID(recipientId);
        return retVal;
    }

    public static AttributeFilterContext unPopulatedFilterContext() {

        BaseContext parent = new BaseContext() {};
        parent.addSubcontext(new AttributeResolutionContext());
        return parent.getSubcontext(AttributeFilterContext.class, true);
    }
}
