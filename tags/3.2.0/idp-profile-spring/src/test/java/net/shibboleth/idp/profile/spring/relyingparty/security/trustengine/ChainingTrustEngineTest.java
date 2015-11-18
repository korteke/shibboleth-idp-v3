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

import org.opensaml.security.SecurityException;
import org.opensaml.security.trust.impl.ChainingTrustEngine;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ChainingTrustEngineTest extends AbstractSecurityParserTest {
    
    private ChainingTrustEngine lookup(String file) throws IOException {
        return getBean(ChainingTrustEngine.class, true, "trustengine/" + file);
    }

    
    @Test public void one() throws IOException, SecurityException {
        final ChainingTrustEngine engine = lookup("chainingTrue.xml");
        
        Assert.assertEquals(engine.getChain().size(), 1);
        Assert.assertTrue(engine.validate(null, null));
    }

    @Test public void two() throws IOException, SecurityException {
        final ChainingTrustEngine engine = lookup("chainingTrueFalse.xml");
        
        Assert.assertEquals(engine.getChain().size(), 2);
        Assert.assertTrue(engine.validate(null, null));
    }
}
