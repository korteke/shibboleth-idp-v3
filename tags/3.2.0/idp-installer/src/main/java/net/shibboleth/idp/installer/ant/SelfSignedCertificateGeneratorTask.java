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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.security.SelfSignedCertificateGenerator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Task to shim around {@link SelfSignedCertificateGenerator}.
 */
public class SelfSignedCertificateGeneratorTask extends Task {

    /** Our wrapped {@link SelfSignedCertificateGenerator}. */
    private SelfSignedCertificateGenerator generator;

    /**
     * Constructor.
     */
    public SelfSignedCertificateGeneratorTask() {
        generator = new SelfSignedCertificateGenerator();
    }

    /**
     * Set the type of key that will be generated. Defaults to RSA.
     * 
     * @param type type of key that will be generated
     */
    public void setKeyType(@Nonnull @NotEmpty final String type) {
        generator.setKeyType(type);
    }

    /**
     * Set the size of the generated key. Defaults to 2048
     * 
     * @param size size of the generated key
     */
    public void setKeySize(@Positive final int size) {
        generator.setKeySize(size);
    }

    /**
     * Set the number of years for which the certificate will be valid.
     * 
     * @param lifetime number of years for which the certificate will be valid
     */
    public void setCertificateLifetime(@Positive final int lifetime) {
        generator.setCertificateLifetime(lifetime);
    }

    /**
     * Set the certificate algorithm that will be used. Defaults to SHA256withRSA.
     * 
     * @param alg certificate algorithm
     */
    public void setCertificateAlg(@Nonnull @NotEmpty final String alg) {
        generator.setCertificateAlg(alg);
    }

    /**
     * Set the hostname that will appear in the certificate's DN.
     * 
     * @param name hostname that will appear in the certificate's DN
     */
    public void setHostName(@Nonnull @NotEmpty final String name) {
        generator.setHostName(name);
    }

    /**
     * Set the file to which the private key will be written.
     * 
     * @param file file to which the private key will be written
     */
    public void setPrivateKeyFile(@Nullable final File file) {
        generator.setPrivateKeyFile(file);
    }

    /**
     * Set the file to which the certificate will be written.
     * 
     * @param file file to which the certificate will be written
     */
    public void setCertificateFile(@Nullable final File file) {
        generator.setCertificateFile(file);
    }

    /**
     * Set the type of keystore to create.
     * 
     * @param type keystore type
     */
    public void setKeystoreType(@Nonnull @NotEmpty final String type) {
        generator.setKeystoreType(type);
    }

    /**
     * Set the file to which the keystore will be written.
     * 
     * @param file file to which the keystore will be written
     */
    public void setKeystoreFile(@Nullable final File file) {
        generator.setKeystoreFile(file);
    }

    /**
     * Set the password for the generated keystore.
     * 
     * @param password password for the generated keystore
     */
    public void setKeystorePassword(@Nullable final String password) {
        generator.setKeystorePassword(password);
    }

    /**
     * Set the optional DNS subject alt names.
     * 
     * @param altNames collection of subject alt names.
     */
    public void setDNSSubjectAltNames(@Nonnull @NonnullElements final String altNames) {
        final List<String> nameList = StringSupport.stringToList(altNames, " ");
        generator.setDNSSubjectAltNames(nameList);
    }

    /**
     * Set the optional URI subject alt names.
     * 
     * @param subjectAltNames collection of subject alt names.
     */
    public void setURISubjectAltNames(@Nonnull @NonnullElements final String subjectAltNames) {
        final List<String> nameList = StringSupport.stringToList(subjectAltNames, " ");
        generator.setURISubjectAltNames(nameList);
    }

    @Override 
    /** {@inheritDoc} */
    public void execute() {
        try {
            generator.generate();
        } catch (Exception e) {
            log("Build failed", e, Project.MSG_ERR);
            throw new BuildException(e);
        }
    }
}
