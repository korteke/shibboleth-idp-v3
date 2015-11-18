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

package net.shibboleth.idp.attribute.filter.complex.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.filter.AttributeFilter;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.attribute.resolver.dc.impl.SAMLAttributeDataConnector;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.saml2.core.Attribute;

import com.google.common.base.Function;

/**
 * Base class for testing complex attribute filter operations.
 */
public class BaseComplexAttributeFilterTestCase extends XMLObjectBaseTestCase {

    private static final String PATH = "/data/net/shibboleth/idp/filter/impl/complex/";

    /**
     * Helper function to return attributes pulled from a file (on the classpath). The file is expected to contain a
     * single <mdattr:EntityAttributes/> statement.
     * 
     * @param xmlFileName the file within the test directory.
     * @return the att
     * @throws ComponentInitializationException
     * @throws ResolutionException
     */
    protected Map<String, IdPAttribute> getIdPAttributes(String xmlFileName) throws ComponentInitializationException,
            ResolutionException {

        final EntityAttributes obj = (EntityAttributes) unmarshallElement(PATH + xmlFileName);

        SAMLAttributeDataConnector connector = new SAMLAttributeDataConnector();
        connector.setId(xmlFileName);
        connector.setAttributesStrategy(new Function<AttributeResolutionContext, List<Attribute>>() {
            @Override
            @Nullable public List<Attribute> apply(@Nullable AttributeResolutionContext input) {
                return obj.getAttributes();
            }
        });

        connector.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        context.getSubcontext(AttributeResolverWorkContext.class, true);
        return connector.resolve(context);
    }

    protected AttributeFilter getPolicy(String xmlFileName) {
        return null;
    }

}