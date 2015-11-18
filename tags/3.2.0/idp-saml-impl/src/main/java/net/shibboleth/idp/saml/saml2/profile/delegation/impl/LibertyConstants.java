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

package net.shibboleth.idp.saml.saml2.profile.delegation.impl;

import javax.xml.namespace.QName;

import org.openliberty.xmltooling.Konstantz;
import org.openliberty.xmltooling.disco.MetadataAbstract;
import org.openliberty.xmltooling.disco.ProviderID;
import org.openliberty.xmltooling.disco.SecurityContext;
import org.openliberty.xmltooling.disco.SecurityMechID;
import org.openliberty.xmltooling.disco.ServiceType;
import org.openliberty.xmltooling.security.Token;
import org.openliberty.xmltooling.soapbinding.Sender;

/** Liberty-related constants. */
public final class LibertyConstants {

    // Various QNames that aren't defined currently in OpenLiberty.
    
    /** disco:Abstract element QName. */
    public static final QName DISCO_ABSTRACT_ELEMENT_NAME =
        new QName(Konstantz.DISCO_NS, MetadataAbstract.LOCAL_NAME, Konstantz.DISCO_PREFIX);
    
    /** disco:ServiceType element QName. */
    public static final QName DISCO_SERVICE_TYPE_ELEMENT_NAME = 
        new QName(Konstantz.DISCO_NS, ServiceType.LOCAL_NAME, Konstantz.DISCO_PREFIX);
    
    /** disco:ProviderID element QName. */
    public static final QName DISCO_PROVIDERID_ELEMENT_NAME = 
        new QName(Konstantz.DISCO_NS, ProviderID.LOCAL_NAME, Konstantz.DISCO_PREFIX);
    
    /** disco:SecurityContext element QName. */
    public static final QName DISCO_SECURITY_CONTEXT_ELEMENT_NAME = 
        new QName(Konstantz.DISCO_NS, SecurityContext.LOCAL_NAME, Konstantz.DISCO_PREFIX);
    
    /** disco:SecurityMechID element QName. */
    public static final QName DISCO_SECURITY_MECH_ID_ELEMENT_NAME = 
        new QName(Konstantz.DISCO_NS, SecurityMechID.LOCAL_NAME, Konstantz.DISCO_PREFIX);
    
    /** disco:ServiceType element QName. */
    public static final QName SECURITY_TOKEN_ELEMENT_NAME = 
        new QName(Konstantz.SEC_NS, Token.LOCAL_NAME, Konstantz.SEC_PREFIX);
    
    /** sb:Sender element QName. */
    public static final QName SOAP_BINDING_SENDER_ELEMENT_NAME = 
        new QName(Konstantz.SB_NS, Sender.LOCAL_NAME, Konstantz.SB_PREFIX);
    
    // Various constants relevant to the extension
    
    /** Liberty SOAP Binding 2.0 URI. */
    public static final String SOAP_BINDING_20_URI = Konstantz.SB_NS;
    
    /** SSOS ServiceType value. */
    public static final String SERVICE_TYPE_SSOS = "urn:liberty:ssos:2006-08";
    
    /** SecurityMechID 'urn:liberty:security:2005-02:ClientTLS:peerSAMLV2'. */
    public static final String SECURITY_MECH_ID_CLIENT_TLS_PEER_SAML_V2 =
        "urn:liberty:security:2005-02:ClientTLS:peerSAMLV2";
    
    /** sec:Token/@usage 'urn:liberty:security:tokenusage:2006-08:SecurityToken'. */
    public static final String TOKEN_USAGE_SECURITY_TOKEN = "urn:liberty:security:tokenusage:2006-08:SecurityToken";
    
    /** Data for SSOS EPR Metadata disco:Abstract. */
    public static final String SSOS_EPR_METADATA_ABSTRACT = "ID-WSF Single Sign-On Service";
    
    /** Liberty SSOS WS-Addressing inbound Action URI (AuthnRequest).*/
    public static final String SSOS_AUTHN_REQUEST_WSA_ACTION_URI = "urn:liberty:ssos:2006-08:AuthnRequest";
    
    /** Liberty SSOS WS-Addressing outbound Action URI (Response).*/
    public static final String SSOS_RESPONSE_WSA_ACTION_URI = "urn:liberty:ssos:2006-08:Response";
    
    /** The default path at the IdP for the SSOS endpoint, relative to the IdP context path. */
    public static final String DEFAULT_SSOS_ENDPOINT_URL_RELATIVE_PATH = "/profile/IDWSF/SSOS";
    
    /** The default port at the IdP for the SSOS endpoint. */
    public static final String DEFAULT_SSOS_ENDPOINT_URL_PORT = "8443";
    
    /** Constructor.  Prevent instantiation. */
    private LibertyConstants() { }

}
