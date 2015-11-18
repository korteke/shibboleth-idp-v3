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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.security.BasicKeystoreKeyStrategyTool;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Wrapper around {@link BasicKeystoreKeyStrategyTool}.
 */
public class BasicKeystoreKeyStrategyTask extends Task {

    /** encapsulated {@link BasicKeystoreKeyStrategyTool}.*/
    private BasicKeystoreKeyStrategyTool tool;
    
    /** Constructor. */
    public BasicKeystoreKeyStrategyTask() {
        tool = new BasicKeystoreKeyStrategyTool();
    }
    
    /**
     * Set the type of key that will be generated. Defaults to AES.
     * 
     * @param type type of key that will be generated
     */
    public void setKeyType(@Nonnull @NotEmpty final String type) {
        tool.setKeyType(type);
    } 

    /**
     * Set the size of the generated key. Defaults to 128
     * 
     * @param size size of the generated key
     */
    public void setKeySize(@Positive final int size) {
        tool.setKeySize(size);
    } 
    
    /**
     * Set the encryption key alias base name.
     * 
     * @param alias the encryption key alias base
     */
    public void setKeyAlias(@Nonnull @NotEmpty final String alias) {
        tool.setKeyAlias(alias);
    } 

    /**
     * Set the number of keys to maintain. Defaults to 3.
     * 
     * @param count number of keys to maintain
     */
    public void setKeyCount(@Positive final int count) {
        tool.setKeyCount(count);
    } 

    /**
     * Set the type of keystore to create. Defaults to JCEKS.
     * 
     * @param type keystore type
     */
    public void setKeystoreType(@Nonnull @NotEmpty final String type) {
        tool.setKeystoreType(type);
    } 

    /**
     * Set the keystore file to create or modify.
     * 
     * @param file keystore file
     */
    public void setKeystoreFile(@Nonnull final File file) {
        tool.setKeystoreFile(file);
    } 

    /**
     * Set the password for the keystore.
     * 
     * @param password password for the keystore
     */
    public void setKeystorePassword(@Nullable final String password) {
        tool.setKeystorePassword(password);
    } 
    
    /**
     * Set the key versioning file to create or modify.
     * 
     * @param file key versioning file
     */
    public void setVersionFile(@Nonnull final File file) {
        tool.setVersionFile(file);
    } 

    /** {@inheritDoc} */
    @Override
    public void execute() {
        try {
            tool.changeKey();
        } catch (Exception e) {
            log("Build failed", e, Project.MSG_ERR);
            throw new BuildException(e);
        }
    }
}
