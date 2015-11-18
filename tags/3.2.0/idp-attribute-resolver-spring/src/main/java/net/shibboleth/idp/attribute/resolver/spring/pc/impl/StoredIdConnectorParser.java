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

package net.shibboleth.idp.attribute.resolver.spring.pc.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.saml.attribute.resolver.impl.StoredIDDataConnector;
import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.idp.saml.nameid.NameIdentifierDecoder;
import net.shibboleth.idp.saml.nameid.impl.StoredPersistentIdDecoder;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.saml.saml1.core.NameIdentifier;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for StoredId Principal Connector<br/>
 * &lt;PrincipalConnector xsi:type="pc:StoredId"&gt;.
 */
public class StoredIdConnectorParser extends AbstractPrincipalConnectorParser {

    /** Schema type name. */
    @Nonnull public static final QName TYPE_NAME = new QName(PrincipalConnectorNamespaceHandler.NAMESPACE, "StoredId");

    /** {@inheritDoc} */
    @Override protected void addSAMLDecoders(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {

        BeanDefinitionBuilder subBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(EmbeddedStoredPersistentIdDecoder.class);

        final String dataConnector = StringSupport.trimOrNull(config.getAttributeNS(null, "storedIdDataConnectorRef"));
        subBuilder.addConstructorArgReference(dataConnector);

        subBuilder.setInitMethodName("initialize");
        subBuilder.setDestroyMethodName("destroy");

        final String id = StringSupport.trimOrNull(config.getAttributeNS(null, "id"));
        subBuilder.addPropertyValue("id", id);

        builder.addConstructorArgValue(subBuilder.getBeanDefinition());

        subBuilder = BeanDefinitionBuilder.genericBeanDefinition(NotImplementedNameIdentifierDecoder.class);
        builder.addConstructorArgValue(subBuilder.getBeanDefinition());
    }

    /** Helper class to allow us to inject in a StoredIdStore into StoredIdStore. */
    public static class EmbeddedStoredPersistentIdDecoder extends StoredPersistentIdDecoder {

        /**
         * Constructor.
         * 
         * @param connector the data connector of interest
         */
        public EmbeddedStoredPersistentIdDecoder(StoredIDDataConnector connector) {
            setPersistentIdStore(connector.getStoredIDStore());
        }
    }

    /** Helper class to allow us to put *something* into a the principalConnector. */
    public static class NotImplementedNameIdentifierDecoder implements NameIdentifierDecoder {

        /** {@inheritDoc} */
        @Override @Nullable public String decode(@Nonnull SubjectCanonicalizationContext c14nContext,
                @Nonnull NameIdentifier nameID) throws NameDecoderException {
            throw new NameDecoderException("Name Decoding for SAML1?");
        }

    }

}