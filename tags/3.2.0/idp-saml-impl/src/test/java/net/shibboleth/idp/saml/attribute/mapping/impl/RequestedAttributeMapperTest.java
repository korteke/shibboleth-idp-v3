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

package net.shibboleth.idp.saml.attribute.mapping.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.shibboleth.idp.attribute.IdPRequestedAttribute;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link RequestedAttributeMapper}.
 */
public class RequestedAttributeMapperTest extends MappingTests {

    private static final String ID = "atributeID";

    private List<RequestedAttribute> attributes;

    private void loadAttributes() {
        if (null != attributes) {
            return;
        }
        attributes = loadFile("requestedAttributeValues.xml");
    }

    private RequestedAttribute getAttr(String attributeName) {
        loadAttributes();
        for (RequestedAttribute attribute : attributes) {
            if (attributeName.equals(attribute.getName())) {
                return attribute;
            }
        }
        return null;
    }

    @Test public void setterGetterInit() throws ComponentInitializationException {
        final RequestedAttributeMapper mapper = new RequestedAttributeMapper();

        Assert.assertNull(mapper.getId());
        Assert.assertTrue(mapper.getAttributeIds().isEmpty());
        Assert.assertNull(mapper.getAttributeFormat());
        Assert.assertNull(mapper.getSAMLName());
        Assert.assertNull(mapper.getValueMapper());

        mapper.setId(ID);
        try {
            mapper.initialize();
            Assert.fail("SAML Name absent");
        } catch (ComponentInitializationException e) {
            // OK
        }
        mapper.setSAMLName(SAML_NAME_ONE);

        try {
            mapper.initialize();
            Assert.fail("Value Mapper absent");
        } catch (ComponentInitializationException e) {
            // OK
        }
        mapper.setValueMapper(new StringAttributeValueMapper());

        mapper.setAttributeIds(Collections.singletonList("one"));
        mapper.setAttributeFormat(THE_FORMAT);

        mapper.initialize();
        Assert.assertEquals(mapper.getId(), ID);
        Assert.assertEquals(mapper.getSAMLName(), SAML_NAME_ONE);
        Assert.assertEquals(mapper.getAttributeFormat(), THE_FORMAT);

        final List<String> list = mapper.getAttributeIds();
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0), "one");

        Assert.assertTrue(mapper.getValueMapper() instanceof StringAttributeValueMapper);
    }

    @Test public void mapAttributeFormat() throws ComponentInitializationException {
        RequestedAttributeMapper mapper = new RequestedAttributeMapper();

        mapper.setId(ID);
        mapper.setSAMLName(SAML_NAME_ONE);
        mapper.setValueMapper(new StringAttributeValueMapper());
        mapper.setAttributeIds(Arrays.asList("one", ID));
        mapper.setAttributeFormat(THE_FORMAT);
        mapper.initialize();

        final Map<String, IdPRequestedAttribute> result =
                mapper.mapAttribute(getAttr(SAML_NAME_ONE));

        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.containsKey(ID));
        Assert.assertTrue(result.containsKey("one"));
        Assert.assertNotNull(result.get(ID));
        Assert.assertNotNull(result.get("one"));
        Assert.assertEquals(result.get(ID).getValues().size(), 4);

        Assert.assertTrue(mapper.mapAttribute(getAttr(SAML_NAME_TWO)).isEmpty());

        mapper = new RequestedAttributeMapper();

        mapper.setId(ID);
        mapper.setSAMLName(SAML_NAME_TWO);
        mapper.setAttributeFormat(THE_FORMAT);
        mapper.setValueMapper(new StringAttributeValueMapper());
        mapper.setAttributeIds(Collections.singletonList(ID));
        mapper.initialize();
        Assert.assertTrue(mapper.mapAttribute(getAttr(SAML_NAME_TWO)).isEmpty());

    }

    @Test public void mapAttributeNoFormat() throws ComponentInitializationException {
        RequestedAttributeMapper mapper = new RequestedAttributeMapper();

        mapper.setId(ID);
        mapper.setAttributeIds(Collections.singletonList(ID));
        mapper.setSAMLName(SAML_NAME_TWO);
        mapper.setValueMapper(new StringAttributeValueMapper());
        mapper.initialize();

        Map<String, net.shibboleth.idp.attribute.IdPRequestedAttribute> result =
                mapper.mapAttribute(getAttr(SAML_NAME_TWO));

        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.get(ID).getValues().isEmpty());

    }

    @Test public void mapNoValueMatch() throws ComponentInitializationException {
        RequestedAttributeMapper mapper = new RequestedAttributeMapper();

        mapper.setId(ID);
        mapper.setAttributeIds(Collections.singletonList(ID));
        mapper.setSAMLName(SAML_NAME_THREE);
        mapper.setValueMapper(new ScopedStringAttributeValueMapper());
        mapper.initialize();

        Map<String, IdPRequestedAttribute> result =
                mapper.mapAttribute(getAttr(SAML_NAME_THREE));
        Assert.assertFalse(result.containsKey(ID));
    }
}