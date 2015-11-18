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

package net.shibboleth.idp.installer.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.installer.metadata.MetadataGenerator;
import net.shibboleth.idp.installer.metadata.MetadataGeneratorParameters;
import net.shibboleth.idp.spring.IdPPropertiesApplicationContextInitializer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Task to generate metadata.
 */
public class MetadataGeneratorTask extends Task {

    /** Where we collect the parameters. */

    /** where to put the data. */
    private File outputFile;

    /** Where idp.home is. */
    @Nullable private String idpHome;

    /** Ant level override for the encryption certificate. */
    @Nullable private File encryptionCert;

    /** Ant level override for the signing certificate. */
    @Nullable private File signingCert;

    /** Ant level override for the back channel certificate. */
    @Nullable private File backchannelCert;

    /** Ant level override for the entity ID. */
    @Nullable private String entityID;

    /** Ant level override for the DNS name. */
    @Nullable private String dnsName;

    /** Ant level override for the scope. */
    @Nullable private String scope;

    /**
     * Whether to comment out the SAML2 AA port.
     */
    private boolean saml2AttributeQueryCommented = true;

    /**
     * Whether to comment out the SAML2 SLO endpoints.
     */
    private boolean saml2LogoutCommented = true;

    /**
     * Where is idp.home.
     * 
     * @return Returns idpHome.
     */
    @Nullable public String getIdpHome() {
        return idpHome;
    }

    /**
     * Set where where is idp.home.
     * 
     * @param home The idpHome to set.
     */
    public void setIdpHome(@Nullable String home) {
        idpHome = home;
    }

    /**
     * Set the output file.
     * 
     * @param file what to set.
     */
    public void setOutput(File file) {

        outputFile = file;
    }

    /**
     * Set the encryption Certificate file. Overrides the Spring definition.
     * 
     * @param file what to set.
     */
    public void setEncryptionCert(File file) {
        encryptionCert = file;
    }

    /**
     * Set the signing Certificate file. Overrides the Spring definition.
     * 
     * @param file what to set.
     */
    public void setSigningCert(File file) {
        signingCert = file;
    }

    /**
     * Set the Backchannel Certificate file.
     * 
     * @param file what to set.
     */
    public void setBackchannelCert(File file) {
        backchannelCert = file;
    }

    /**
     * Sets the entityID. Overrides the Spring definition.
     * 
     * @param id what to set.
     */
    public void setEntityID(String id) {
        entityID = id;
    }

    /**
     * Sets the dns name.
     * 
     * @param name what to set.
     */
    public void setDnsName(String name) {
        dnsName = name;
    }

    /**
     * Sets the scope. Overrides the Spring definition.
     * 
     * @param value what to set.
     */
    public void setScope(String value) {
        scope = value;
    }

    /** Returns whether to comment the SAML2 AA endpoint.
     * @return Returns when to comment the SAML2 AA endpoint.
     */
    public boolean isSAML2AttributeQueryCommented() {
        return saml2AttributeQueryCommented;
    }

    /** Sets whether to comment the SAML2 AA endpoint.
     * @param asComment whether to comment or not.
     */
    public void setSAML2AttributeQueryCommented(boolean asComment) {
        saml2AttributeQueryCommented = asComment;
    }

    /** Returns whether to comment the SAML2 Logout endpoints.
     * @return  whether to comment the SAML2 Logout endpoints
     */
    public boolean isSAML2LogoutCommented() {
        return saml2LogoutCommented;
    }

    /** Sets whether to comment the SAML2 Logout endpoints.
     * @param asComment whether to comment or not
     */
    public void setSAML2LogoutCommented(boolean asComment) {
        saml2LogoutCommented = asComment;
    }
    
    /** {@inheritDoc} */
    // Checkstyle: CyclomaticComplexity OFF
    @Override public void execute() {
        try {
            final MetadataGeneratorParameters parameters;

            final Resource resource = new ClassPathResource("net/shibboleth/idp/installer/metadata-generator.xml");

            final ApplicationContextInitializer initializer = new Initializer();

            final GenericApplicationContext context =
                    SpringSupport.newContext(MetadataGeneratorTask.class.getName(),
                            Collections.singletonList(resource), Collections.<BeanFactoryPostProcessor>emptyList(),
                            Collections.<BeanPostProcessor>emptyList(), Collections.singletonList(initializer), null);

            parameters = context.getBean("IdPConfiguration", MetadataGeneratorParameters.class);

            if (encryptionCert != null) {
                parameters.setEncryptionCert(encryptionCert);
            }
            if (signingCert != null) {
                parameters.setSigningCert(signingCert);
            }
            if (backchannelCert != null) {
                parameters.setBackchannelCert(backchannelCert);
            }


            final MetadataGenerator generator = new MetadataGenerator(outputFile);
            final List<List<String>> signing = new ArrayList<>(2);
            List<String> value = parameters.getBackchannelCert();
            if (null != value) {
                signing.add(value);
            }
            value = parameters.getSigningCert();
            if (null != value) {
                signing.add(value);
            }
            generator.setSigningCerts(signing);
            value = parameters.getEncryptionCert();
            if (null != value) {
                generator.setEncryptionCerts(Collections.singletonList(value));
            }
            if (dnsName != null) {
                generator.setDNSName(dnsName);
            } else {
                generator.setDNSName(parameters.getDnsName());
            }
            if (entityID != null) {
                generator.setEntityID(entityID);
            } else {
                generator.setEntityID(parameters.getEntityID());   
            }
            if (scope != null) {
                generator.setScope(scope);
            } else {
                generator.setScope(parameters.getScope());
            }
            generator.setSAML2AttributeQueryCommented(isSAML2AttributeQueryCommented());
            generator.setSAML2LogoutCommented(isSAML2LogoutCommented());
            generator.generate();

        } catch (Exception e) {
            log("Build failed", e, Project.MSG_ERR);
            throw new BuildException(e);
        }
    }
    // Checkstyle: CyclomaticComplexity ON

    /**
     * An initializer which knows about our idp.home.
     * 
     */
    public class Initializer extends IdPPropertiesApplicationContextInitializer {
        @Override @Nonnull public String[] getSearchLocations() {
            if (null == idpHome) {
                return super.getSearchLocations();
            }
            final String[] result = new String[1];
            result[0] = idpHome;
            return result;
        }
    }
}
