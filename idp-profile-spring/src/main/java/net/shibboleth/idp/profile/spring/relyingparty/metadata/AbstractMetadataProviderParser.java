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

package net.shibboleth.idp.profile.spring.relyingparty.metadata;

import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.saml.metadata.RelyingPartyMetadataProvider;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parser for the MetadataProviderType in the <code>urn:mace:shibboleth:2.0:metadata</code> namespace.
 * 
 * This also handles the ambivalence of where the &lt;MetadataProvider&gt; can be found. If it is found inside a
 * &lt;RelyingPartyGroup&gt; or a &lt;ChainingMetadataPRovider&gt; then we just emit a MetadataResolver of the correct
 * type and the outer parsers will deal with the rest. If we are the top most element then we need to summon up a
 * {@link RelyingPartyMetadataProvider} and inject what we would usually create into that.
 */
public abstract class AbstractMetadataProviderParser extends AbstractSingleBeanDefinitionParser {

    /** Namespace for Security. */
    public static final String SECURITY_NAMESPACE = "urn:mace:shibboleth:2.0:security";
    /** Namespace for RelyingParty. */
    public static final String RP_NAMESPACE = "urn:mace:shibboleth:2.0:relying-party";
    /** Namespace for Metadata. */
    public static final String METADATA_NAMESPACE = "urn:mace:shibboleth:2.0:metadata";
    
