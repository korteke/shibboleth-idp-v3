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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.shibboleth.idp.profile.spring.relyingparty.security.AbstractSecurityParserTest;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.security.trust.TrustEngine;
import org.opensaml.security.x509.PKIXValidationInformation;
import org.opensaml.security.x509.PKIXValidationOptions;
import org.opensaml.security.x509.impl.BasicPKIXValidationInformation;
import org.opensaml.security.x509.impl.CertPathPKIXTrustEvaluator;
import org.opensaml.security.x509.impl.CertPathPKIXValidationOptions;
import org.opensaml.security.x509.impl.StaticPKIXValidationInformationResolver;
import org.opensaml.xmlsec.signature.support.impl.PKIXSignatureTrustEngine;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for xsi:type="security:StaticPKIXKeySignature".
 */
public class StaticPKIXSignatureParserTest extends AbstractSecurityParserTest {

    @Test public void simple() throws IOException, ResolverException {
        final PKIXSignatureTrustEngine engine =
                (PKIXSignatureTrustEngine) getBean(TrustEngine.class, true, "trustengine/staticPKIX.xml");
        
        Assert.assertNotNull(engine.getX509CredentialNameEvaluator());

        final StaticPKIXValidationInformationResolver resolver =
                (StaticPKIXValidationInformationResolver) engine.getPKIXResolver();
        Assert.assertTrue(resolver.resolveTrustedNames(null).isEmpty());

        final List<PKIXValidationInformation> infos = new ArrayList<>();
        for (final PKIXValidationInformation info : resolver.resolve(null)) {
            infos.add(info);
        }
        Assert.assertEquals(infos.size(), 1);

        final CertPathPKIXTrustEvaluator trustEvaluator = (CertPathPKIXTrustEvaluator) engine.getPKIXTrustEvaluator();
        final PKIXValidationOptions options = trustEvaluator.getPKIXValidationOptions();
        Assert.assertTrue(options.isProcessCredentialCRLs());
        Assert.assertTrue(options.isProcessEmptyCRLs());
        Assert.assertTrue(options.isProcessExpiredCRLs());
        Assert.assertEquals(options.getDefaultVerificationDepth().intValue(), 1);
    }
    
    @Test public void nameCheckDisabled() throws IOException, ResolverException {
        final PKIXSignatureTrustEngine engine =
                (PKIXSignatureTrustEngine) getBean(TrustEngine.class, true, "trustengine/staticPKIX-nameCheckDisabled.xml");
        
        Assert.assertNull(engine.getX509CredentialNameEvaluator());

        final StaticPKIXValidationInformationResolver resolver =
                (StaticPKIXValidationInformationResolver) engine.getPKIXResolver();
        Assert.assertTrue(resolver.resolveTrustedNames(null).isEmpty());

        final List<PKIXValidationInformation> infos = new ArrayList<>();
        for (final PKIXValidationInformation info : resolver.resolve(null)) {
            infos.add(info);
        }
        Assert.assertEquals(infos.size(), 1);

        final CertPathPKIXTrustEvaluator trustEvaluator = (CertPathPKIXTrustEvaluator) engine.getPKIXTrustEvaluator();
        final PKIXValidationOptions options = trustEvaluator.getPKIXValidationOptions();
        Assert.assertTrue(options.isProcessCredentialCRLs());
        Assert.assertTrue(options.isProcessEmptyCRLs());
        Assert.assertTrue(options.isProcessExpiredCRLs());
        Assert.assertEquals(options.getDefaultVerificationDepth().intValue(), 1);
    }

    @Test public void values() throws IOException, ResolverException {
        final PKIXSignatureTrustEngine engine =
                (PKIXSignatureTrustEngine) getBean(TrustEngine.class, true, "trustengine/staticPKIXValues.xml");
        
        Assert.assertNotNull(engine.getX509CredentialNameEvaluator());

        final StaticPKIXValidationInformationResolver resolver =
                (StaticPKIXValidationInformationResolver) engine.getPKIXResolver();
        final Set<String> tns = resolver.resolveTrustedNames(null);
        Assert.assertEquals(tns.size(), 3);
        Assert.assertTrue(tns.contains("Name1"));
        Assert.assertTrue(tns.contains("Name2"));
        Assert.assertTrue(tns.contains("Name3"));

        final List<PKIXValidationInformation> infos = new ArrayList<>();
        for (final PKIXValidationInformation info : resolver.resolve(null)) {
            infos.add(info);
        }
        Assert.assertEquals(infos.size(), 2);
        final int firstVal = ((BasicPKIXValidationInformation) infos.get(0)).getVerificationDepth().intValue();
        final int secondVal = ((BasicPKIXValidationInformation) infos.get(1)).getVerificationDepth().intValue();

        Assert.assertTrue((98 == firstVal) || (99 == firstVal));
        Assert.assertTrue((98 == secondVal) || (99 == secondVal));
        Assert.assertNotEquals(firstVal, secondVal);

        final CertPathPKIXTrustEvaluator trustEvaluator = (CertPathPKIXTrustEvaluator) engine.getPKIXTrustEvaluator();
        final PKIXValidationOptions options = trustEvaluator.getPKIXValidationOptions();
        Assert.assertFalse(options.isProcessCredentialCRLs());
        Assert.assertFalse(options.isProcessEmptyCRLs());
        Assert.assertFalse(options.isProcessExpiredCRLs());
        Assert.assertEquals(options.getDefaultVerificationDepth().intValue(), 2);
    }
    
    @Test public void certPath() throws IOException, ResolverException {
        final PKIXSignatureTrustEngine engine =
                (PKIXSignatureTrustEngine) getBean(TrustEngine.class, true, "trustengine/staticPKIXValuesCertPathOpts.xml");
        
        Assert.assertNotNull(engine.getX509CredentialNameEvaluator());

        final StaticPKIXValidationInformationResolver resolver =
                (StaticPKIXValidationInformationResolver) engine.getPKIXResolver();
        final Set<String> tns = resolver.resolveTrustedNames(null);
        Assert.assertEquals(tns.size(), 1);
        Assert.assertTrue(tns.contains("Name1"));

        final List<PKIXValidationInformation> infos = new ArrayList<>();
        for (final PKIXValidationInformation info : resolver.resolve(null)) {
            infos.add(info);
        }
        Assert.assertEquals(infos.size(), 1);
        final int value = ((BasicPKIXValidationInformation) infos.get(0)).getVerificationDepth().intValue();

        Assert.assertEquals(value, 99);

        final CertPathPKIXTrustEvaluator trustEvaluator = (CertPathPKIXTrustEvaluator) engine.getPKIXTrustEvaluator();
        final CertPathPKIXValidationOptions options = (CertPathPKIXValidationOptions) trustEvaluator.getPKIXValidationOptions();
        Assert.assertFalse(options.isProcessCredentialCRLs());
        Assert.assertFalse(options.isProcessEmptyCRLs());
        Assert.assertFalse(options.isProcessExpiredCRLs());
        Assert.assertEquals(options.getDefaultVerificationDepth().intValue(), 3);

        Assert.assertFalse(options.isRevocationEnabled());
        Assert.assertTrue(options.isAnyPolicyInhibited());
        Assert.assertTrue(options.isPolicyMappingInhibited());
        Assert.assertTrue(options.isForceRevocationEnabled());
        Assert.assertEquals(options.getInitialPolicies().size(), 1);
        Assert.assertTrue(options.getInitialPolicies().contains("1234"));
    }
    
}