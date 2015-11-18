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

package net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl;

import java.util.List;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.impl.SecurityNamespaceHandler;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Base Parser for trust engines of type SignatureChaining and Chaining.
 */
public abstract class AbstractChainingParser extends AbstractTrustEngineParser {
    
    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        final List<Element> childEngines =
                ElementSupport.getChildElements(element, AbstractMetadataProviderParser.TRUST_ENGINE_ELEMENT_NAME);
        final List<Element> childEngineRefs =
                ElementSupport.getChildElements(element, SecurityNamespaceHandler.TRUST_ENGINE_REF);
        
        final List<BeanMetadataElement> allChildren = new ManagedList<>(childEngines.size()+ childEngineRefs.size());
        
        allChildren.addAll(SpringSupport.parseCustomElements(childEngines, parserContext));
        
        for (Element ref:childEngineRefs) {
            final String reference = StringSupport.trimOrNull(ref.getAttributeNS(null, "ref"));
            if (null != reference) {
                allChildren.add(new RuntimeBeanReference(StringSupport.trim(reference)));
            }
        }
        builder.addConstructorArgValue(allChildren);
    }
}
