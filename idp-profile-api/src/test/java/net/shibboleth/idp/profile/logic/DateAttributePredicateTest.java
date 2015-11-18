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

package net.shibboleth.idp.profile.logic;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.opensaml.profile.context.ProfileRequestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Unit test for {@link DateAttributePredicate}.
 *
 * @author Marvin S. Addison
 */
public class DateAttributePredicateTest {

    private final DateTimeFormatter formatter = ISODateTimeFormat.dateTime();

    @DataProvider(name = "test-data")
    public Object[][] provideTestData() {
        return new Object[][] {
                // Future date matches
                new Object[] {
                        new DateAttributePredicate("expirationDate"),
                        "expirationDate",
                        dateStrings(Duration.standardDays(1)),
                        true,
                },
                // Current date does not match
                new Object[] {
                        new DateAttributePredicate("expirationDate"),
                        "expirationDate",
                        dateStrings(Duration.ZERO),
                        false,
                },
                // Past date does not match
                new Object[] {
                        new DateAttributePredicate("expirationDate"),
                        "expirationDate",
                        dateStrings(Duration.standardDays(-1)),
                        false,
                },
                // Increase target date by 90 days
                new Object[] {
                        newPredicate("expirationDate", Duration.standardDays(90)),
                        "expirationDate",
                        dateStrings(Duration.standardDays(91)),
                        true,
                },
                // Decrease target date by 30 days
                // e.g. expiration warning case
                new Object[] {
                        newPredicate("expirationDate", Duration.standardDays(-30)),
                        "expirationDate",
                        dateStrings(Duration.standardDays(29)),
                        false,
                },
        };
    }


    @Test(dataProvider = "test-data")
    public void testApply(
            final DateAttributePredicate predicate,
            final String attribute,
            final String[] values,
            final boolean expected) throws Exception {
        assertEquals(predicate.apply(createProfileRequestContext(attribute, values)), expected);
    }

    private ProfileRequestContext createProfileRequestContext(final String name, final String[] values) {
        final ProfileRequestContext prc = new ProfileRequestContext();
        final RelyingPartyContext rpc = new RelyingPartyContext();
        final IdPAttribute attribute = new IdPAttribute(name);
        final List<IdPAttributeValue<?>> attributeValues = new ArrayList<>();
        for (String value : values) {
            attributeValues.add(new StringAttributeValue(value));
        }
        attribute.setValues(attributeValues);
        final AttributeContext ac = new AttributeContext();
        ac.setIdPAttributes(Collections.singletonList(attribute));
        ac.setUnfilteredIdPAttributes(Collections.singletonList(attribute));
        rpc.addSubcontext(ac);
        prc.addSubcontext(rpc);
        return prc;
    }

    /**
     * Produces an array of date strings that are offsets from current system time.
     *
     * @param offsets One or more durations that are added to the current system time.
     *
     * @return Array of date strings, one for each provided offset.
     */
    private String[] dateStrings(final Duration ... offsets) {
        final String[] dates = new String[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            dates[i] = formatter.print(DateTime.now().plus(offsets[i]));
        }
        return dates;
    }

    private DateAttributePredicate newPredicate(final String attribute, final Duration offset) {
        final DateAttributePredicate p = new DateAttributePredicate(attribute);
        p.setSystemTimeOffset(offset);
        return p;
    }
}