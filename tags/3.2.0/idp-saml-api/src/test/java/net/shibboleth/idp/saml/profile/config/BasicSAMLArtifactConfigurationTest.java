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

package net.shibboleth.idp.saml.profile.config;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link BasicSAMLArtifactConfiguration}. */
public class BasicSAMLArtifactConfigurationTest {

    @Test public void testArtifactType() {
        BasicSAMLArtifactConfiguration config = new BasicSAMLArtifactConfiguration();
        
        Assert.assertNull(config.getArtifactType());

        config.setArtifactType(1);
        Assert.assertEquals(config.getArtifactType(), new byte[] {0x0, 0x1});
        
        config.setArtifactType(null);
        Assert.assertNull(config.getArtifactType());
    }

    @Test public void testURL() {
        BasicSAMLArtifactConfiguration config = new BasicSAMLArtifactConfiguration();
        
        Assert.assertNull(config.getArtifactResolutionServiceURL());

        config.setArtifactResolutionServiceURL("https://idp.example.org/artifact");
        Assert.assertEquals(config.getArtifactResolutionServiceURL(), "https://idp.example.org/artifact");
        
        config.setArtifactResolutionServiceURL("  ");
        Assert.assertNull(config.getArtifactResolutionServiceURL());
    }
        
    @Test public void testIndex() {
        BasicSAMLArtifactConfiguration config = new BasicSAMLArtifactConfiguration();
        
        Assert.assertNull(config.getArtifactResolutionServiceIndex());

        config.setArtifactResolutionServiceIndex(1);
        Assert.assertEquals(config.getArtifactResolutionServiceIndex(), Integer.valueOf(1));
        
        config.setArtifactResolutionServiceIndex(null);
        Assert.assertNull(config.getArtifactResolutionServiceIndex());
    }

}