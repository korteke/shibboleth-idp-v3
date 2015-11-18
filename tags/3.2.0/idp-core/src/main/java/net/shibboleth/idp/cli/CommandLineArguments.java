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

package net.shibboleth.idp.cli;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nonnull;

/**
 * Interface for JCommander command line argument handling for an HTTP-based remote service call. 
 */
public interface CommandLineArguments {
    
    /**
     * Should command usage be displayed?
     * 
     * @return  true iff this is a help request
     */
    boolean isUsage();

    /**
     * Validate the parameter set.
     * 
     * @throws IllegalArgumentException if the parameters are invalid
     */
    void validate();
    
    /**
     * Compute the full URL to connect to.
     * 
     * @return the URL to connect to
     * 
     * @throws MalformedURLException if the URL constructed is invalid 
     */
    @Nonnull public URL buildURL() throws MalformedURLException;
    
}