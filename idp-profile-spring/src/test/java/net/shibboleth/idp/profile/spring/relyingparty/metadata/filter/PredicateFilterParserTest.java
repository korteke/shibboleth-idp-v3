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

import org.opensaml.saml.common.profile.logic.EntityAttributesPredicate;
import org.opensaml.saml.common.profile.logic.EntityAttributesPredicate.Candidate;
import org.opensaml.saml.common.profile.logic.EntityGroupNamePredicate;
import org.opensaml.saml.common.profile.logic.EntityIdPredicate;
import org.opensaml.saml.metadata.resolver.filter.impl.PredicateFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.PredicateFilter.Direction;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for parser for PredicateFilter filter.
 */
public class PredicateFilterParserTest extends AbstractMetadataParserTest {

    @Test(expectedExceptions=BeanDefinitionStoreException.class)
    public void empty() throws IOException {
        getBean(PredicateFilter.class, "filter/predicateEmpty.xml");
    }

    @Test(expectedExceptions=BeanCreationException.class)
    public void badDirection() throws IOException {
        getBean(PredicateFilter.class, "filter/predicateBadDirection.xml");
    }

    @Test
    public void entity() throws IOException {
        final PredicateFilter filter = getBean(PredicateFilter.class, "filter/predicateEntity.xml");
        Assert.assertNotNull(filter);
        Assert.assertEquals(filter.getDirection(), Direction.INCLUDE);
        
        final EntityIdPredicate condition = (EntityIdPredicate) filter.getCondition();
        Assert.assertEquals(condition.getEntityIds().size(), 3);
        Assert.assertTrue(condition.getEntityIds().contains("urn:bar"));
    }

    @Test
    public void group() throws IOException {
        final PredicateFilter filter = getBean(PredicateFilter.class, "filter/predicateGroup.xml");
        Assert.assertNotNull(filter);
        Assert.assertEquals(filter.getDirection(), Direction.INCLUDE);
        
        final EntityGroupNamePredicate condition = (EntityGroupNamePredicate) filter.getCondition();
        Assert.assertEquals(condition.getGroupNames().size(), 3);
        Assert.assertTrue(condition.getGroupNames().contains("urn:bar"));
    }

    @Test
    public void tag() throws IOException {
        final PredicateFilter filter = getBean(PredicateFilter.class, "filter/predicateTag.xml");
        Assert.assertNotNull(filter);
        Assert.assertEquals(filter.getDirection(), Direction.EXCLUDE);
        
        final EntityAttributesPredicate condition = (EntityAttributesPredicate) filter.getCondition();
        Assert.assertTrue(condition.getTrimTags());
        Assert.assertEquals(condition.getCandidates().size(), 2);
        
        final Candidate c1 = (Candidate) condition.getCandidates().toArray()[0];
        final Candidate c2 = (Candidate) condition.getCandidates().toArray()[1];
        Assert.assertEquals(c1.getValues().size(), 2);
        Assert.assertEquals(c2.getValues().size(), 2);
        if (c1.getName().equals("urn:foo")) {
            Assert.assertNull(c1.getNameFormat());
            Assert.assertEquals(c2.getNameFormat(), "foo");
        } else {
            Assert.assertNull(c2.getNameFormat());
            Assert.assertEquals(c1.getNameFormat(), "foo");
        }
    }

    @Test
    public void or() throws IOException {
        final PredicateFilter filter = getBean(PredicateFilter.class, "filter/predicateOr.xml");
        Assert.assertNotNull(filter);
        Assert.assertEquals(filter.getDirection(), Direction.EXCLUDE);
        Assert.assertNotNull(filter.getCondition());
        Assert.assertFalse(filter.getCondition() instanceof EntityIdPredicate);
        Assert.assertFalse(filter.getCondition() instanceof EntityGroupNamePredicate);
        Assert.assertFalse(filter.getCondition() instanceof EntityAttributesPredicate);
    }

}