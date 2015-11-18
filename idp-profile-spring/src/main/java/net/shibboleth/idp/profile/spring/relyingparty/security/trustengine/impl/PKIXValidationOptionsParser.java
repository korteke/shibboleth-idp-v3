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

import javax.xml.namespace.QName;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.security.x509.PKIXValidationOptions;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * A Parser for the &lt; ValidationOptions &gt; within a StaticPKIXSignature.
 */
public class PKIXValidationOptionsParser extends AbstractSingleBeanDefinitionParser {

    /** Validation Options. */
    public static final QName ELEMENT_NAME = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "ValidationOptions");

    /** {@inheritDoc} */
    @Override protected Class<?> getBeanClass(Element element) {
        return PKIXValidationOptions.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        if (element.hasAttributeNS(null, "processEmptyCRLs")) {
            builder.addPropertyValue("processEmptyCRLs",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "processEmptyCRLs")));
        }

        if (element.hasAttributeNS(null, "processExpiredCRLs")) {
            builder.addPropertyValue("processExpiredCRLs",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "processExpiredCRLs")));
        }

        if (element.hasAttributeNS(null, "processCredentialCRLs")) {
            builder.addPropertyValue("processCredentialCRLs",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "processCredentialCRLs")));
        }

        if (element.hasAttributeNS(null, "defaultVerificationDepth")) {
            builder.addPropertyValue("defaultVerificationDepth",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "defaultVerificationDepth")));
        }
    }

    /** {@inheritDoc} */
    @Override protected boolean shouldGenerateId() {
        return true;
    }

}
