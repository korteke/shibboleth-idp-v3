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

package net.shibboleth.idp.cas.service;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.Principal;

/**
 * Container for metadata about a CAS service (i.e. relying party).
 *
 * @author Marvin S. Addison
 */
public class Service implements Principal {

    /** Service URL. */
    @Nonnull
    @NotEmpty
    private final String serviceURL;

    /** Group to which service belongs. */
    @Nullable
    private final String group;

    /** Proxy authorization flag. */
    private final boolean authorizedToProxy;

    /** Indicates whether a service wants to receive SLO messages. */
    private final boolean singleLogoutParticipant;


    /**
     * Creates a new service that does not participate in SLO.
     *
     * @param url CAS service URL.
     * @param group Group to which service belongs.
     * @param proxy True to authorize proxying, false otherwise.
     */
    public Service(
            @Nonnull @NotEmpty final String url,
            @Nullable @NotEmpty final String group,
            final boolean proxy) {
        this(url, group, proxy, false);
    }

    /**
     * Creates a new service that MAY participate in SLO.
     *
     * @param url CAS service URL.
     * @param group Group to which service belongs.
     * @param proxy True to authorize proxying, false otherwise.
     * @param wantsSLO True to indicate the service wants to receive SLO messages, false otherwise.
     */
    public Service(
            @Nonnull @NotEmpty final String url,
            @Nullable @NotEmpty final String group,
            final boolean proxy,
            final boolean wantsSLO) {
        this.serviceURL = Constraint.isNotNull(StringSupport.trimOrNull(url), "Service URL cannot be null or empty");
        this.group = StringSupport.trimOrNull(group);
        this.authorizedToProxy = proxy;
        this.singleLogoutParticipant = wantsSLO;
    }

    /** @return Service URL. */
    @Override
    public String getName() {
        return serviceURL;
    }

    /** @return Service group name. */
    @Nullable
    public String getGroup() {
        return group;
    }

    /** @return True if proxying is authorized, false otherwise. */
    public boolean isAuthorizedToProxy() {
        return authorizedToProxy;
    }

    /** @return True to indicate the service wants to receive SLO messages, false otherwise. */
    public boolean isSingleLogoutParticipant() {
        return singleLogoutParticipant;
    }

    @Override
    public String toString() {
        return serviceURL;
    }
}
