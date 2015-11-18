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

package net.shibboleth.idp.consent.context.impl;

import java.util.Map;

import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.idp.consent.impl.ConsentTestingSupport;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link ConsentContext} unit test. */
public class ConsentContextTest {

    private ConsentContext ctx;

    private Map<String, Consent> map;

    @BeforeMethod public void setUp() {
        ctx = new ConsentContext();

        map = ConsentTestingSupport.newConsentMap();
    }

    @Test public void testInstantiation() {
        Assert.assertTrue(ctx.getCurrentConsents().isEmpty());
        Assert.assertTrue(ctx.getPreviousConsents().isEmpty());
    }

    @Test public void testCurrentConsents() {
        ctx.getCurrentConsents().putAll(map);
        Assert.assertEquals(map, ctx.getCurrentConsents());
    }

    @Test public void testPreviousConsents() {
        ctx.getPreviousConsents().putAll(map);
        Assert.assertEquals(map, ctx.getPreviousConsents());
    }

}
