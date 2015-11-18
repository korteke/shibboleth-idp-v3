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

package net.shibboleth.idp.attribute.filter.spring.policyrule;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.filter.MatcherFromPolicy;
import net.shibboleth.idp.attribute.filter.spring.impl.AbstractWarningFilterParser;
import net.shibboleth.idp.attribute.filter.spring.matcher.BaseAttributeValueMatcherParser;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Base function for all natural policy rules. <br/>
 * This function takes care of the bean nesting needed to convert the bean (which is a natural policy rule) into the
 * correct type. Specifically:<br/>
 * <table>
 * <tr>
 * <td>PolicyRule</td>
 * <td>Native</td>
 * </tr>
 * <tr>
 * <td>Matcher</td>
 * <td>Nest inside a {@link MatcherFromPolicy}</td>
 * </tr>
 * </table>
 */
public abstract class BasePolicyRuleParser extends AbstractWarningFilterParser {

    /**
     * Helper function to determine if the Attribute Matcher has the attribute Id Specified.
     * 
     * @param configElement the config element to inspect
     * @return whether here is a an attribute Id
     */
    protected boolean hasAttributeId(@Nonnull final Element configElement) {
        return configElement.hasAttributeNS(null, BaseAttributeValueMatcherParser.ATTRIBUTE_ID);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull protected Class<?> getBeanClass(@Nonnull final Element element) {
        if (isPolicyRule(element)) {
            return getNativeBeanClass();
        } else {
            return MatcherFromPolicy.class;
        }
    }

    /**
     * Parse bean definition. If needs be inject it into a parent bean. {@inheritDoc}
     */
    @Override
    protected void doParse(@Nonnull final Element element, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        final String myId = builder.getBeanDefinition().getAttribute("qualifiedId").toString();

        builder.addPropertyValue("id", myId);

        if (isPolicyRule(element)) {
            doNativeParse(element, parserContext, builder);
        } else {

            BeanDefinitionBuilder childBuilder = BeanDefinitionBuilder.genericBeanDefinition(getNativeBeanClass());

            childBuilder.setInitMethodName("initialize");
            childBuilder.setDestroyMethodName("destroy");

            doNativeParse(element, parserContext, childBuilder);
            childBuilder.addPropertyValue("id", "PMId:" + myId);

            builder.addConstructorArgValue(childBuilder.getBeanDefinition());
        }
    }

    /**
     * Method return the native Matcher implementation.
     * 
     * @return the class.
     */
    @Nonnull protected abstract Class<?> getNativeBeanClass();

    /**
     * Parser the native bean class. This is either called direct or then injected into the nesting class.
     * 
     * @param element the config
     * @param parserContext the context
     * @param builder the builder
     */
    protected abstract void doNativeParse(@Nonnull Element element, @Nonnull ParserContext parserContext,
            @Nonnull BeanDefinitionBuilder builder);
}