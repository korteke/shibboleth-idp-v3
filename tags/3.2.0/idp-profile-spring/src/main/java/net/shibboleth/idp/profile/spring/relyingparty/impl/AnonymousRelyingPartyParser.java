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

package net.shibboleth.idp.profile.spring.relyingparty.impl;

import javax.xml.namespace.QName;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for the &lt:rp:AnonymousRelyingParty&gt; element. <br/>
 * The {@link com.google.common.base.Predicate} which is injected looks to see if there is metadata associated with the
 * RelyingParty and if so says TRUE.
 */
public class AnonymousRelyingPartyParser extends AbstractRelyingPartyParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(AbstractMetadataProviderParser.RP_NAMESPACE,
            "AnonymousRelyingParty");

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        builder.addPropertyValue("id", "AnonymousRelyingParty");
        // We don't set an activationCondition property because the anonymous/unverified case is a special one.
    }

    /** {@inheritDoc} */
    @Override protected boolean shouldGenerateId() {
        return true;
    }
}
