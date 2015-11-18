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
import org.opensaml.saml.metadata.resolver.filter.impl.RequiredValidUntilFilter;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for parser for RequiredValidUntil filter
 */
public class RequiredValidUntilParserTest extends AbstractMetadataParserTest {

    @Test public void validUntil() throws IOException {
        MetadataResolver resolver = getBean(MetadataResolver.class, "filter/requiredValidUntil.xml");

        final RequiredValidUntilFilter filter = (RequiredValidUntilFilter) resolver.getMetadataFilter();
        Assert.assertEquals(filter.getMaxValidityInterval(), 0);
    }
    
    @Test public void param() throws IOException {
        MetadataResolver resolver = getBean(MetadataResolver.class, "filter/requiredValidUntilParam.xml");

        final RequiredValidUntilFilter filter = (RequiredValidUntilFilter) resolver.getMetadataFilter();
        Assert.assertEquals(filter.getMaxValidityInterval(), 2*3600*24*1000);
    }

    @Test public void nonDuration() throws IOException {
        MetadataResolver resolver = getBean(MetadataResolver.class, "filter/requiredValidUntilParamNonDuration.xml");

        final RequiredValidUntilFilter filter = (RequiredValidUntilFilter) resolver.getMetadataFilter();
        Assert.assertEquals(filter.getMaxValidityInterval(), 2*1000);
    }
}
