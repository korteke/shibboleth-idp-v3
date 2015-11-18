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

import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Service ticket validation response protocol message.
 *
 * @author Marvin S. Addison
 */
public class TicketValidationResponse extends AbstractProtocolResponse {

    @Nullable private String userName;

    @Nonnull private Map<String, List<String>> attributes = new HashMap<>();

    @Nullable private String pgtIou;

    @Nonnull private List<String> proxies = new ArrayList<>();


    /** @return Non-null subject principal on ticket validation success. */
    @Nullable public String getUserName() {
        return userName;
    }

    public void setUserName(@Nonnull final String user) {
        Constraint.isNotNull(user, "Username cannot be null");
        this.userName = user;
    }

    /** @return Immutable map of user attributes. */
    @Nonnull public Map<String, List<String>> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Adds an attribute to the attribute mapping.
     *
     * @param proxy Name of a proxying service, typically a URI.
     */
    public void addAttribute(final String name, final String value) {
        List<String> values = attributes.get(name);
        if (values == null) {
            values = new ArrayList<>();
            attributes.put(name, values);
        }
        values.add(value);
    }

    @Nullable public String getPgtIou() {
        return pgtIou;
    }

    public void setPgtIou(@Nullable final String pgtIou) {
        this.pgtIou = StringSupport.trimOrNull(pgtIou);
    }

    /** @return Immutable list of proxies traversed in order of most recent to last recent. */
    @Nonnull public List<String> getProxies() {
        return Collections.unmodifiableList(proxies);
    }

    /**
     * Adds a proxy to the list of proxies traversed.
     *
     * @param proxy Name of a proxying service, typically a URI.
     */
    public void addProxy(final String proxy) {
        proxies.add(proxy);
    }
}
