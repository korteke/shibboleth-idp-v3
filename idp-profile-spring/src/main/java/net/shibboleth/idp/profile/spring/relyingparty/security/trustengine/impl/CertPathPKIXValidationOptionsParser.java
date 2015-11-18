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

import javax.xml.namespace.QName;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.security.x509.impl.CertPathPKIXValidationOptions;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Spring bean definition parser for {urn:mace:shibboleth:2.0:security}ValidationOptions elements which have a type
 * specialization of {urn:mace:shibboleth:2.0:security}CertPathValidationOptionsType.
 */
public class CertPathPKIXValidationOptionsParser extends PKIXValidationOptionsParser {
    /** Schema type. */
    public static final QName SCHEMA_TYPE = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "CertPathValidationOptionsType");

    /** PolicyOid Element type. */
    public static final QName POLICY_OID_ELEMENT = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "PolicyOID");

    /** {@inheritDoc} */
    @Override protected Class getBeanClass(final Element element) {
        return CertPathPKIXValidationOptions.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(final Element element, final ParserContext parserContext,
            final BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        if (element.hasAttributeNS(null, "forceRevocationEnabled")) {
            builder.addPropertyValue("forceRevocationEnabled",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "forceRevocationEnabled")));
        }

        if (element.hasAttributeNS(null, "revocationEnabled")) {
            builder.addPropertyValue("revocationEnabled",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "revocationEnabled")));
        }

        if (element.hasAttributeNS(null, "policyMappingInhibit")) {
            builder.addPropertyValue("policyMappingInhibit",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "policyMappingInhibit")));
        }

        if (element.hasAttributeNS(null, "anyPolicyInhibit")) {
            builder.addPropertyValue("anyPolicyInhibit",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "anyPolicyInhibit")));
        }

        final List<Element> childElems = ElementSupport.getChildElements(element, POLICY_OID_ELEMENT);
        if (null != childElems && !childElems.isEmpty()) {
            final List<String> initialPolicies = new ManagedList<>(childElems.size());
            for (Element nameElem : childElems) {
                final String value = StringSupport.trimOrNull(nameElem.getTextContent());
                if (null != value) {
                    initialPolicies.add(value);
                }
            }
            builder.addPropertyValue("initialPolicies", initialPolicies);
        }
    }
}
