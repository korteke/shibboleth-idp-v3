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

package net.shibboleth.idp.attribute.filter.spring.saml.impl;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.attribute.filter.policyrule.saml.impl.RegistrationAuthorityPolicyRule;
import net.shibboleth.idp.attribute.filter.spring.BaseFilterParser;
import net.shibboleth.idp.attribute.filter.spring.policyrule.BasePolicyRuleParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/** Spring bean definition parser that creates {@link RegistrationAuthorityPolicyRule} beans. */
public class RegistrationAuthorityRuleParser extends BasePolicyRuleParser {

    /** Schema type - saml. */
    public static final QName SCHEMA_TYPE = new QName(AttributeFilterSAMLNamespaceHandler.NAMESPACE,
            "RegistrationAuthority");
    
    /** Schema type - afp. */
    public static final QName SCHEMA_TYPE_AFP = new QName(BaseFilterParser.NAMESPACE,
            "RegistrationAuthority");

    /** Name of the attribute carrying the Issuers list. */
    public static final String REGISTRARS_ATTR_NAME = "registrars";

    /** Name of the attribute carrying the boolean to flag behaviour if the metadata MDRPI. */
    public static final String MATCH_IF_METADATA_SILENT_ATTR_NAME = "matchIfMetadataSilent";

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(RegistrationAuthorityRuleParser.class);

    /** {@inheritDoc} */
    @Override protected Class<RegistrationAuthorityPolicyRule> getNativeBeanClass() {
        return RegistrationAuthorityPolicyRule.class;
    }

    /** {@inheritDoc} */
    @Override protected QName getAFPName() {
        return SCHEMA_TYPE_AFP;
    }

    /** {@inheritDoc} */
    @Override protected void doNativeParse(@Nonnull final Element element, @Nonnull final ParserContext parserContext,
            @Nonnull BeanDefinitionBuilder builder) {

        if (element.hasAttributeNS(null, MATCH_IF_METADATA_SILENT_ATTR_NAME)) {
            String matchIfSilent =
                    StringSupport.trimOrNull(element.getAttributeNS(null, MATCH_IF_METADATA_SILENT_ATTR_NAME));
            log.debug("Registration Authority Filter: Match if Metadata silent = {}", matchIfSilent);
            builder.addPropertyValue("matchIfMetadataSilent", matchIfSilent);
        }

        final Attr attr = element.getAttributeNodeNS(null, REGISTRARS_ATTR_NAME);
        if (attr != null) {
            final ManagedList<String> issuers = SpringSupport.getAttributeValueAsManagedList(attr);
            log.debug("Registration Authority Filter: Issuers = {}", issuers);
            builder.addPropertyValue("issuers", issuers);
        }
    }
}
