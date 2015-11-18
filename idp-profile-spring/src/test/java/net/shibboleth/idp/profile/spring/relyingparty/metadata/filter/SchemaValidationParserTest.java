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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.filter;

import java.io.IOException;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataParserTest;

import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.impl.SchemaValidationFilter;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for parser for SchemaValidation filter
 */
public class SchemaValidationParserTest extends AbstractMetadataParserTest {

    @Test public void validUntil() throws IOException {
        MetadataResolver resolver = getBean(MetadataResolver.class, "filter/schemaValidation.xml", "beans.xml");

        Assert.assertEquals(resolver.getMetadataFilter().getClass(), SchemaValidationFilter.class); 
    }
    
    @Test public void param() throws IOException {
        MetadataResolver resolver = getBean(MetadataResolver.class, "filter/schemaValidationParam.xml", "beans.xml");

        Assert.assertEquals(resolver.getMetadataFilter().getClass(), SchemaValidationFilter.class); 
    }

    @Test public void bean() throws IOException {
        MetadataResolver resolver = getBean(MetadataResolver.class, "filter/schemaValidationRefs.xml", "filter/schemaValidationRefs-beans.xml");

        Assert.assertEquals(resolver.getMetadataFilter().getClass(), SchemaValidationFilter.class); 
    }
}