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

package net.shibboleth.idp.profile.spring.relyingparty.saml.impl;

import javax.xml.namespace.QName;

import net.shibboleth.idp.saml.saml2.profile.config.SingleLogoutProfileConfiguration;

import org.w3c.dom.Element;

/**
 * Parser to generate {@link SingleLogoutProfileConfiguration} from a
 * <code>saml:SAML2LogoutRequestProfile</code>.
 */
public class SAML2LogoutRequestProfileParser extends BaseSAML2ProfileConfigurationParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(RelyingPartySAMLNamespaceHandler.NAMESPACE,
            "SAML2LogoutRequestProfile");

    /** Constructor. */
    public SAML2LogoutRequestProfileParser() {
        setArtifactAware(true);
    }

    /** {@inheritDoc} */
    @Override protected Class<SingleLogoutProfileConfiguration> getBeanClass(Element element) {
        return SingleLogoutProfileConfiguration.class;
    }

    /** {@inheritDoc} */
    @Override protected String getProfileBeanNamePrefix() {
        return "shibboleth.SAML2.Logout.";
    }
    
}