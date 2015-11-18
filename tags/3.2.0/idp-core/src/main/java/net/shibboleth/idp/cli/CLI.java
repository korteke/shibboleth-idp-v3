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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import com.beust.jcommander.JCommander;


/**
 * Entry point for command line attribute utility.
 */
public final class CLI {

    /** Name of system property for command line argument class. */
    @Nonnull @NotEmpty public static final String ARGS_PROPERTY = "net.shibboleth.idp.cli.arguments";
    
    /** Constructor. */
    private CLI() {
        
    }

    /**
     * Command line entry point.
     * 
     * @param args  command line arguments
     */
    public static void main(@Nonnull final String[] args) {

        // Get name of parameter class to load using system property.
        final String argType = System.getProperty(ARGS_PROPERTY);
        if (argType == null) {
            errorAndExit(ARGS_PROPERTY + " system property not set");
        }

        CommandLineArguments argObject = null;
        
        try {
            final Object obj = Class.forName(argType).newInstance();
            if (!(obj instanceof CommandLineArguments)) {
                errorAndExit("Argument class was not of the correct type");
            }
            argObject = (CommandLineArguments) obj;
            final JCommander jc = new JCommander(argObject, args);
            if (argObject.isUsage()) {
                jc.usage();
                return;
            }
        } catch (final ClassNotFoundException e) {
            errorAndExit("Argument class " + argType + " not found ");
        } catch (final InstantiationException | IllegalAccessException e) {
            errorAndExit(e.getMessage());
        }
        
        try {
            argObject.validate();
        } catch (final IllegalArgumentException e) {
            errorAndExit(e.getMessage());
        }
        
        doRequest(argObject);
    }

    /**
     * Make a request using the arguments established.
     * 
     * @param args  the populated command line arguments
     */
    private static void doRequest(@Nonnull final CommandLineArguments args) {
        URL url = null;
        try {
            url = args.buildURL();
            try (final InputStream stream = url.openStream()) {
                try (final InputStreamReader reader = new InputStreamReader(stream)) {
                    try (final BufferedReader in = new BufferedReader(reader)) {
                        String line;
                        while((line = in.readLine()) != null) {
                            System.out.println(line);
                        }
                    }
                }
            }
        } catch (final MalformedURLException e) {
            errorAndExit(e.getMessage());
        } catch (final IOException e) {
            errorAndExit((url != null ? "(" + url.toString() + ") " : "") + e.getMessage());
        }
    }
    
    /**
     * Logs, as an error, the error message and exits the program.
     * 
     * @param errorMessage error message
     */
    private static void errorAndExit(@Nonnull final String errorMessage) {
        System.err.println(errorMessage);
        System.exit(1);
    }
    
}