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

package net.shibboleth.idp.attribute.filter.spring.impl;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.attribute.filter.spring.BaseFilterParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for &lt;afp:AttributeFilterPolicyGroup&gt;, top top level of the filter "stack". <br.>
 * 
 * There is no bean being summoned up here. Rather we just parse all the children. Then over in the service all the *
 * {@link net.shibboleth.idp.attribute.filter.AttributeFilterPolicy} beans are sucked out of spring by type and injected
 * into a new {@link net.shibboleth.idp.attribute.filter.impl.AttributeFilterImpl} via a
 * {@link AttributeFilterServiceStrategy}.
 */
public class AttributeFilterPolicyGroupParser implements BeanDefinitionParser {
    /** Schema type name. */
    public static final QName TYPE_NAME = new QName(BaseFilterParser.NAMESPACE,
            "AttributeFilterPolicyGroupType");

    /** Local name of the policy requirement element. */
    public static final QName POLICY_REQUIREMENT_ELEMENT_NAME = new QName(BaseFilterParser.NAMESPACE,
            "PolicyRequirement");

    /** Local name of the value filter element. */
    public static final QName PERMIT_VALUE_ELEMENT_NAME = new QName(BaseFilterParser.NAMESPACE,
            "PermitValue");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AttributeFilterPolicyGroupParser.class);

    /** {@inheritDoc} */
    @Override public BeanDefinition parse(Element config, ParserContext context) {

        String policyId = StringSupport.trimOrNull(config.getAttributeNS(null, "id"));

        log.debug("Parsing attribute filter policy group {}", policyId);

        List<Element> children;
        Map<QName, List<Element>> childrenMap = ElementSupport.getIndexedChildElements(config);

        //
        // Top level definitions
        //

        children = childrenMap.get(new QName(BaseFilterParser.NAMESPACE, "PolicyRequirementRule"));
        SpringSupport.parseCustomElements(children, context);

        children = childrenMap.get(new QName(BaseFilterParser.NAMESPACE, "AttributeRule"));
        SpringSupport.parseCustomElements(children, context);

        children = childrenMap.get(new QName(BaseFilterParser.NAMESPACE, "PermitValueRule"));
        SpringSupport.parseCustomElements(children, context);

        children = childrenMap.get(new QName(BaseFilterParser.NAMESPACE, "DenyValueRule"));
        SpringSupport.parseCustomElements(children, context);

        //
        // The actual policies
        //
        children = childrenMap.get(new QName(BaseFilterParser.NAMESPACE, "AttributeFilterPolicy"));

        SpringSupport.parseCustomElements(children, context);
        return null;
    }
}