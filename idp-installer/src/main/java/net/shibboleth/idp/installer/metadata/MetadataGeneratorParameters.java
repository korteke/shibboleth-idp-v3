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

package net.shibboleth.idp.installer.metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.springframework.core.io.Resource;

/**
 * POJO to collect parameters from the metadata configuration (partially via spring).
 */
public class MetadataGeneratorParameters {

    /**
     * The file with the certificate the IDP uses to encrypt.
     */
    private File encryptionCert;

    /**
     * The file with the certificate that TLS uses to 'sign'.
     */
    private File backChannelCert;

    /**
     * The file with the certificate the IDP uses to sign.
     */
    private File signingCert;

    /** The entityID. */
    private String entityID;

    /** The DNS name. */
    private String dnsName;

    /** The scope. */
    private String scope;
    
    /**
     * Get the string representation of the encryption cert.
     * 
     * @return Returns the encryptionCert.
     * @throws IOException if badness occurrs
     */
    @Nullable public List<String> getEncryptionCert() throws IOException {
        return getCertificateContents(encryptionCert);
    }

    /**
     * Set the encryption Certificate file.
     * 
     * @param file what to set.
     */
    public void setEncryptionCert(File file) {

        encryptionCert = file;
    }

    /**
     * Set the encryption Certificate file.
     * 
     * @param resource what to set.
     */
    public void setEncryptionCertResource(Resource resource) {

        try {
            encryptionCert = resource.getFile();
        } catch (IOException e) {
            encryptionCert = null;
        }
    }

    /**
     * Get the string representation of the signing cert.
     * 
     * @return Returns the encryptionCert.
     * @throws IOException if badness occurrs
     */
    @Nullable public List<String> getSigningCert() throws IOException {
        return getCertificateContents(signingCert);
    }

    /**
     * Set the signing Certificate file.
     * 
     * @param file what to set.
     */
    public void setSigningCert(File file) {

        signingCert = file;
    }

    /**
     * Set the signing Certificate file.
     * 
     * @param resource what to set.
     */
    public void setSigningCertResource(Resource resource) {


        try {
            signingCert = resource.getFile();
        } catch (IOException e) {
            signingCert = null;
        }
    }

    /**
     * Get the string representation of the back channel cert.
     * 
     * @return Returns the encryptionCert.
     * @throws IOException if badness occurrs
     */
    @Nullable public List<String> getBackchannelCert() throws IOException {
        return getCertificateContents(backChannelCert);
    }

    /**
     * Set the Backchannel Certificate file.
     * 
     * @param file what to set.
     */
    public void setBackchannelCert(File file) {

        backChannelCert = file;
    }

    /**
     * Open the file and return the contents and a list of lines.
     * 
     * @param file the file
     * @return the contents
     * @throws IOException if badness occurrs.
     */
    private List<String> getCertificateContents(File file) throws IOException {
        if (null == file || !file.exists()) {
            return null;
        }
        final FileReader fr = new FileReader(file);
        final BufferedReader reader = new BufferedReader(fr);

        try {
            final List<String> output = new ArrayList<>();
            String s = reader.readLine();
            while (s != null) {
                output.add(s);
                s = reader.readLine();
            }
            if ((output.size() > 0) && output.get(0).startsWith("----")) {
                output.remove(0);
            }
            final int last = output.size() - 1;
            if (last <= 0) {
                return null;
            }
            if (output.get(last).startsWith("----")) {
                output.remove(last);
            }
            return output;

        } finally {
            try {
                reader.close();
                fr.close();
            } catch (IOException e1) {
            }
        }
    }

    /**
     * Returns the entityID.
     * 
     * @return the entityID.
     */
    public String getEntityID() {
        return entityID;
    }

    /**
     * Sets the entityID.
     * 
     * @param id what to set.
     */
    public void setEntityID(String id) {
        entityID = id;
    }

    /**
     * Returns the dnsName.
     * 
     * @return the dnsname.
     */
    public String getDnsName() {
        return dnsName;
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
     * Returns the scope.
     * 
     * @return the scope.
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the scope.
     * 
     * @param value what to set.
     */
    public void setScope(String value) {
        scope = value;
    }

}
