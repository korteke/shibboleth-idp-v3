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

package net.shibboleth.idp.attribute.filter.spring.policy;

import javax.annotation.Nullable;

import net.shibboleth.ext.spring.util.SchemaTypeAwareXMLBeanDefinitionReader;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.policyrule.filtercontext.impl.PredicatePolicyRule;
import net.shibboleth.idp.attribute.filter.spring.BaseAttributeFilterParserTest;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class PredicateRuleParserTest extends BaseAttributeFilterParserTest {

    @Test public void policy() throws ComponentInitializationException {
        policy(true);
        policy(false);
    }

    public void policy(boolean isAfp) throws ComponentInitializationException {
        GenericApplicationContext ctx = new GenericApplicationContext();
        setTestContext(ctx);
        SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader = new SchemaTypeAwareXMLBeanDefinitionReader(ctx);

        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource(BaseAttributeFilterParserTest.POLICY_RULE_PATH + (isAfp?"afp/":"")
                + "predicate1.xml"), new ClassPathResource(BaseAttributeFilterParserTest.POLICY_RULE_PATH
                + "predicateBeans.xml"));

        ctx.refresh();

        final PredicatePolicyRule rule = ctx.getBean(PredicatePolicyRule.class);

        Assert.assertEquals(rule.getRulePredicate().getClass(), Foo.class);
        Assert.assertNull(rule.getProfileContextStrategy().apply(new AttributeFilterContext()));
        ProfileRequestContext pc = new ProfileRequestContext<>();
        Assert.assertSame(
                rule.getProfileContextStrategy().apply(
                        pc.getSubcontext(RelyingPartyContext.class, true).getSubcontext(AttributeFilterContext.class,
                                true)), pc);
    }

    @Test public void strategy() throws ComponentInitializationException {
        strategy(true);
        strategy(false);
    }
    
    public void strategy(boolean isAfp) throws ComponentInitializationException {
        GenericApplicationContext ctx = new GenericApplicationContext();
        setTestContext(ctx);
        SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader = new SchemaTypeAwareXMLBeanDefinitionReader(ctx);

        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource(BaseAttributeFilterParserTest.POLICY_RULE_PATH 
                + "predicateBeans.xml"), new ClassPathResource(BaseAttributeFilterParserTest.POLICY_RULE_PATH + (isAfp?"afp/":"")
                + "predicate2.xml"));

        ctx.refresh();

        final PredicatePolicyRule rule = ctx.getBean(PredicatePolicyRule.class);

        Assert.assertEquals(rule.getRulePredicate().getClass(), Foo.class);
        Assert.assertNotNull(rule.getProfileContextStrategy().apply(new AttributeFilterContext()));
        Assert.assertEquals(rule.getProfileContextStrategy().getClass(), Func.class);
    }

    private AttributeFilterContext prcFor(String sp) {

        final ProfileRequestContext prc = new ProfileRequestContext();
        final  RelyingPartyContext rpc = prc.getSubcontext(RelyingPartyContext.class, true);rpc.setRelyingPartyId(sp);
        return rpc.getSubcontext(AttributeFilterContext.class, true);
    }
    

    @Test public void rp() throws Exception {
        rp(true);
        rp(false);
    }
    
    public void rp(boolean isAfp) throws Exception {
        GenericApplicationContext ctx = new GenericApplicationContext();
        setTestContext(ctx);
        SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader = new SchemaTypeAwareXMLBeanDefinitionReader(ctx);

        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource(BaseAttributeFilterParserTest.POLICY_RULE_PATH
                + "predicateBeans.xml"), new ClassPathResource(BaseAttributeFilterParserTest.POLICY_RULE_PATH + (isAfp?"afp/":"")
                + "predicateRp.xml"));

        ctx.refresh();

        final PredicatePolicyRule rule = ctx.getBean(PredicatePolicyRule.class);
        Assert.assertEquals(rule.matches(prcFor("https://example.org")), Tristate.FALSE);
        Assert.assertEquals(rule.matches(prcFor("https://sp.example.org")), Tristate.TRUE);
        Assert.assertEquals(rule.matches(prcFor("https://sp2.example.org")), Tristate.TRUE);

    }

    static class Foo implements Predicate<AttributeFilterContext> {

        /** {@inheritDoc} */
        @Override public boolean apply(@Nullable AttributeFilterContext input) {
            return false;
        }

    }

    static class Func implements Function<AttributeFilterContext, ProfileRequestContext> {

        /** {@inheritDoc} */
        @Override public ProfileRequestContext apply(@Nullable AttributeFilterContext input) {
            return new ProfileRequestContext<>();
        }

    }

}
