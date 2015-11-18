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

package net.shibboleth.idp.consent.storage.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.idp.consent.impl.ConsentTestingSupport;
import net.shibboleth.idp.consent.logic.impl.AttributeValuesHashFunction;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Function;

/** Unit tests for {@link ConsentSerializer}. */
public class ConsentSerializerTest {

    /** Class logger. */
    @Nonnull protected final Logger log = LoggerFactory.getLogger(ConsentSerializerTest.class);

    private static final String CONTEXT = "_context";

    private static final String KEY = "_key";

    protected ConsentSerializer serializer;

    protected IdPAttribute attribute1;

    protected IdPAttribute attribute2;

    protected IdPAttributeValue<?> value1;

    protected IdPAttributeValue<?> value2;

    protected IdPAttributeValue<?> value3;

    protected Consent consent1;

    protected Consent consent2;

    protected DateTime expiration;

    protected Map<String, Consent> consents;

    protected Function<Collection<IdPAttributeValue<?>>, String> attributeValuesHashFunction;

    @BeforeMethod public void setUp() {
        serializer = new ConsentSerializer();

        attributeValuesHashFunction = new AttributeValuesHashFunction();

        final Map<String, IdPAttribute> attributes = ConsentTestingSupport.newAttributeMap();

        consent1 = new Consent();
        consent1.setId("attribute1");
        consent1.setValue(attributeValuesHashFunction.apply(attributes.get("attribute1").getValues()));
        consent1.setApproved(true);

        consent2 = new Consent();
        consent2.setId("attribute2");
        consent2.setValue(attributeValuesHashFunction.apply(attributes.get("attribute2").getValues()));
        consent2.setApproved(false);

        consents = new LinkedHashMap<>();
        consents.put(consent1.getId(), consent1);
        consents.put(consent2.getId(), consent2);
    }

    @Test(expectedExceptions = ConstraintViolationException.class) public void testNull() throws Exception {
        serializer.initialize();
        serializer.serialize(null);
    }

    @Test(expectedExceptions = ConstraintViolationException.class) public void testEmpty() throws Exception {
        serializer.initialize();
        serializer.serialize(new HashMap<String, Consent>());
    }

    @Test(expectedExceptions = ConstraintViolationException.class) public void testNullSymoblics() throws Exception {
        serializer.setSymbolics(null);
    }

    @Test(expectedExceptions = UnmodifiableComponentException.class) public void testMutatingSymoblics()
            throws Exception {
        serializer.initialize();
        serializer.setSymbolics(new HashMap<String, Integer>());
    }

    @Test public void testSimple() throws Exception {
        serializer.initialize();

        final String serialized = serializer.serialize(consents);
        Assert.assertEquals(
                serialized,
                "[{\"id\":\"attribute1\",\"v\":\"yePBj0hcjLihhDtDb//R/ymyw2CHZAUreX/4RupmSXM=\"},{\"id\":\"attribute2\",\"v\":\"xxuA06hGJ1DcJ4JSaWiBXXGfcRr6oxHM5jaURXBBnbA=\",\"appr\":false}]");

        final Map<String, Consent> deserialized = serializer.deserialize(1, CONTEXT, KEY, serialized, null);

        Assert.assertEquals(consents, deserialized);
    }

    @Test public void testSymbolics() throws Exception {
        serializer.setSymbolics(ConsentTestingSupport.newSymbolicsMap());

        serializer.initialize();

        final String serialized = serializer.serialize(consents);
        Assert.assertEquals(
                serialized,
                "[{\"id\":201,\"v\":\"yePBj0hcjLihhDtDb//R/ymyw2CHZAUreX/4RupmSXM=\"},{\"id\":202,\"v\":\"xxuA06hGJ1DcJ4JSaWiBXXGfcRr6oxHM5jaURXBBnbA=\",\"appr\":false}]");

        final Map<String, Consent> deserialized = serializer.deserialize(1, CONTEXT, KEY, serialized, null);

        Assert.assertEquals(consents, deserialized);
    }

    @Test public void testSymbolicsWithNulls() throws Exception {
        final Map<String, Integer> symbolics = new HashMap<>();
        symbolics.put("attribute1", null);
        symbolics.put(null, 222);
        serializer.setSymbolics(symbolics);

        serializer.initialize();

        final String serialized = serializer.serialize(consents);
        Assert.assertEquals(
                serialized,
                "[{\"id\":\"attribute1\",\"v\":\"yePBj0hcjLihhDtDb//R/ymyw2CHZAUreX/4RupmSXM=\"},{\"id\":\"attribute2\",\"v\":\"xxuA06hGJ1DcJ4JSaWiBXXGfcRr6oxHM5jaURXBBnbA=\",\"appr\":false}]");

        final Map<String, Consent> deserialized = serializer.deserialize(1, CONTEXT, KEY, serialized, null);

        Assert.assertEquals(consents, deserialized);
    }
}
