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

package net.shibboleth.idp.profile.interceptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link AbstractProfileInterceptorResult} unit test. */
public class AbstractProfileInterceptorResultTest {

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testEmptyContext() {
        new MockAbstractProfileInterceptorResult("", "key", "value", new Long(100));
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testEmptyKey() {
        new MockAbstractProfileInterceptorResult("context", "", "value", new Long(100));
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testEmptyValue() {
        new MockAbstractProfileInterceptorResult("context", "key", "", new Long(100));
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testNullContext() {
        new MockAbstractProfileInterceptorResult(null, "key", "value", new Long(100));
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testNullKey() {
        new MockAbstractProfileInterceptorResult("context", null, "value", new Long(100));
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testNullValue() {
        new MockAbstractProfileInterceptorResult("context", "key", null, new Long(100));
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testNegativeExpiration() {
        new MockAbstractProfileInterceptorResult("context", "key", null, new Long(-100));
    }

    @Test public void testNullExpiration() {
        final MockAbstractProfileInterceptorResult result =
                new MockAbstractProfileInterceptorResult("context", "key", "value", null);
        Assert.assertEquals(result.getStorageContext(), "context");
        Assert.assertEquals(result.getStorageKey(), "key");
        Assert.assertEquals(result.getStorageValue(), "value");
        Assert.assertEquals(result.getStorageExpiration(), null);
    }

    @Test(expectedExceptions = ConstraintViolationException.class) public void testZeroExpiration() {
        new MockAbstractProfileInterceptorResult("context", "key", null, new Long(0));
    }

    @Test public void testResult() {
        final MockAbstractProfileInterceptorResult result =
                new MockAbstractProfileInterceptorResult("context", "key", "value", new Long(100));
        Assert.assertEquals(result.getStorageContext(), "context");
        Assert.assertEquals(result.getStorageKey(), "key");
        Assert.assertEquals(result.getStorageValue(), "value");
        Assert.assertEquals(result.getStorageExpiration(), new Long(100));
    }

    private class MockAbstractProfileInterceptorResult extends AbstractProfileInterceptorResult {

        public MockAbstractProfileInterceptorResult(@Nonnull @NotEmpty final String context,
                @Nonnull @NotEmpty final String key, @Nonnull @NotEmpty final String value,
                @Nullable @Positive @Duration final Long expiration) {
            super(context, key, value, expiration);
        }
    }
}
