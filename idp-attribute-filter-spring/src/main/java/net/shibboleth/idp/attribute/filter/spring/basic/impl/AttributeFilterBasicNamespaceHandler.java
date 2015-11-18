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

package net.shibboleth.idp.attribute.filter.spring.basic.impl;

import net.shibboleth.ext.spring.util.BaseSpringNamespaceHandler;
import net.shibboleth.idp.attribute.filter.spring.matcher.impl.AttributeScopeMatcherParser;
import net.shibboleth.idp.attribute.filter.spring.matcher.impl.AttributeScopeRegexMatcherParser;
import net.shibboleth.idp.attribute.filter.spring.matcher.impl.AttributeValueRegexMatcherParser;
import net.shibboleth.idp.attribute.filter.spring.matcher.impl.AttributeValueStringMatcherParser;
import net.shibboleth.idp.attribute.filter.spring.policyrule.impl.AttributeIssuerRegexRuleParser;
import net.shibboleth.idp.attribute.filter.spring.policyrule.impl.AttributeIssuerRuleParser;
import net.shibboleth.idp.attribute.filter.spring.policyrule.impl.AttributeRequesterRegexRuleParser;
import net.shibboleth.idp.attribute.filter.spring.policyrule.impl.AttributeRequesterRuleParser;
import net.shibboleth.idp.attribute.filter.spring.policyrule.impl.AuthenticationMethodRegexRuleParser;
import net.shibboleth.idp.attribute.filter.spring.policyrule.impl.AuthenticationMethodRuleParser;
import net.shibboleth.idp.attribute.filter.spring.policyrule.impl.NumOfAttributeValuesRuleParser;
import net.shibboleth.idp.attribute.filter.spring.policyrule.impl.PredicateRuleParser;
import net.shibboleth.idp.attribute.filter.spring.policyrule.impl.PrincipalNameRegexRuleParser;
import net.shibboleth.idp.attribute.filter.spring.policyrule.impl.PrincipalNameRuleParser;

/** Namespace handler for the attribute filter engine basic match functions. */
public class AttributeFilterBasicNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Basic match function namespace. */
    public static final String NAMESPACE = "urn:mace:shibboleth:2.0:afp:mf:basic";

    /** {@inheritDoc} */
    @Override
    public void init() {

        registerBeanDefinitionParser(AnyParser.SCHEMA_TYPE, new AnyParser());

        // LOGIC

        registerBeanDefinitionParser(AndMatcherParser.SCHEMA_TYPE, new AndMatcherParser());

        registerBeanDefinitionParser(OrMatcherParser.SCHEMA_TYPE, new OrMatcherParser());

        registerBeanDefinitionParser(NotMatcherParser.SCHEMA_TYPE, new NotMatcherParser());

        // Attribute/Matcher
        registerBeanDefinitionParser(AttributeValueStringMatcherParser.SCHEMA_TYPE,
                new AttributeValueStringMatcherParser());

        registerBeanDefinitionParser(AttributeScopeMatcherParser.SCHEMA_TYPE, new AttributeScopeMatcherParser());

        registerBeanDefinitionParser(AttributeValueRegexMatcherParser.SCHEMA_TYPE,
                new AttributeValueRegexMatcherParser());

        registerBeanDefinitionParser(AttributeScopeRegexMatcherParser.SCHEMA_TYPE,
                new AttributeScopeRegexMatcherParser());

        // Policy
        registerBeanDefinitionParser(AttributeRequesterRuleParser.SCHEMA_TYPE, new AttributeRequesterRuleParser());

        registerBeanDefinitionParser(AttributeRequesterRegexRuleParser.SCHEMA_TYPE,
                new AttributeRequesterRegexRuleParser());

        registerBeanDefinitionParser(AttributeIssuerRuleParser.SCHEMA_TYPE, new AttributeIssuerRuleParser());

        registerBeanDefinitionParser(AttributeIssuerRegexRuleParser.SCHEMA_TYPE, new AttributeIssuerRegexRuleParser());

        registerBeanDefinitionParser(PrincipalNameRuleParser.SCHEMA_TYPE, new PrincipalNameRuleParser());

        registerBeanDefinitionParser(PrincipalNameRegexRuleParser.SCHEMA_TYPE, new PrincipalNameRegexRuleParser());

        registerBeanDefinitionParser(AuthenticationMethodRuleParser.SCHEMA_TYPE, new AuthenticationMethodRuleParser());

        registerBeanDefinitionParser(AuthenticationMethodRegexRuleParser.SCHEMA_TYPE,
                new AuthenticationMethodRegexRuleParser());

        registerBeanDefinitionParser(NumOfAttributeValuesRuleParser.SCHEMA_TYPE, new NumOfAttributeValuesRuleParser());

        registerBeanDefinitionParser(ScriptedMatcherParser.SCHEMA_TYPE, new ScriptedMatcherParser());
        
        registerBeanDefinitionParser(PredicateRuleParser.SCHEMA_TYPE, new PredicateRuleParser());
    }
}