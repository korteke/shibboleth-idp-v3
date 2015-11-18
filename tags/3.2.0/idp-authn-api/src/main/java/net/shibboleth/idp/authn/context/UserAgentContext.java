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

package net.shibboleth.idp.authn.context;

import java.net.InetAddress;

import javax.annotation.Nullable;

import org.opensaml.messaging.context.BaseContext;

/** A context, usually attached to {@link AuthenticationContext}, containing data about the user agent. */
public class UserAgentContext extends BaseContext {

    /** Address of the user-agent host. */
    @Nullable private InetAddress address;
    
    /** An identification string (such as a User-Agent header). */
    @Nullable private String identifier;

    /**
     * Get the address of the user-agent host.
     * 
     * @return address of the user-agent host
     */
    @Nullable public InetAddress getAddress() {
        return address;
    }

    /**
     * Get the user agent identifier.
     * 
     * @return identifier for the user agent
     */
    @Nullable public String getIdentifier() {
        return identifier;
    }
    
    /**
     * Set the address of the user-agent host.
     * 
     * @param userAgentAddress address of the user-agent host
     * 
     * @return this context
     */
    public UserAgentContext setAddress(@Nullable final InetAddress userAgentAddress) {
        address = userAgentAddress;
        return this;
    }

    /**
     * Set the user agent identifier.
     * 
     * @param id identifier for the user agent
     * 
     * @return this context
     */
    public UserAgentContext setIdentifier(@Nullable final String id) {
        identifier = id;
        return this;
    }

}