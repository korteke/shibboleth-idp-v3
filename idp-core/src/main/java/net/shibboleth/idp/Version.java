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

package net.shibboleth.idp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Class for getting and printing the version of the IdP. */
public final class Version {

    /** Name of {@link org.slf4j.MDC} attribute that holds the IdP's version string: <code>idp.version</code>. */
    @Nonnull public static final String MDC_ATTRIBUTE = "idp.version";

    /** IdP version. */
    @Nullable private static final String VERSION = Version.class.getPackage().getImplementationVersion();

    /** Constructor. */
    private Version() {
    }

    /**
     * Main entry point to program.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println(VERSION);
    }

    /**
     * Get the version of the IdP.
     * 
     * @return version of the IdP
     */
    @Nullable public static String getVersion() {
        return VERSION;
    }
}