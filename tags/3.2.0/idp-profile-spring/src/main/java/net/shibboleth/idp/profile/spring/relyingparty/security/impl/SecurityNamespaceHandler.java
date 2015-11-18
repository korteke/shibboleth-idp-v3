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

package net.shibboleth.idp.profile.spring.relyingparty.security.impl;

import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.BaseSpringNamespaceHandler;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl.BasicInlineCredentialParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl.BasicResourceCredentialParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl.X509InlineCredentialParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl.X509ResourceCredentialParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl.CertPathPKIXValidationOptionsParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl.ChainingParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl.PKIXInlineValidationInfoParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl.PKIXResourceValidationInfoParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl.PKIXValidationOptionsParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl.SignatureChainingParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl.StaticExplicitKeyParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl.StaticExplicitKeySignatureParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl.StaticPKIXSignatureParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl.StaticPKIXX509CredentialParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl.UnsupportedTrustEngineParser;

/** Namespace handler <em>{@value NAMESPACE}</em>. */
public class SecurityNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Credential element name. */
    public static final QName CREDENTIAL_ELEMENT_NAME = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "Credential");

    /** SecurityPolicy element name. */
    public static final QName SECURITY_POLICY_NAME = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "SecurityPolicy");

    /** TrustEngineRef element name. */
    public static final QName TRUST_ENGINE_REF = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "TrustEngineRef");

    /** {@inheritDoc} */
    @Override public void init() {
        // Credentials
        registerBeanDefinitionParser(X509ResourceCredentialParser.TYPE_NAME_FILESYSTEM,
                new X509ResourceCredentialParser());
        registerBeanDefinitionParser(X509ResourceCredentialParser.TYPE_NAME_RESOURCE,
                new X509ResourceCredentialParser());
        registerBeanDefinitionParser(X509InlineCredentialParser.TYPE_NAME, new X509InlineCredentialParser());
        registerBeanDefinitionParser(BasicInlineCredentialParser.TYPE_NAME, new BasicInlineCredentialParser());
        registerBeanDefinitionParser(BasicResourceCredentialParser.TYPE_NAME_FILESYSTEM,
                new BasicResourceCredentialParser());
        registerBeanDefinitionParser(BasicResourceCredentialParser.TYPE_NAME_RESOURCE,
                new BasicResourceCredentialParser());

        registerBeanDefinitionParser(StaticExplicitKeySignatureParser.TYPE_NAME, 
                new StaticExplicitKeySignatureParser());
        registerBeanDefinitionParser(StaticPKIXSignatureParser.TYPE_NAME, new StaticPKIXSignatureParser());
        registerBeanDefinitionParser(SignatureChainingParser.TYPE_NAME, new SignatureChainingParser());

        // Metadata based unsupported
        registerBeanDefinitionParser(UnsupportedTrustEngineParser.METADATA_EXPLICIT_KEY_TYPE,
                new UnsupportedTrustEngineParser());
        registerBeanDefinitionParser(UnsupportedTrustEngineParser.METADATA_EXPLICIT_KEY_SIGNATURE_TYPE,
                new UnsupportedTrustEngineParser());
        registerBeanDefinitionParser(UnsupportedTrustEngineParser.METADATA_PKIX_CREDENTIAL_TYPE,
                new UnsupportedTrustEngineParser());
        registerBeanDefinitionParser(UnsupportedTrustEngineParser.METADATA_PKIX_SIGNATURE_TYPE,
                new UnsupportedTrustEngineParser());

        // Validation Info
        registerBeanDefinitionParser(PKIXResourceValidationInfoParser.TYPE_NAME_FILESYSTEM,
                new PKIXResourceValidationInfoParser());
        registerBeanDefinitionParser(PKIXResourceValidationInfoParser.TYPE_NAME_RESOURCE,
                new PKIXResourceValidationInfoParser());
        registerBeanDefinitionParser(PKIXInlineValidationInfoParser.SCHEMA_TYPE, new PKIXInlineValidationInfoParser());

        // Validation Opts
        registerBeanDefinitionParser(PKIXValidationOptionsParser.ELEMENT_NAME, new PKIXValidationOptionsParser());
        registerBeanDefinitionParser(CertPathPKIXValidationOptionsParser.ELEMENT_NAME,
                new CertPathPKIXValidationOptionsParser());

        //
        // Trust Engines needed for the HttpMetadataProvider
        //
        registerBeanDefinitionParser(ChainingParser.TYPE_NAME, new ChainingParser());
        registerBeanDefinitionParser(StaticExplicitKeyParser.TYPE_NAME, new StaticExplicitKeyParser());
        registerBeanDefinitionParser(StaticPKIXX509CredentialParser.TYPE_NAME, new StaticPKIXX509CredentialParser());

    }
}