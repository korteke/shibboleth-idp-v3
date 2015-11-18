/*
 * See LICENSE for licensing and NOTICE for copyright.
 */

package net.shibboleth.idp.cas.ticket.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link TicketIdentifierGenerationStrategy}.
 *
 * @author Marvin S. Addison
 */
public class TicketIdentifierGenerationStrategyTest {

    private static final Pattern TICKET_REGEX = Pattern.compile("(.+)-(\\d+)-([A-Za-z0-9]+)(-(.+))?");

    @DataProvider(name = "generators")
    public Object[][] provideGenerators() {
        final TicketIdentifierGenerationStrategy gen1 = new TicketIdentifierGenerationStrategy("ST", 25);
        gen1.setSuffix("node_1-1");
        final TicketIdentifierGenerationStrategy gen2 = new TicketIdentifierGenerationStrategy("PT", 25);
        gen2.setSuffix("host1.example.com");
        final TicketIdentifierGenerationStrategy gen3 = new TicketIdentifierGenerationStrategy("PGT", 50);
        return new Object[][] {
                { gen1 , "ST", 25, "node_1-1"},
                { gen2 , "PT", 25, "host1.example.com" },
                { gen3, "PGT", 50, null },
        };
    }

    @DataProvider(name = "url-safety")
    public Object[][] provideBadPrefixSuffix() {
        return new Object[][] {
                {"<ST>", "host"},
                {"PGT", "http://host.example.com/"},
        };
    }

    @Test(dataProvider = "generators")
    public void testGenerate(
            final TicketIdentifierGenerationStrategy generator,
            final String expectedPrefix,
            final int expectedRandomLength,
            final String expectedSuffix) throws Exception {

        final long now = System.currentTimeMillis();
        final String id = generator.generateIdentifier();
        final Matcher m = TICKET_REGEX.matcher(id);
        assertTrue(m.matches());
        assertEquals(m.group(1), expectedPrefix);
        assertEquals(Long.parseLong(m.group(2)) / now, 1);
        assertEquals(m.group(3).length(), expectedRandomLength);
        if (expectedSuffix != null) {
            assertEquals(m.group(5), expectedSuffix);
        }
    }

    @Test(dataProvider = "url-safety",
          expectedExceptions = IllegalArgumentException.class)
    public void testUrlSafety(final String prefix, final String suffix) {
        new TicketIdentifierGenerationStrategy(prefix, 10).setSuffix(suffix);
    }

    @Test
    public void testConcurrentGeneration() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(20);
        final TicketIdentifierGenerationStrategy generator = new TicketIdentifierGenerationStrategy("ST", 50);
        final Collection<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            tasks.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return generator.generateIdentifier();
                }
            });
        }
        final List<Future<String>> results = executor.invokeAll(tasks);
        for (Future<String> result : results) {
            assertNotNull(result.get(1, TimeUnit.SECONDS));
        }
    }
}
