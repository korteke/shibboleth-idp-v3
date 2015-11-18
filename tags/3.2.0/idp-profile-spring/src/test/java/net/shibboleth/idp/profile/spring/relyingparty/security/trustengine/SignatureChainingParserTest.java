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

package net.shibboleth.idp.profile.spring.relyingparty.security.trustengine;

import java.io.IOException;

import net.shibboleth.idp.profile.spring.relyingparty.security.AbstractSecurityParserTest;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.joda.time.DateTime;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.security.trust.TrustedCredentialTrustEngine;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.support.impl.ChainingSignatureTrustEngine;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for  xsi:type="security:StaticExplicitKeySignature".
 */
public class SignatureChainingParserTest extends AbstractSecurityParserTest {
 
    @Test public void simple() throws IOException, ResolverException {
        final ChainingSignatureTrustEngine chain =  getBean(ChainingSignatureTrustEngine.class, true, "trustengine/chain.xml");
        
        Assert.assertEquals(chain.getChain().size(),1 );
        
        final TrustedCredentialTrustEngine engine = (TrustedCredentialTrustEngine) chain.getChain().get(0);
        
        final StaticCredentialResolver resolver = (StaticCredentialResolver) engine.getCredentialResolver();
        
        BasicX509Credential credential  = (BasicX509Credential) resolver.resolveSingle(null);
        
        Assert.assertEquals(credential.getEntityCertificateChain().size(), 1);
        Assert.assertTrue(credential.getEntityCertificateChain().contains(credential.getEntityCertificate()));

        Assert.assertEquals(credential.getEntityCertificate().getNotAfter().getTime(), DateTime.parse("2024-04-08T13:39:18Z").getMillis());
        
    }
}
