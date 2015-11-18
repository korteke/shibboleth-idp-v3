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

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.idp.relyingparty.RelyingPartyConfiguration;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for the common parts of &lt;AnonymousRelyingParty&gt; &lt;DefaultRelyingParty&gt; and &lt;RelyingParty&gt;.
 * Implementations only differ by being named or not (for reference elsewhere) and by the
 * {@link com.google.common.base.Predicate} which is injected.
 */
public abstract class AbstractRelyingPartyParser extends AbstractSingleBeanDefinitionParser {

    /** Element name. */
    public static final QName PROFILE_CONFIGURATION = new QName(AbstractMetadataProviderParser.RP_NAMESPACE,
            "ProfileConfiguration");

    /** log. */
    private Logger log = LoggerFactory.getLogger(AbstractRelyingPartyParser.class);

    /** {@inheritDoc} */
    @Override protected java.lang.Class<RelyingPartyConfiguration> getBeanClass(Element element) {
        return RelyingPartyConfiguration.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        builder.setLazyInit(true);
        super.doParse(element, parserContext, builder);

        // defaultSigningCredentialRef, defaultAuthenticationMethod and nameIDFormatPrecedence are dealt with
        // in the specific SSO profileConfigurations.
        // IDP-563: defaultAuthenticationMethod had weird semantics in V2. Warn.
        if (element.hasAttributeNS(null, "defaultAuthenticationMethod")) {
            log.warn("Specific authentication methods may not work for all profiles.  defaultAuthenticationMethod='{}'",
                    element.getAttributeNS(null, "defaultAuthenticationMethod"));
        }

        final String provider = StringSupport.trimOrNull(element.getAttributeNS(null, "provider"));
        builder.addPropertyValue("responderId", provider);

        final String detailedErrors = StringSupport.trimOrNull(element.getAttributeNS(null, "detailedErrors"));
        if (null != detailedErrors) {
            builder.addPropertyValue("detailedErrors", detailedErrors);
        }

        final List<BeanDefinition> profileConfigurations =
                SpringSupport.parseCustomElements(ElementSupport.getChildElements(element, PROFILE_CONFIGURATION),
                        parserContext);
        builder.addPropertyValue("profileConfigurations", profileConfigurations);

        builder.setInitMethodName("initialize");
        builder.setDestroyMethodName("destroy");
    }
}
