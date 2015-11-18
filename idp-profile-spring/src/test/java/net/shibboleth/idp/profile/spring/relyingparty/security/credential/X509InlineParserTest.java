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

package net.shibboleth.idp.profile.spring.relyingparty.security.credential;

import java.io.IOException;

import net.shibboleth.idp.profile.spring.relyingparty.security.AbstractSecurityParserTest;

import org.joda.time.DateTime;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.springframework.beans.factory.BeanCreationException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test parsing X509 Inline Credentials
 */
public class X509InlineParserTest extends AbstractSecurityParserTest {

    @Test public void certOnly() throws IOException {
        final BasicX509Credential credential =
                (BasicX509Credential) getBean(Credential.class, true, "credential/inlineCertOnly.xml");

        Assert.assertEquals(credential.getEntityCertificateChain().size(), 1);
        Assert.assertTrue(credential.getEntityCertificateChain().contains(credential.getEntityCertificate()));

        Assert.assertEquals(credential.getEntityCertificate().getNotAfter().getTime(), DateTime.parse("2024-04-08T13:39:18Z").getMillis());
    }
    
    @Test(expectedExceptions={BeanCreationException.class,}) public void wrongCert() throws IOException {
        getBean(Credential.class, true, "credential/inlineWrongCert.xml");
    }
    
    @Test public void certKeyCrl() throws IOException {
        final BasicX509Credential credential =
                (BasicX509Credential) getBean(Credential.class, true, "credential/inlineKeyCertCrl.xml");

        Assert.assertEquals(credential.getEntityCertificate().getNotAfter().getTime(), DateTime.parse("2024-04-08T13:39:18Z").getMillis());
        Assert.assertEquals(credential.getEntityCertificateChain().size(), 3);
        Assert.assertTrue(credential.getEntityCertificateChain().contains(credential.getEntityCertificate()));
        Assert.assertEquals(credential.getCRLs().size(), 1);
        Assert.assertEquals(credential.getCRLs().iterator().next().getNextUpdate().getTime(), DateTime.parse("2007-09-02T14:14:48Z").getMillis());
    }
    
}