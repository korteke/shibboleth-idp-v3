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
import net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl.PKIXValidationOptionsParser;

import org.opensaml.security.x509.PKIXValidationOptions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link PKIXValidationOptionsParser}.
 */
public class PKIXValidationOptionsParserTest extends AbstractSecurityParserTest {
    
    
    @Test public void simple() throws IOException {
        PKIXValidationOptions what = getBean(PKIXValidationOptions.class, false, "trustengine/validationOptions.xml");

        Assert.assertTrue(what.isProcessCredentialCRLs());
        Assert.assertTrue(what.isProcessEmptyCRLs());
        Assert.assertTrue(what.isProcessExpiredCRLs());
        Assert.assertEquals(what.getDefaultVerificationDepth(), new Integer(1));
    }
    
    @Test public void complex() throws IOException {
        PKIXValidationOptions what = getBean(PKIXValidationOptions.class, false, "trustengine/validationOptionsValues.xml");

        Assert.assertFalse(what.isProcessCredentialCRLs());
        Assert.assertFalse(what.isProcessEmptyCRLs());
        Assert.assertTrue(what.isProcessExpiredCRLs());
        Assert.assertEquals(what.getDefaultVerificationDepth(), new Integer(2));
    }

}
