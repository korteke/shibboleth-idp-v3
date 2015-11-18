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

package net.shibboleth.idp.cas.config.impl;

import java.util.Comparator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.cas.service.impl.DefaultServiceComparator;
import net.shibboleth.idp.cas.ticket.impl.TicketIdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;

/**
 * CAS protocol configuration that applies to the following ticket validation URIs:
 *
 * <ul>
 *     <li><code>/proxyValidate</code></li>
 *     <li><code>/serviceValidate</code></li>
 * </ul>
 *
 * @author Marvin S. Addison
 */
public class ValidateConfiguration extends AbstractProtocolConfiguration {

    /** Ticket validation profile ID. */
    public static final String PROFILE_ID = PROTOCOL_URI + "/serviceValidate";

    /** Default ticket prefix. */
    public static final String DEFAULT_TICKET_PREFIX = "PGT";

    /** Default ticket length (random part). */
    public static final int DEFAULT_TICKET_LENGTH = 50;


    /** PGTIOU ticket ID generator. */
    @Nonnull
    private IdentifierGenerationStrategy pgtIOUGenerator = new TicketIdentifierGenerationStrategy("PGTIOU", 50);

    /** Component responsible for enforcing ticket requestor matches ticket validator. */
    @Nonnull
    private Comparator<String> serviceComparator = new DefaultServiceComparator();

    /** Name of IdP attribute to use for user returned in CAS ticket validation response. */
    @Nullable
    private String userAttribute;


    /** Creates a new instance. */
    public ValidateConfiguration() {
        super(PROFILE_ID);
    }

    /**
     * @return PGTIOU ticket ID generator.
     */
    @Nonnull
    public IdentifierGenerationStrategy getPGTIOUGenerator() {
        return pgtIOUGenerator;
    }

    /**
     * Sets the PGTIOU ticket ID generator.
     *
     * @param generator ID generator.
     */
    public void setPGTIOUGenerator(@Nonnull IdentifierGenerationStrategy generator) {
        this.pgtIOUGenerator = Constraint.isNotNull(generator, "PGTIOU generator cannot be null");
    }

    @Nonnull
    public Comparator<String> getServiceComparator() {
        return serviceComparator;
    }

    public void setServiceComparator(@Nonnull Comparator<String> serviceComparator) {
        this.serviceComparator = serviceComparator;
    }

    @Override
    @Nonnull
    protected String getDefaultTicketPrefix() {
        return DEFAULT_TICKET_PREFIX;
    }

    @Override
    @Nonnull
    protected int getDefaultTicketLength() {
        return DEFAULT_TICKET_LENGTH;
    }

    /** @return Name of IdP attribute to use for username returned in CAS ticket validation response. */
    @Nullable
    public String getUserAttribute() {
        return userAttribute;
    }

    /**
     * Sets the name of IdP attribute to use for username returned in CAS ticket validation response.
     *
     * @param attribute Attribute name to use
     */
    public void setUserAttribute(@Nullable String attribute) {
        this.userAttribute = attribute;
    }
}
