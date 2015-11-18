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
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.impl.SignatureValidationFilter;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * test for parser for SignatureValidation filter
 */
public class SignatureValidationParserTest extends AbstractMetadataParserTest {

    @Test public void correctSig() throws IOException, ResolverException {
        MetadataResolver resolver =
                getBean(MetadataResolver.class, "filter/switch-metadata.xml", "filter/switch.xml", "beans.xml");

        final Iterable<EntityDescriptor> result = resolver.resolve(criteriaFor("urn:mace:switch.ch:SWITCHaai:ethz.ch"));
        Assert.assertTrue(result.iterator().hasNext());
    }

    @Test public void wrongSig() throws IOException, ResolverException {
        MetadataResolver resolver =
                getBean(MetadataResolver.class,  "filter/switch-metadata.xml", "filter/other.xml", "beans.xml");

        final Iterable<EntityDescriptor> result = resolver.resolve(criteriaFor("urn:mace:switch.ch:SWITCHaai:ethz.ch"));
        Assert.assertFalse(result.iterator().hasNext());
    }
    
    @Test public void noSigCheck() throws IOException, ResolverException {
        MetadataResolver resolver =
                getBean(MetadataResolver.class, "filter/signingCertCheck.xml", "filter/switch.xml", "beans.xml");

        final Iterable<EntityDescriptor> result = resolver.resolve(criteriaFor("https://sp.example.org/sp/shibboleth"));
        Assert.assertFalse(result.iterator().hasNext());
    }

    @Test public void noSigNoCheck() throws IOException, ResolverException {
        MetadataResolver resolver =
                getBean(MetadataResolver.class, "filter/signingNoCertCheck.xml", "filter/switch.xml", "beans.xml");

        final Iterable<EntityDescriptor> result = resolver.resolve(criteriaFor("https://sp.example.org/sp/shibboleth"));
        Assert.assertTrue(result.iterator().hasNext());
    }

    @Test public void cert() throws IOException, ResolverException {
        MetadataResolver resolver =
                getBean(MetadataResolver.class, "filter/switch-metadata-file.xml", "beans.xml");

        final Iterable<EntityDescriptor> result = resolver.resolve(criteriaFor("urn:mace:switch.ch:SWITCHaai:ethz.ch"));
        Assert.assertTrue(result.iterator().hasNext());
    }
    
    @Test public void pubkey() throws IOException, ResolverException {
        MetadataResolver resolver =
                getBean(MetadataResolver.class, "filter/switch-metadata-inline.xml", "beans.xml");

        final Iterable<EntityDescriptor> result = resolver.resolve(criteriaFor("urn:mace:switch.ch:SWITCHaai:ethz.ch"));
        Assert.assertTrue(result.iterator().hasNext());
    }
    
    @Test public void inlineTrustEngine() throws IOException, ResolverException {
        MetadataResolver resolver =
                getBean(MetadataResolver.class, "filter/switch-metadata-trustengine-inline.xml", "beans.xml");

        final Iterable<EntityDescriptor> result = resolver.resolve(criteriaFor("urn:mace:switch.ch:SWITCHaai:ethz.ch"));
        Assert.assertTrue(result.iterator().hasNext());
    }
    
    @Test public void inlineFilterTrustEngine() throws IOException, ResolverException {
        MetadataResolver resolver =
                getBean(MetadataResolver.class, "filter/switch-metadata-trustengine-inline-filter.xml", "beans.xml");

        final Iterable<EntityDescriptor> result = resolver.resolve(criteriaFor("urn:mace:switch.ch:SWITCHaai:ethz.ch"));
        Assert.assertTrue(result.iterator().hasNext());
    }

    
    @Test public void optionalFilterRefs() throws IOException {
        ApplicationContext context = 
                getApplicationContext("optionalFilterRefs", "filter/signatureValidationRefs.xml", "filter/switch.xml", "filter/signatureValidationRefs-beans.xml");
        
        SignatureValidationFilter filter = context.getBean(SignatureValidationFilter.class);
        Assert.assertNotNull(filter);
        
        
        // These beans are defined as singletons, so can just test for reference equality
        Assert.assertSame(filter.getDefaultCriteria(), context.getBean("test.CriteriaSet"));
        Assert.assertSame(filter.getSignaturePrevalidator(), context.getBean("test.SignaturePrevalidator"));
        Assert.assertSame(filter.getDynamicTrustedNamesStrategy(), context.getBean("test.DynamicTrustedNamesStrategy"));
        
        // Test the other args just for the hell of it
        Assert.assertFalse(filter.getRequireSignedRoot());
        Assert.assertSame(filter.getSignatureTrustEngine(), context.getBean("bean.pem"));
    }


    @Test(expectedExceptions={BeanDefinitionStoreException.class,}) public void none() throws IOException, ResolverException {
                getBean(MetadataResolver.class, "filter/signingNone.xml", "beans.xml");
    }
    
}