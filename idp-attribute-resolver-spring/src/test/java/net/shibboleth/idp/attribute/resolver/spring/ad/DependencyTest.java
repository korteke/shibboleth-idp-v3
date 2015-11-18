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

package net.shibboleth.idp.attribute.resolver.spring.ad;

import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.ResolverPluginDependency;
import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.service.ServiceException;

import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/** A work in progress to test the attribute resolver service. */

public class DependencyTest extends BaseAttributeDefinitionParserTest {
    
    private ResolverPluginDependency getDependency(String fileName) {

        GenericApplicationContext context = new GenericApplicationContext();
        setTestContext(context);
        context.setDisplayName("ApplicationContext: " + DependencyTest.class);

        return getBean(ATTRIBUTE_FILE_PATH + fileName, ResolverPluginDependency.class, context);
    }

    @Test public void testOrphan() throws ComponentInitializationException, ServiceException, ResolutionException {
        ResolverPluginDependency depend = getDependency("orphanDependency.xml");
        
        Assert.assertEquals(depend.getDependencyPluginId(), "TheOrphan");
    }

    @Test public void testNoId() throws ComponentInitializationException, ServiceException, ResolutionException {
        ResolverPluginDependency depend = getDependency("noIdInParentDependency.xml");
        
        Assert.assertEquals(depend.getDependencyPluginId(), "TheHasNotRef");
    }

    @Test public void testId() throws ComponentInitializationException, ServiceException, ResolutionException {
        ResolverPluginDependency depend = getDependency("idInParentDependency.xml");
        
        Assert.assertEquals(depend.getDependencyPluginId(), "TheHasRef");
        Assert.assertNull(depend.getDependencyAttributeId());
    }
}
