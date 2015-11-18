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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beust.jcommander.Parameter;

/** Command line processing for reload-metadata flow. */
public class ReloadMetadataArguments extends AbstractCommandLineArguments {

    /** Attribute requester identity. */
    @Parameter(names = {"-id", "--provider"}, required = true, description = "Metadata Provider ID")
    @Nullable private String id;

    /** {@inheritDoc} */
    @Override
    protected StringBuilder doBuildURL(@Nonnull final StringBuilder builder) {
        
        if (getPath() == null) {
            builder.append("/profile/admin/reload-metadata");
        }
        
        if (builder.toString().contains("?")) {
            builder.append('&');
        } else {
            builder.append('?');
        }
        
        try {
            builder.append("id=").append(URLEncoder.encode(id, "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            // UTF-8 is a required encoding. 
        }
        
        return builder;
    }
        
}