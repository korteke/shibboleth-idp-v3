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

package net.shibboleth.idp.saml.profile.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit test for {@link DefaultNameIdentifierFormatStrategy}. */
public class DefaultNameIdentifierFormatStrategyTest extends OpenSAMLInitBaseTestCase {

    private SPSSODescriptor role;
    
    private BrowserSSOProfileConfiguration profileConfig;
    
    private ProfileRequestContext prc;
    
    private DefaultNameIdentifierFormatStrategy strategy;
    
    @BeforeMethod
    public void setUp() throws ComponentInitializationException {
        SAMLObjectBuilder<EntityDescriptor> edBuilder = (SAMLObjectBuilder<EntityDescriptor>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<EntityDescriptor>getBuilderOrThrow(
                        EntityDescriptor.DEFAULT_ELEMENT_NAME);
        SAMLObjectBuilder<SPSSODescriptor> roleBuilder = (SAMLObjectBuilder<SPSSODescriptor>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<SPSSODescriptor>getBuilderOrThrow(
                        SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        
        profileConfig = new BrowserSSOProfileConfiguration();
        prc = new RequestContextBuilder().setRelyingPartyProfileConfigurations(
                Collections.<ProfileConfiguration>singletonList(profileConfig)).buildProfileRequestContext();
        prc.setProfileId(BrowserSSOProfileConfiguration.PROFILE_ID);
        
        final EntityDescriptor entity = edBuilder.buildObject();
        role = roleBuilder.buildObject();
        entity.getRoleDescriptors().add(role);
        prc.getInboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true).getSubcontext(
                SAMLMetadataContext.class, true).setEntityDescriptor(entity);
        prc.getInboundMessageContext().getSubcontext(SAMLPeerEntityContext.class).getSubcontext(
                SAMLMetadataContext.class).setRoleDescriptor(entity.getRoleDescriptors().get(0));
        
        strategy = new DefaultNameIdentifierFormatStrategy();
    }
    
    @Test
    public void testNoConfiguration() {
        final ProfileRequestContext context = new ProfileRequestContext();
        final List<String> formats = strategy.apply(context);
        Assert.assertEquals(formats, Collections.singletonList(NameID.UNSPECIFIED));
    }

    @Test
    public void testNoFormats() {
        final List<String> formats = strategy.apply(prc);
        Assert.assertEquals(formats, Collections.singletonList(NameID.UNSPECIFIED));
    }
    
    @Test
    public void testNoMetadata() {
        profileConfig.setNameIDFormatPrecedence(Arrays.asList(NameID.EMAIL, NameID.TRANSIENT));
        final List<String> formats = strategy.apply(prc);
        Assert.assertEquals(formats, Arrays.asList(NameID.EMAIL, NameID.TRANSIENT));
    }
    
    @Test
    public void testNoProfileConfig() {
        SAMLObjectBuilder<NameIDFormat> formatBuilder = (SAMLObjectBuilder<NameIDFormat>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<NameIDFormat>getBuilderOrThrow(
                        NameIDFormat.DEFAULT_ELEMENT_NAME);
        NameIDFormat format = formatBuilder.buildObject();
        format.setFormat(NameID.EMAIL);
        role.getNameIDFormats().add(format);
        format = formatBuilder.buildObject();
        format.setFormat(NameID.TRANSIENT);
        role.getNameIDFormats().add(format);

        final List<String> formats = strategy.apply(prc);
        Assert.assertEquals(formats, Arrays.asList(NameID.EMAIL, NameID.TRANSIENT));
    }
    
    @Test
    public void testNoOverlap() {
        profileConfig.setNameIDFormatPrecedence(Arrays.asList(NameID.EMAIL, NameID.TRANSIENT));

        SAMLObjectBuilder<NameIDFormat> formatBuilder = (SAMLObjectBuilder<NameIDFormat>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<NameIDFormat>getBuilderOrThrow(
                        NameIDFormat.DEFAULT_ELEMENT_NAME);
        NameIDFormat format = formatBuilder.buildObject();
        format.setFormat(NameID.PERSISTENT);
        role.getNameIDFormats().add(format);
        format = formatBuilder.buildObject();
        format.setFormat(NameID.X509_SUBJECT);
        role.getNameIDFormats().add(format);

        final List<String> formats = strategy.apply(prc);
        Assert.assertTrue(formats.isEmpty());
    }

    @Test
    public void testOverlap() {
        profileConfig.setNameIDFormatPrecedence(Arrays.asList(NameID.EMAIL, NameID.TRANSIENT));

        SAMLObjectBuilder<NameIDFormat> formatBuilder = (SAMLObjectBuilder<NameIDFormat>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<NameIDFormat>getBuilderOrThrow(
                        NameIDFormat.DEFAULT_ELEMENT_NAME);
        NameIDFormat format = formatBuilder.buildObject();
        format.setFormat(NameID.TRANSIENT);
        role.getNameIDFormats().add(format);
        format = formatBuilder.buildObject();
        format.setFormat(NameID.X509_SUBJECT);
        role.getNameIDFormats().add(format);

        final List<String> formats = strategy.apply(prc);
        Assert.assertEquals(formats, Collections.singletonList(NameID.TRANSIENT));
    }

    @Test
    public void testUnspecifiedInMetadata() {
        profileConfig.setNameIDFormatPrecedence(Arrays.asList(NameID.EMAIL, NameID.TRANSIENT));

        SAMLObjectBuilder<NameIDFormat> formatBuilder = (SAMLObjectBuilder<NameIDFormat>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<NameIDFormat>getBuilderOrThrow(
                        NameIDFormat.DEFAULT_ELEMENT_NAME);
        NameIDFormat format = formatBuilder.buildObject();
        format.setFormat(NameID.TRANSIENT);
        role.getNameIDFormats().add(format);
        format = formatBuilder.buildObject();
        format.setFormat(NameID.UNSPECIFIED);
        role.getNameIDFormats().add(format);
        
        final List<String> formats = strategy.apply(prc);
        Assert.assertEquals(formats, Arrays.asList(NameID.EMAIL, NameID.TRANSIENT));
    }
    
}