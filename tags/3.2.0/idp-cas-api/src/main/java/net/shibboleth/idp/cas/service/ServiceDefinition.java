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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Defines a registered CAS service (i.e. relying party).
 *
 * @author Marvin S. Addison
 */
public class ServiceDefinition {
    /** Service identifier. */
    @Nonnull
    private final String id;

    /** Logical group to which service belongs. */
    @Nullable
    private String group;

    /** Proxy authorization flag. */
    private boolean authorizedToProxy;

    /** Indicates whether a service wants to receive SLO messages. */
    private boolean singleLogoutParticipant;


    /**
     * Creates a new instance with the given id.
     *
     * @param regex Service identifier. For historical reasons this parameter is named "regex" but will be renamed
     *              in a future version.
     */
    public ServiceDefinition(@Nonnull @NotEmpty final String regex) {
        this.id = Constraint.isNotNull(StringSupport.trimOrNull(regex), "ID cannot be null or empty");
    }

    /** @return Service identifier. */
    @Nonnull public String getId() {
        return id;
    }

    /**
     * @return Group name to which services matching this definition belong.
     */
    @Nullable public String getGroup() {
        return group;
    }

    /**
     * Sets the group name.
     *
     * @param group Group name.
     */
    public void setGroup(@NotEmpty final String group) {
        this.group = StringSupport.trimOrNull(group);
    }

    /** @return True if proxy is authorized, false otherwise. */
    public boolean isAuthorizedToProxy() {
        return authorizedToProxy;
    }

    /**
     * Sets the proxy authorization flag.
     *
     * @param proxy True to allow the service to request proxy-granting tickets, false otherwise.
     */
    public void setAuthorizedToProxy(final boolean proxy) {
        this.authorizedToProxy = proxy;
    }

    /** @return True to indicate the service wants to receive SLO messages, false otherwise. */
    public boolean isSingleLogoutParticipant() {
        return singleLogoutParticipant;
    }

    /**
     * Determines whether the service participates in SLO.
     *
     * @param wantsSLO True to indicate the service wants to receive SLO messages, false otherwise.
     */
    public void setSingleLogoutParticipant(final boolean wantsSLO) {
        this.singleLogoutParticipant = wantsSLO;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null) {
            return false;
        }
        return o instanceof ServiceDefinition && id.equals(((ServiceDefinition) o).getId());
    }

    @Override
    public int hashCode() {
        return 97 + id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
