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

package net.shibboleth.idp.profile.impl;

import java.util.Collections;

import javax.annotation.Nullable;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.config.SecurityConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.relyingparty.RelyingPartyConfiguration;
import net.shibboleth.idp.relyingparty.RelyingPartyConfigurationResolver;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiedInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link SelectRelyingPartyConfiguration} unit test. */
public class SelectRelyingPartyConfigurationTest {

    @Test(expectedExceptions = ComponentInitializationException.class) public void testNoResolver()
            throws ComponentInitializationException {
        final SelectRelyingPartyConfiguration action = new SelectRelyingPartyConfiguration();
        action.initialize();
    }

    /** Test that the action errors out properly if there is no relying party context. */
    @Test public void testNoRelyingPartyContext() throws Exception {
        final RequestContext src = new RequestContextBuilder().buildRequestContext();
        final ProfileRequestContext prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        prc.removeSubcontext(RelyingPartyContext.class);

        final RelyingPartyConfigurationResolver resolver = new MockResolver(null, null);

        final SelectRelyingPartyConfiguration action = new SelectRelyingPartyConfiguration();
        action.setRelyingPartyConfigurationResolver(resolver);
        action.initialize();

        final Event event = action.execute(src);

        ActionTestingSupport.assertEvent(event, IdPEventIds.INVALID_RELYING_PARTY_CTX);
    }

    /** Test that the action errors out properly if there is no relying party configuration. */
    @Test public void testNoRelyingPartyConfiguration() throws Exception {
        final RequestContext src = new RequestContextBuilder().buildRequestContext();
        final ProfileRequestContext prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        prc.getSubcontext(RelyingPartyContext.class).setConfiguration(null);

        final RelyingPartyConfigurationResolver resolver = new MockResolver(null, null);

        final SelectRelyingPartyConfiguration action = new SelectRelyingPartyConfiguration();
        action.setRelyingPartyConfigurationResolver(resolver);
        action.initialize();

        final Event event = action.execute(src);

        ActionTestingSupport.assertEvent(event, IdPEventIds.INVALID_RELYING_PARTY_CONFIG);
    }

    /** Test that the action errors out properly if the relying party configuration can not be resolved. */
    @Test public void testUnableToResolveRelyingPartyConfiguration() throws Exception {
        final RequestContext src = new RequestContextBuilder().buildRequestContext();
        final ProfileRequestContext prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        prc.getSubcontext(RelyingPartyContext.class).setConfiguration(null);

        final RelyingPartyConfiguration config = new RelyingPartyConfiguration();
        config.setId("foo");
        config.setResponderId("http://idp.example.org");
        config.setDetailedErrors(true);
        config.initialize();

        final RelyingPartyConfigurationResolver resolver = new MockResolver(config, new ResolverException());

        final SelectRelyingPartyConfiguration action = new SelectRelyingPartyConfiguration();
        action.setRelyingPartyConfigurationResolver(resolver);
        action.initialize();

        final Event event = action.execute(src);

        ActionTestingSupport.assertEvent(event, IdPEventIds.INVALID_RELYING_PARTY_CONFIG);
    }

    /** Test that the action resolves the relying party and proceeds properly. */
    @Test public void testResolveRelyingPartyConfiguration() throws Exception {
        final RequestContext src = new RequestContextBuilder().buildRequestContext();
        final ProfileRequestContext prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        prc.getSubcontext(RelyingPartyContext.class).setConfiguration(null);

        final RelyingPartyConfiguration config = new RelyingPartyConfiguration();
        config.setId("foo");
        config.setResponderId("http://idp.example.org");
        config.setDetailedErrors(true);
        config.initialize();

        final RelyingPartyConfigurationResolver resolver = new MockResolver(config, null);

        final SelectRelyingPartyConfiguration action = new SelectRelyingPartyConfiguration();
        action.setRelyingPartyConfigurationResolver(resolver);
        action.initialize();

        final Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);

        final RelyingPartyConfiguration resolvedConfig =
                prc.getSubcontext(RelyingPartyContext.class).getConfiguration();
        Assert.assertEquals(resolvedConfig.getId(), config.getId());
        Assert.assertEquals(resolvedConfig.getResponderId(), config.getResponderId());
        Assert.assertEquals(resolvedConfig.getProfileConfigurations(), config.getProfileConfigurations());
    }

    /** A resolver that returns a relying party configuration or throws an exception. */
    private class MockResolver extends AbstractIdentifiedInitializableComponent implements
            RelyingPartyConfigurationResolver {

        /** The relying party configuration to be returned. */
        private RelyingPartyConfiguration configuration;

        /** Exception thrown by {@link #resolve(ProfileRequestContext)} and {@link #resolveSingle(ProfileRequestContext)} */
        private ResolverException exception;

        /**
         * Constructor.
         * 
         * @param relyingPartyConfiguration the relying party configuration to be returned
         * @param resolverException exception thrown by {@link #resolve(ProfileRequestContext)} and
         *            {@link #resolveSingle(ProfileRequestContext)}
         */
        public MockResolver(@Nullable final RelyingPartyConfiguration relyingPartyConfiguration,
                @Nullable final ResolverException resolverException) {
            configuration = relyingPartyConfiguration;
            exception = resolverException;
        }

        /** {@inheritDoc} */
        @Override public Iterable<RelyingPartyConfiguration> resolve(final ProfileRequestContext context)
                throws ResolverException {
            if (exception != null) {
                throw exception;
            }
            return Collections.singleton(configuration);
        }

        /** {@inheritDoc} */
        @Override public RelyingPartyConfiguration resolveSingle(final ProfileRequestContext context)
                throws ResolverException {
            if (exception != null) {
                throw exception;
            }
            return configuration;
        }

        /** {@inheritDoc} */
        @Override public SecurityConfiguration getDefaultSecurityConfiguration(String profileId) {
            return null;
        }
    }

}