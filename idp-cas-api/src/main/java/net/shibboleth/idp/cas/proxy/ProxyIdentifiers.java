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

package net.shibboleth.idp.cas.proxy;

import net.shibboleth.utilities.java.support.logic.Constraint;

import javax.annotation.Nonnull;

/**
 * Container for identifiers used in authenticating a proxy callback endpoint.
 *
 * @author Marvin S. Addison
 */
public class ProxyIdentifiers {

    /** Proxy-granting ticket ID. */
    @Nonnull
    private final String pgtId;

    /** Proxy-granting ticket IOU. */
    @Nonnull
    private final String pgtIou;

    public ProxyIdentifiers(@Nonnull final String pgtId, @Nonnull final String pgtIou) {
        this.pgtId = Constraint.isNotNull(pgtId, "PGT cannot be null");
        this.pgtIou = Constraint.isNotNull(pgtIou, "PGTIOU cannot be null");
    }

    @Nonnull
    public String getPgtId() {
        return pgtId;
    }

    @Nonnull
    public String getPgtIou() {
        return pgtIou;
    }
}
