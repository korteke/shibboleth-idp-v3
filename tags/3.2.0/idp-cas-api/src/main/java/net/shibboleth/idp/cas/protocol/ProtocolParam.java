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

package net.shibboleth.idp.cas.protocol;

/**
 * Protocol parameter name enumeration.
 *
 * @author Marvin S. Addison
 */
public enum ProtocolParam {

    /** Service identifier, which is typically a URL. */
    Service,

    /** Service ticket. */
    Ticket,

    /** Forced authentication flag. */
    Renew,

    /** Gateway authentication flag. */
    Gateway,

    /** Proxy-granting ticket. */
    Pgt,

    /** Proxy-granting ticket identifier sent to proxy callback URL. */
    PgtId,

    /** Proxy-granting ticket IOU identifier. */
    PgtIou,

    /** Proxy-granting ticket callback URL. */
    PgtUrl,

    /** Target service for proxy-granting ticket. */
    TargetService;


    /**
     * Converts enumeration name to lower-case name as used by CAS protocol document.
     *
     * @return Enumeration name with first letter lower-cased.
     */
    public String id() {
        return this.name().substring(0, 1).toLowerCase() + this.name().substring(1);
    }
}
