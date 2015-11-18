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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.Nonnull;

import net.shibboleth.idp.installer.PropertiesWithComments;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * A class to merge a property file into another property file, preserving the comments. 
 */
public class MergePropertiesTask extends Task {

    /** The input file. */
    private File inFile;
    
    /** The output file. */
    private File outFile;

    /** The merge file. */
    private File mergeFile;
    
    /** Set the input file.
     * @param what what to set
     */
    public void setInFile(@Nonnull File what) {
        inFile = Constraint.isNotNull(what, "Provided file must not be null");
    }
    
    /** Set the output file.
     * @param what what to set
     */
    public void setOutFile(@Nonnull File what) {
        outFile = Constraint.isNotNull(what, "Provided file must not be null");
    }

    /** Set the merge file.
     * @param what what to set
     */
    public void setMergeFile(@Nonnull File what) {
        mergeFile = Constraint.isNotNull(what, "Provided file must not be null");
    }

    /** {@inheritDoc} */
    // Checkstyle: CyclomaticComplexity OFF
    @Override
    public void execute() {
        if (null == inFile) {
            log("Input file not provided", Project.MSG_ERR);
            throw new BuildException("Non existant input file");
        }
        if (!inFile.exists()) {
            log("Input file " + inFile.getAbsolutePath() + " does not exist");
            throw new BuildException("Non existant input file");
        }
        if (null == outFile) {
            log("Output file not provided, input taken", Project.MSG_INFO);
        }
        if (null == mergeFile) {
            log("Merge file not provided", Project.MSG_ERR);
            throw new BuildException("Non existant input file");
        }
        if (!mergeFile.exists()) {
            log("Input file " + mergeFile.getAbsolutePath() + " does not exist");
            throw new BuildException("Non existant merge file");
        }
        
        final PropertiesWithComments in = new PropertiesWithComments(); 

        try {
            in.load(new FileInputStream(inFile));
        } catch (IOException e) {
            log("Could not load input " + inFile.getAbsolutePath(), e, Project.MSG_ERR);
            throw new BuildException(e);
        }
        
        final Properties merge = new Properties(); 
        try {
            merge.load(new FileInputStream(mergeFile));
        } catch (IOException e) {
            log("Could not load merge " + mergeFile.getAbsolutePath(), e, Project.MSG_ERR);
            throw new BuildException(e);
        }
        
        
        for (Object propName:merge.keySet()) {
            if (propName instanceof String) {
                String name = (String) propName;
                in.replaceProperty(name, merge.getProperty(name));
            } 
        }

        try {
            in.store(new FileOutputStream(outFile));
        } catch (IOException e) {
            log("Could not store output " + outFile.getAbsolutePath(), e, Project.MSG_ERR);
            throw new BuildException(e);
        }
    }
    // Checkstyle: CyclomaticComplexity ON
}
