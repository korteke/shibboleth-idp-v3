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

package net.shibboleth.idp.saml.attribute.mapping.impl;

import java.util.List;

import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;

/**
 * Base class for testing attribute mapping
 */
public class MappingTests  extends XMLObjectBaseTestCase  {

    public static final String FILE_PATH = "/net/shibboleth/idp/saml/impl/attribute/mapping/";

    protected  static final String THE_FORMAT = "urn:oasis:names:tc:SAML:2.0:attrname-format:uri";

    protected  static final String SAML_NAME_ONE = "stdFormat";
    
    protected  static final String SAML_NAME_TWO = "oddFormat";
    
    protected  static final String SAML_NAME_THREE = "urn:oid:2.16.840.1.113730.3.1.241";
    
    protected List<RequestedAttribute> loadFile(final String file)
    {
        AttributeConsumingService service = unmarshallElement(FILE_PATH + file);
        
        return service.getRequestAttributes();
    }

    
}