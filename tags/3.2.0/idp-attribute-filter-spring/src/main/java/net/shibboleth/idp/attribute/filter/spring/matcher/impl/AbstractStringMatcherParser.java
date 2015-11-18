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

package net.shibboleth.idp.attribute.filter.spring.matcher.impl;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.filter.spring.matcher.BaseAttributeValueMatcherParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Base class for string matching functors of natural type Matcher (mostly attribute value matchers).
 */
public abstract class AbstractStringMatcherParser extends BaseAttributeValueMatcherParser {

    /** {@inheritDoc} */
    @Override protected void doNativeParse(@Nonnull final Element element, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(element, builder);

        builder.addPropertyValue("matchString", StringSupport.trimOrNull(element.getAttributeNS(null, "value")));

        if (element.hasAttributeNS(null, "ignoreCase")) {
            builder.addPropertyValue("ignoreCase", 
                    StringSupport.trimOrNull(element.getAttributeNS(null, "ignoreCase")));
        }
    }
}