/*
 * See LICENSE for licensing and NOTICE for copyright.
 */

package net.shibboleth.idp.cas.ticket.impl;

import net.shibboleth.idp.cas.ticket.ProxyGrantingTicket;
import net.shibboleth.idp.cas.ticket.ProxyTicket;
import net.shibboleth.idp.cas.ticket.ServiceTicket;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.opensaml.storage.impl.MemoryStorageService;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for {@link SimpleTicketService} class.
 *
 * @author Marvin S. Addison
 */
public class SimpleTicketServiceTest {

    private static final String TEST_SESSION_ID = "jHXRo42W0ATPEN+X5Zk1cw==";

    private static final String TEST_SERVICE = "https://example.com/widget";

    private SimpleTicketService ticketService;

    @BeforeTest
    public void setUp() throws Exception {
        final MemoryStorageService ss = new MemoryStorageService();
        ss.setId("shibboleth.StorageService");
        ss.initialize();
        ticketService = new SimpleTicketService(ss);
    }


    @Test
    public void testCreateRemoveServiceTicket() throws Exception {
        final ServiceTicket st = createServiceTicket();
        assertNotNull(st);
        assertEquals(ticketService.removeServiceTicket(st.getId()), st);
        assertNull(ticketService.removeServiceTicket(st.getId()));
    }

    @Test
    public void testCreateFetchRemoveProxyGrantingTicket() throws Exception {
        final ProxyGrantingTicket pgt = createProxyGrantingTicket();
        assertNotNull(pgt);
        assertEquals(ticketService.fetchProxyGrantingTicket(pgt.getId()), pgt);
        assertEquals(ticketService.removeProxyGrantingTicket(pgt.getId()), pgt);
        assertNull(ticketService.removeProxyGrantingTicket(pgt.getId()));
    }

    @Test
    public void testCreateRemoveProxyTicket() throws Exception {
        final ProxyTicket pt = ticketService.createProxyTicket(
                new TicketIdentifierGenerationStrategy("PT", 25).generateIdentifier(),
                expiry(),
                createProxyGrantingTicket(),
                TEST_SERVICE);
        assertNotNull(pt);
        assertEquals(ticketService.removeProxyTicket(pt.getId()), pt);
        assertNull(ticketService.removeProxyTicket(pt.getId()));
    }

    private ServiceTicket createServiceTicket() {
        return ticketService.createServiceTicket(
                new TicketIdentifierGenerationStrategy("ST", 25).generateIdentifier(),
                expiry(),
                TEST_SESSION_ID,
                TEST_SERVICE,
                false);
    }

    private ProxyGrantingTicket createProxyGrantingTicket() {
        return ticketService.createProxyGrantingTicket(
                new TicketIdentifierGenerationStrategy("PGT", 50).generateIdentifier(),
                expiry(),
                createServiceTicket());
    }

    private static Instant expiry() {
        return DateTime.now().plusSeconds(10).toInstant();
    }
}
