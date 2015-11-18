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

package net.shibboleth.idp.ui.context;

import java.util.Arrays;
import java.util.Collections;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 */
public class RelyingPartyUIContextTest extends XMLObjectBaseTestCase {

    private EntityDescriptor[] theEntities = new EntityDescriptor[4];

    private SPSSODescriptor[] theSPSSOs = new SPSSODescriptor[4];

    private AttributeConsumingService[] theACSs = new AttributeConsumingService[4];

    private UIInfo[] theUiInfos = new UIInfo[4];

    private RelyingPartyUIContext getContext() {
        return getContext(0);
    }

    private RelyingPartyUIContext getContext(int which) {
        RelyingPartyUIContext result = new RelyingPartyUIContext();
        result.setRPEntityDescriptor(theEntities[which]);
        if (null != theACSs[which]) {
            result.setRPAttributeConsumingService(theACSs[which]);
        }
        if (null != theSPSSOs[which]) {
            result.setRPSPSSODescriptor(theSPSSOs[which]);
        }
        if (null != theUiInfos[which]) {
            result.setRPUInfo(theUiInfos[which]);
        }
        result.setBrowserLanguages(Arrays.asList("en", "fr"));
        return result;
    }

    @BeforeClass public void setup() {
        theEntities[0] = unmarshallElement("/net/shibboleth/idp/ui/example-metadata.xml");
        theSPSSOs[0] = theEntities[0].getSPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol");
        final Extensions exts = theSPSSOs[0].getExtensions();
        if (exts != null) {
            for (XMLObject object : exts.getOrderedChildren()) {
                if (object instanceof UIInfo) {
                    theUiInfos[0] = (UIInfo) object;
                }
            }
        }
        theACSs[0] = theSPSSOs[0].getDefaultAttributeConsumingService();
        Assert.assertNotNull(theEntities[0]);
        Assert.assertNotNull(theSPSSOs[0]);
        Assert.assertNotNull(theACSs[0]);

        theEntities[1] = unmarshallElement("/net/shibboleth/idp/ui/example-metadata2.xml");
        Assert.assertNotNull(theEntities[1]);
        theSPSSOs[1] = null;
        theACSs[1] = null;
        theUiInfos[1] = null;

        theEntities[2] = unmarshallElement("/net/shibboleth/idp/ui/example-metadata3.xml");
        Assert.assertNotNull(theEntities[2]);
        theSPSSOs[2] = null;
        theACSs[2] = null;
        theUiInfos[2] = null;

        theEntities[3] = theEntities[0];
        theSPSSOs[3] = theSPSSOs[0];
        theACSs[3] = theACSs[0];
        theUiInfos[3] = null;
    }

    @Test public void givenName() {
        RelyingPartyUIContext ctx = getContext();

        Assert.assertEquals(ctx.getContactGivenName("administrative"), "GNadministrative");
        Assert.assertEquals(ctx.getContactGivenName("billing"), "GNbilling");
        Assert.assertEquals(ctx.getContactGivenName("other" ), null);
        Assert.assertEquals(ctx.getContactGivenName("support"), "GNsupport");
        Assert.assertEquals(ctx.getContactGivenName("technical"), "GNtechnical");
    }

    @Test public void surName() {
        RelyingPartyUIContext ctx = getContext();

        Assert.assertEquals(ctx.getContactSurName("administrative"), "Admin");
        Assert.assertEquals(ctx.getContactSurName("other"), "OTHER");
        Assert.assertNull(ctx.getContactSurName("billing"));
    }

    @Test public void email() {
        RelyingPartyUIContext ctx = getContext();

        Assert.assertEquals(ctx.getContactEmail("administrative"), "mailto:administrative@example.org");
        Assert.assertEquals(ctx.getContactEmail("other") , null);
        Assert.assertEquals(ctx.getContactEmail("support"), null);
    }

    @Test public void organizationXX() {
        RelyingPartyUIContext ctx = getContext();

        Assert.assertEquals(ctx.getOrganizationDisplayName(), "The Shibboleth Consortium");
        Assert.assertEquals(ctx.getOrganizationName(), null);
        Assert.assertEquals(ctx.getOrganizationURL(), null);
    }

    @Test public void service() {
        RelyingPartyUIContext ctx = getContext();

        Assert.assertEquals(ctx.getServiceName(), "TEST SP (display Name)");
        Assert.assertEquals(ctx.getServiceDescription(), "TEST SP (description)");

        ctx = getContext(1);
        Assert.assertEquals(ctx.getServiceName(), "sp.example.org");

        ctx = getContext(2);
        Assert.assertEquals(ctx.getServiceName(), "urn:sp.example.org");

        ctx = getContext(3);
        Assert.assertEquals(ctx.getServiceName(), "le Service Name");
        Assert.assertEquals(ctx.getServiceDescription(), "The ServiceDescription");
    }

    @Test public void urls() {
        RelyingPartyUIContext ctx = getContext();

        Assert.assertEquals(ctx.getInformationURL(), "https://www.example.org");
        Assert.assertEquals(ctx.getPrivacyStatementURL(), "https://www.example.org/privacy");

        ctx = getContext(3);
        Assert.assertEquals(ctx.getInformationURL(),null);
        Assert.assertEquals(ctx.getPrivacyStatementURL(), null);
    }

    @Test public void logo() {
        RelyingPartyUIContext ctx = getContext();
        
        Assert.assertNull(ctx.getLogo(66, 1, 100, 10000));
        Assert.assertEquals(ctx.getLogo(), "https://shibboleth.net/images/shibboleth.png");
        
        ctx.setBrowserLanguages(Collections.singletonList("de"));
        Assert.assertEquals(ctx.getLogo(), "https://shibboleth.net/images/shibboleth.pngde");

    }
    
    @Test public void fallbackLanguage() {
        RelyingPartyUIContext ctx = getContext();
        
        ctx.setBrowserLanguages(Collections.singletonList("improbable"));
        Assert.assertEquals(ctx.getLogo(), "https://shibboleth.net/images/shibboleth.png");
        ctx.setFallbackLanguages(Collections.singletonList("de"));
        Assert.assertEquals(ctx.getLogo(), "https://shibboleth.net/images/shibboleth.pngde");

    }


}
