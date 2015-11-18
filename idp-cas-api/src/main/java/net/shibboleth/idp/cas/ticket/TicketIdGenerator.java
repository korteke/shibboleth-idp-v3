/*
 * See LICENSE for licensing and NOTICE for copyright.
 */

package net.shibboleth.idp.cas.ticket;

import javax.annotation.Nonnull;

/**
 * Strategy for ticket generation.
 *
 * @author Marvin S. Addison
 */
public interface TicketIdGenerator {

    /**
     * Generates a ticket identifier.
     *
     * @return Ticket identifier.
     */
    @Nonnull
    String generate();
}
