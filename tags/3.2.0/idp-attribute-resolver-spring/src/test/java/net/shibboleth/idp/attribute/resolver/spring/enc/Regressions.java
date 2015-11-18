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

package net.shibboleth.idp.attribute.resolver.spring.enc;

import java.util.Collection;

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;

import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 */
public class Regressions extends BaseAttributeDefinitionParserTest {

    @Test public void idp571() {
        GenericApplicationContext context = new GenericApplicationContext();
        setTestContext(context);
        loadFile(ENCODER_FILE_PATH + "idp-571.xml", context);
        context.refresh();
     
        Collection<AttributeEncoder> encoders = context.getBeansOfType(AttributeEncoder.class).values();
        Collection<AttributeDefinition> definitions = context.getBeansOfType(AttributeDefinition.class).values();
        
        Assert.assertEquals(encoders.size(), 1);
        Assert.assertEquals(definitions.size(), 1);
    }
}
