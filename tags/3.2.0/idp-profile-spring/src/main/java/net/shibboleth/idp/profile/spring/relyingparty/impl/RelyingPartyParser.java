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

import java.util.List;

import javax.xml.namespace.QName;

import net.shibboleth.idp.profile.logic.RelyingPartyIdPredicate;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.idp.saml.profile.context.navigate.SAMLMetadataContextLookupFunction;
import net.shibboleth.utilities.java.support.logic.StrategyIndirectedPredicate;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.saml.common.messaging.context.navigate.EntityDescriptorLookupFunction;
import org.opensaml.saml.common.profile.logic.EntityGroupNamePredicate;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.google.common.base.Functions;
import com.google.common.base.Predicates;

/**
 * Parser for the &lt:rp:RelyingParty&gt; element.
 */
public class RelyingPartyParser extends AbstractRelyingPartyParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(AbstractMetadataProviderParser.RP_NAMESPACE, "RelyingParty");

    /**
     * {@inheritDoc} The construction of the activation Condition is more complicated than one might suppose. The
     * definition is that if the it matches the relyingPartyID *or* it matches the &lt;EntitiesDescriptor&gt;, then the
     * configuration matches. So we need to
     * {@link Predicates#or(com.google.common.base.Predicate, com.google.common.base.Predicate)} a
     * {@link RelyingPartyIdPredicate} and an {@link EntityGroupNamePredicate} These however may have injected lookup
     * strategies and so these need to be constructed as a BeanDefinition.
     * */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        final String id = StringSupport.trimOrNull(element.getAttributeNS(null, "id"));
        builder.addPropertyValue("id", id);

        List<String> ids = new ManagedList<>(1);
        ids.add(id);

        // This is a simple predicate acting directly on the RelyingPartyContext.
        final BeanDefinitionBuilder rpPredicate =
                BeanDefinitionBuilder.genericBeanDefinition(RelyingPartyIdPredicate.class);
        rpPredicate.addConstructorArgValue(ids);

        // This is the complex predicate to apply to the EntityDescriptor buried inside the context.
        final BeanDefinitionBuilder egPredicate =
                BeanDefinitionBuilder.genericBeanDefinition(EntityGroupNamePredicate.class);
        egPredicate.addConstructorArgValue(ids);
        
        // This is a lookup function composition to get from the PRC to the SAMLMetadataContext.
        final BeanDefinitionBuilder lookupFunction =
                BeanDefinitionBuilder.rootBeanDefinition(Functions.class, "compose");
        lookupFunction.addConstructorArgValue(BeanDefinitionBuilder.genericBeanDefinition(
                EntityDescriptorLookupFunction.class).getBeanDefinition());
        lookupFunction.addConstructorArgValue(BeanDefinitionBuilder.genericBeanDefinition(
                SAMLMetadataContextLookupFunction.class).getBeanDefinition());
        
        // And this indirects the predicate to apply to the result of the lookup.
        final BeanDefinitionBuilder indirectPredicate =
                BeanDefinitionBuilder.genericBeanDefinition(StrategyIndirectedPredicate.class);
        indirectPredicate.addConstructorArgValue(lookupFunction.getBeanDefinition());
        indirectPredicate.addConstructorArgValue(egPredicate.getBeanDefinition());

        BeanDefinitionBuilder orPredicate = BeanDefinitionBuilder.genericBeanDefinition(Predicates.class);
        orPredicate.setFactoryMethod("or");
        orPredicate.addConstructorArgValue(rpPredicate.getBeanDefinition());
        orPredicate.addConstructorArgValue(indirectPredicate.getBeanDefinition());

        builder.addPropertyValue("activationCondition", orPredicate.getBeanDefinition());
    }
    
}