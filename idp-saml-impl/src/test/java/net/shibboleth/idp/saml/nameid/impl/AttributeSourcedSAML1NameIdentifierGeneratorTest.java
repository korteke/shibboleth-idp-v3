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

package net.shibboleth.idp.saml.nameid.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.XMLObjectAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml1.core.impl.NameIdentifierBuilder;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit test for {@link AttributeSourcedSAML1NameIdentifierGenerator}. */
public class AttributeSourcedSAML1NameIdentifierGeneratorTest extends OpenSAMLInitBaseTestCase {

    /** The name we give the test attribute. */
    private final static String ATTR_NAME = "foo";

    private static NameIdentifierBuilder saml1Builder;

    private static NameIDBuilder saml2Builder;

    /** test values. */
    private final static String NAME_1 = "NameId1";

    private final static String OTHERID = "NameOtherProtocol";

    private final static String QUALIFIER = "Qualifier";

    private static IdPAttributeValue<?> saml1NameIdFor(final String ident) {
        NameIdentifier id = saml1Builder.buildObject();

        id.setValue(ident);
        id.setFormat(NameIdentifier.X509_SUBJECT);
        id.setNameQualifier(QUALIFIER);
        return new XMLObjectAttributeValue(id);
    }

    private static IdPAttributeValue<?> saml2NameIdFor(final String ident) {
        NameID id = saml2Builder.buildObject();

        id.setValue(ident);
        id.setFormat(NameID.X509_SUBJECT);
        id.setNameQualifier(QUALIFIER);
        return new XMLObjectAttributeValue(id);
    }

    private AttributeSourcedSAML1NameIdentifierGenerator generator;

    private ProfileRequestContext prc;

    @BeforeMethod public void initTest() throws ComponentInitializationException {

        generator = new AttributeSourcedSAML1NameIdentifierGenerator();
        generator.setId("test");
        generator.setFormat(NameIdentifier.X509_SUBJECT);
        saml1Builder = new NameIdentifierBuilder();
        saml2Builder = new NameIDBuilder();
        prc = new RequestContextBuilder().buildProfileRequestContext();
    }

    @Test(expectedExceptions = {ComponentInitializationException.class,}) public void testInvalidConfig()
            throws Exception {
        generator.initialize();
    }

    @Test public void testNoSource() throws ComponentInitializationException, SAMLException {
        generator.setAttributeSourceIds(Collections.singletonList("bar"));
        generator.initialize();
        Assert.assertNull(generator.generate(prc, generator.getFormat()));
    }

    @Test public void testWrongType() throws Exception {
        final int[] intArray = {1, 2, 3, 4};
        final Collection<? extends IdPAttributeValue<?>> values = Arrays.asList(new IdPAttributeValue<Object>() {
            public Object getValue() {
                return intArray;
            }
            public String getDisplayValue() {
                return intArray.toString();
            }
        }, saml2NameIdFor(OTHERID));

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);
        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setIdPAttributes(
                Collections.singleton(inputAttribute));

        generator.setAttributeSourceIds(Collections.singletonList(ATTR_NAME));
        generator.initialize();

        Assert.assertNull(generator.generate(prc, generator.getFormat()));
    }

    @Test public void testWrongFormat() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values = Collections.singletonList(saml1NameIdFor(NAME_1));
        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);
        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setIdPAttributes(
                Collections.singleton(inputAttribute));

        generator.setFormat(NameIdentifier.EMAIL);
        generator.setAttributeSourceIds(Collections.singletonList(ATTR_NAME));
        generator.initialize();
        Assert.assertNull(generator.generate(prc, generator.getFormat()));
    }

    @Test public void testNameIdentifierValued() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values = Collections.singletonList(saml1NameIdFor(NAME_1));
        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);
        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setIdPAttributes(
                Collections.singleton(inputAttribute));

        generator.setAttributeSourceIds(Collections.singletonList(ATTR_NAME));
        generator.initialize();
        final NameIdentifier outputNameId = generator.generate(prc, generator.getFormat());

        Assert.assertNotNull(outputNameId);
        Assert.assertEquals(outputNameId.getValue(), NAME_1);
        Assert.assertEquals(outputNameId.getFormat(), NameIdentifier.X509_SUBJECT);
        Assert.assertEquals(outputNameId.getNameQualifier(), QUALIFIER);
    }

    @Test public void testMultiNameIdentifierValued() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values =
                Arrays.asList(saml2NameIdFor(OTHERID), saml1NameIdFor(NAME_1));

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);
        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setIdPAttributes(
                Collections.singleton(inputAttribute));

        generator.setAttributeSourceIds(Collections.singletonList(ATTR_NAME));
        generator.initialize();
        final NameIdentifier outputNameId = generator.generate(prc, generator.getFormat());

        Assert.assertNotNull(outputNameId);
        Assert.assertEquals(outputNameId.getValue(), NAME_1);
        Assert.assertEquals(outputNameId.getFormat(), NameIdentifier.X509_SUBJECT);
        Assert.assertEquals(outputNameId.getNameQualifier(), QUALIFIER);
    }

    @Test public void testStringValued() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values =
                Collections.singletonList(new StringAttributeValue(NAME_1));

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);
        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setIdPAttributes(
                Collections.singleton(inputAttribute));

        generator.setAttributeSourceIds(Collections.singletonList(ATTR_NAME));
        generator.initialize();
        final NameIdentifier outputNameId = generator.generate(prc, generator.getFormat());

        Assert.assertNotNull(outputNameId);
        Assert.assertEquals(outputNameId.getValue(), NAME_1);
        Assert.assertEquals(outputNameId.getFormat(), NameIdentifier.X509_SUBJECT);
        Assert.assertEquals(outputNameId.getNameQualifier(), prc.getSubcontext(RelyingPartyContext.class)
                .getConfiguration().getResponderId());
    }

    @Test public void testScopeValued() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values =
                Collections.singletonList(new ScopedStringAttributeValue(NAME_1, QUALIFIER));

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);
        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setIdPAttributes(
                Collections.singleton(inputAttribute));

        generator.setAttributeSourceIds(Collections.singletonList(ATTR_NAME));
        generator.initialize();
        final NameIdentifier outputNameId = generator.generate(prc, generator.getFormat());

        Assert.assertNotNull(outputNameId);
        Assert.assertEquals(outputNameId.getValue(), NAME_1 + '@' + QUALIFIER);
        Assert.assertEquals(outputNameId.getFormat(), NameIdentifier.X509_SUBJECT);
        Assert.assertEquals(outputNameId.getNameQualifier(), prc.getSubcontext(RelyingPartyContext.class)
                .getConfiguration().getResponderId());
    }
}