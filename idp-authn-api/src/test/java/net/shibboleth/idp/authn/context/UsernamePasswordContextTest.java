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

package net.shibboleth.idp.authn.context;

import net.shibboleth.idp.authn.context.UsernamePasswordContext;

import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link UsernamePasswordContext} unit test. */
public class UsernamePasswordContextTest {

    /** Tests mutating the username. */
    @Test public void testUsername() {
        UsernamePasswordContext ctx = new UsernamePasswordContext();
        Assert.assertNull(ctx.getUsername());

        ctx.setUsername("bob");
        Assert.assertEquals(ctx.getUsername(), "bob");

        ctx.setUsername("foo");
        Assert.assertEquals(ctx.getUsername(), "foo");

        ctx.setUsername("");
        Assert.assertEquals(ctx.getUsername(), "");

        ctx.setUsername(null);
        Assert.assertNull(ctx.getUsername());
    }

    /** Tests mutating the password. */
    @Test public void testPassword() {
        UsernamePasswordContext ctx = new UsernamePasswordContext();
        Assert.assertNull(ctx.getPassword());

        ctx.setPassword("bob");
        Assert.assertEquals(ctx.getPassword(), "bob");

        ctx.setPassword("foo");
        Assert.assertEquals(ctx.getPassword(), "foo");

        ctx.setPassword("");
        Assert.assertEquals(ctx.getPassword(), "");

        ctx.setPassword(null);
        Assert.assertNull(ctx.getPassword());
    }
}