    /** MetadataFilter Element name. */
    public static final QName METADATA_FILTER_ELEMENT_NAME = new QName(METADATA_NAMESPACE, "MetadataFilter");
    /** ChainingMetadataProviderElement name. */
    public static final QName CHAINING_PROVIDER_ELEMENT_NAME = 
            new QName(METADATA_NAMESPACE, "ChainingMetadataProvider");
    /** RelyingPartyGroup Element name. */
    public static final QName RELYING_PARTY_GROUP_ELEMENT_NAME = new QName(RP_NAMESPACE, "RelyingPartyGroup");
    /** TrustEngine element name. */
    public static final QName TRUST_ENGINE_ELEMENT_NAME = new QName(SECURITY_NAMESPACE, "TrustEngine");

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractMetadataProviderParser.class);

    /**
     * Handle attributes which are inappropriate for specific implementations. The chaining metadata provider cannot
     * have "requireValidMetadata" or "failFastInitialization" set, even though they are present in the schema. <br/>
     * This method detects whether these elements are present and if the element is not a chaining provider returns
     * true, otherwise it returns false and emits a warning.
     * 
     * @param element the element
     * @param attribute the attribute
     * @return true iff this is not a chaining resolver and the attribute is present
     */
    private boolean isPresentNotChaining(@Nonnull Element element, @Nonnull String attribute) {

        if (!element.hasAttributeNS(null, attribute)) {
            return false;
        }

        if (CHAINING_PROVIDER_ELEMENT_NAME.equals(DOMTypeSupport.getXSIType(element))) {
            log.warn("{} is not valid for {}", attribute, CHAINING_PROVIDER_ELEMENT_NAME.getLocalPart());
            return false;
        }
        return true;
    }

    /**
     * Is this the element at the top of the file? Yes, if it has no parent or if the parent is a RelyingPartyGroup. In
     * this situation we need to wrap the element in a {@link RelyingPartyMetadataProvider}.
     * 
     * @param element the element.
     * @return whether it is the outmost element.
     */
    private boolean isTopMost(@Nonnull Element element) {
        final Node parent = element.getParentNode();

        if (parent.getNodeType() == Node.DOCUMENT_NODE) {
            return true;
        }

        if (parent.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        }
        return RELYING_PARTY_GROUP_ELEMENT_NAME.getLocalPart().equals(parent.getLocalName())
                && RELYING_PARTY_GROUP_ELEMENT_NAME.getNamespaceURI().equals(parent.getNamespaceURI());
    }

    /**
     * Return the real class implement by this type. This has the same function as the more usual
     * {@link AbstractSingleBeanDefinitionParser#getBeanClass(Element)} but it may need to be shimmed in
     * {@link AbstractMetadataProviderParser} which may need to insert an extra bean.
     * 
     * @param element the {@code Element} that is being parsed
     * @return the {@link Class} of the bean that is being defined via parsing the supplied {@code Element}, or
     *         {@code null} if none
     * @see #getBeanClassName
     */
    protected abstract Class<? extends MetadataResolver> getNativeBeanClass(Element element);

    /** {@inheritDoc} */
    @Override protected final Class<? extends MetadataResolver> getBeanClass(Element element) {
        if (isTopMost(element)) {
            return RelyingPartyMetadataProvider.class;
        }
        return getNativeBeanClass(element);
    }

    /** {@inheritDoc} */
    @Override protected final void doParse(Element element, ParserContext parserContext, 
            BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        if (isTopMost(element)) {
            builder.setLazyInit(true);
            BeanDefinitionBuilder childBeanDefinitionBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(getNativeBeanClass(element));
            doNativeParse(element, parserContext, childBeanDefinitionBuilder);

            builder.addConstructorArgValue(childBeanDefinitionBuilder.getBeanDefinition());

            if (element.hasAttributeNS(null, "sortKey")) {
                builder.addPropertyValue("sortKey", StringSupport.trimOrNull(element.getAttributeNS(null, "sortKey")));
            }
        } else {
            if (element.hasAttributeNS(null, "sortKey")) {
                log.warn("{} sortKey is only valid on 'top level' MetadataProviders", parserContext.getReaderContext()
                        .getResource().getDescription());
            }
            doNativeParse(element, parserContext, builder);
        }
    }

    /**
     * Parse the element into the provider builder. This has the same function as the more usual
     * {@link AbstractSingleBeanDefinitionParser#doParse(Element, ParserContext, BeanDefinitionBuilder)} but it may need
     * to be shimmed in this class which may need to insert an extra bean.
     * 
     * @param element the XML element being parsed
     * @param parserContext the object encapsulating the current state of the parsing process
     * @param builder used to define the {@code BeanDefinition}
     * @see #doParse(Element, BeanDefinitionBuilder)
     */
    protected void doNativeParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        builder.setInitMethodName("initialize");
        builder.setDestroyMethodName("destroy");
        builder.setLazyInit(true);

        builder.addPropertyValue("id", StringSupport.trimOrNull(element.getAttributeNS(null, "id")));

        if (isPresentNotChaining(element, "failFastInitialization")) {
            builder.addPropertyValue("failFastInitialization",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "failFastInitialization")));
        }

        if (isPresentNotChaining(element, "requireValidMetadata")) {
            builder.addPropertyValue("requireValidMetadata",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "requireValidMetadata")));
        }

        final List<Element> filters =
                ElementSupport.getChildElements(element, METADATA_FILTER_ELEMENT_NAME);

        if (null != filters) {
            if (filters.size() == 1) {
                // Install directly.
                builder.addPropertyValue("metadataFilter", SpringSupport.parseCustomElements(filters, parserContext));
            } else if (filters.size() > 1) {
                // Wrap in a chaining filter.
                final BeanDefinitionBuilder chainBuilder =
                        BeanDefinitionBuilder.genericBeanDefinition(MetadataFilterChain.class);
                chainBuilder.addPropertyValue("filters", SpringSupport.parseCustomElements(filters, parserContext));
                builder.addPropertyValue("metadataFilter", chainBuilder.getBeanDefinition());
            }
        }

        final List<Element> trustEngines =
                ElementSupport.getChildElements(element, TRUST_ENGINE_ELEMENT_NAME);
        if (trustEngines != null && !trustEngines.isEmpty()) {
            log.warn("{} Deprecated placement of <TrustEngine> inside <MetadataProvider>. "
                    + "Place inside the relevant filter", parserContext.getReaderContext().getResource()
                    .getDescription());
            SpringSupport.parseCustomElements(trustEngines, parserContext);
        }
    }
}
