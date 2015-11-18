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

package net.shibboleth.idp.attribute.resolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DataConnector}. This test does not test any methods inherited from
 * {@link ResolverPlugin}, those are covered in {@link AbstractResolverPluginTest}.
 */
public class AbstractDataConnectorTest {

    /** Test instantiation and post-instantiation state. */
    @Test public void instantiation() {
        MockBaseDataConnector connector = new MockBaseDataConnector("foo", Collections.EMPTY_MAP);

        Assert.assertNull(connector.getFailoverDataConnectorId());
    }

    /** Test getting/setting dependency ID. */
    @Test public void failoverDependencyId() {
        MockBaseDataConnector connector = new MockBaseDataConnector("foo", Collections.EMPTY_MAP);

        connector.setFailoverDataConnectorId(" foo ");
        Assert.assertEquals(connector.getFailoverDataConnectorId(), "foo");

        connector.setFailoverDataConnectorId("");
        Assert.assertNull(connector.getFailoverDataConnectorId());

        connector.setFailoverDataConnectorId(null);
        Assert.assertNull(connector.getFailoverDataConnectorId());
    }

    /** Test the resolution of the data connector. */
    @Test public void resolve() throws Exception {
        AttributeResolutionContext context = new AttributeResolutionContext();
        context.getSubcontext(AttributeResolverWorkContext.class, true);

        MockBaseDataConnector connector = new MockBaseDataConnector("foo", (Map<String, IdPAttribute>) null);
        connector.initialize();
        Assert.assertNull(connector.resolve(context));

        HashMap<String, IdPAttribute> values = new HashMap<>();
        connector = new MockBaseDataConnector("foo", values);
        connector.initialize();
        Assert.assertNotNull(connector.resolve(context));

        IdPAttribute attribute = new IdPAttribute("foo");
        values.put(attribute.getId(), attribute);

        connector = new MockBaseDataConnector("foo", values);
        connector.initialize();
        Map<String, IdPAttribute> result = connector.resolve(context);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsKey(attribute.getId()));
        Assert.assertEquals(result.get(attribute.getId()), attribute);
    }

    /**
     * This class implements the minimal level of functionality and is meant only as a means of testing the abstract
     * {@link DataConnector}.
     */
    private static final class MockBaseDataConnector extends AbstractDataConnector {

        /** Static values returned for {@link #resolve(AttributeResolutionContext)}. */
        private Map<String, IdPAttribute> staticValues;

        /**
         * Constructor.
         * 
         * @param id id of the data connector
         * @param values values returned for {@link #resolve(AttributeResolutionContext)}
         */
        public MockBaseDataConnector(final String id, final Map<String, IdPAttribute> values) {
            setId(id);
            staticValues = values;
        }

        /** {@inheritDoc} */
        @Override
        @Nullable protected Map<String, IdPAttribute> doDataConnectorResolve(
                @Nonnull final AttributeResolutionContext resolutionContext,
                @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
            return staticValues;
        }
    }
}