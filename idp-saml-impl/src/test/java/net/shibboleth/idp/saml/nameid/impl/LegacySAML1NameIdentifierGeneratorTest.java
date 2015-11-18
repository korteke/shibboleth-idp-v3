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

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML1StringNameIdentifierEncoder;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit test for {@link LegacySAML1NameIdentifierGenerator}. */
public class LegacySAML1NameIdentifierGeneratorTest extends OpenSAMLInitBaseTestCase {

    /** The name we give the test attribute. */
    private final static String ATTR_NAME = "foo";

    /** test values. */
    private final static String NAME_1 = "NameId1";

    private final static String QUALIFIER = "Qualifier";

    private LegacySAML1NameIdentifierGenerator generator;
    
    private ProfileRequestContext prc;

    @BeforeMethod public void initTest() throws ComponentInitializationException {
        generator = new LegacySAML1NameIdentifierGenerator();
        generator.setId("this");
        generator.initialize();
        prc = new RequestContextBuilder().buildProfileRequestContext();
    }

    @Test public void testNoAttributes() throws Exception {
        NameIdentifier outputNameId = generator.generate(prc, NameIdentifier.X509_SUBJECT);
        Assert.assertNull(outputNameId);

        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true);
        outputNameId = generator.generate(prc, NameIdentifier.X509_SUBJECT);
        Assert.assertNull(outputNameId);
    }
    
    @Test public void testNoValues() throws Exception {
        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);

        final SAML1StringNameIdentifierEncoder encoder = new SAML1StringNameIdentifierEncoder();
        encoder.setNameQualifier(QUALIFIER);
        encoder.setNameFormat(NameIdentifier.EMAIL);
        inputAttribute.setEncoders(Collections.<AttributeEncoder<?>>singleton(encoder));
        
        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setIdPAttributes(
                Collections.singleton(inputAttribute));
        
        NameIdentifier outputNameId = generator.generate(prc, NameIdentifier.X509_SUBJECT);
        Assert.assertNull(outputNameId);
    }
    
    @Test public void testNoEncoders() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values =
                Collections.singletonList(new StringAttributeValue(NAME_1));

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setIdPAttributes(
                Collections.singleton(inputAttribute));
        
        NameIdentifier outputNameId = generator.generate(prc, NameIdentifier.X509_SUBJECT);
        Assert.assertNull(outputNameId);
    }

    @Test public void testWrongFormat() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values =
                Collections.singletonList(new StringAttributeValue(NAME_1));

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        final SAML1StringNameIdentifierEncoder encoder = new SAML1StringNameIdentifierEncoder();
        encoder.setNameQualifier(QUALIFIER);
        encoder.setNameFormat(NameIdentifier.EMAIL);
        inputAttribute.setEncoders(Collections.<AttributeEncoder<?>>singleton(encoder));
        
        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setIdPAttributes(
                Collections.singleton(inputAttribute));
        
        NameIdentifier outputNameId = generator.generate(prc, NameIdentifier.X509_SUBJECT);
        Assert.assertNull(outputNameId);
    }
    
    @Test public void testSingle() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values =
                Collections.singletonList(new StringAttributeValue(NAME_1));

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        final SAML1StringNameIdentifierEncoder encoder = new SAML1StringNameIdentifierEncoder();
        encoder.setNameQualifier(QUALIFIER);
        encoder.setNameFormat(NameIdentifier.EMAIL);
        inputAttribute.setEncoders(Collections.<AttributeEncoder<?>>singleton(encoder));

        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setIdPAttributes(
                Collections.singleton(inputAttribute));
        
        NameIdentifier outputNameId = generator.generate(prc, NameIdentifier.EMAIL);
        Assert.assertNotNull(outputNameId);
        Assert.assertEquals(outputNameId.getValue(), NAME_1);
        Assert.assertEquals(outputNameId.getFormat(), NameIdentifier.EMAIL);
        Assert.assertEquals(outputNameId.getNameQualifier(), QUALIFIER);
    }

    @Test public void testMultiple() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values =
                Collections.singletonList(new StringAttributeValue(NAME_1));

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        final SAML1StringNameIdentifierEncoder encoder = new SAML1StringNameIdentifierEncoder();
        encoder.setNameQualifier(QUALIFIER);
        encoder.setNameFormat(NameIdentifier.EMAIL);
        final SAML1StringNameIdentifierEncoder encoder2 = new SAML1StringNameIdentifierEncoder();
        encoder.setNameQualifier(QUALIFIER);
        encoder.setNameFormat(NameIdentifier.X509_SUBJECT);
        inputAttribute.setEncoders(Arrays.<AttributeEncoder<?>>asList(encoder, encoder2));

        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setIdPAttributes(
                Collections.singleton(inputAttribute));
        
        NameIdentifier outputNameId = generator.generate(prc, NameIdentifier.X509_SUBJECT);
        Assert.assertNotNull(outputNameId);
        Assert.assertEquals(outputNameId.getValue(), NAME_1);
        Assert.assertEquals(outputNameId.getFormat(), NameIdentifier.X509_SUBJECT);
        Assert.assertEquals(outputNameId.getNameQualifier(), QUALIFIER);
    }
}