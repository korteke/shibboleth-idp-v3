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

package net.shibboleth.idp.test;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.mock.env.MockPropertySource;

/**
 * An {@link ApplicationContextInitializer} which prepends properties.
 * 
 * <ul>
 * <li>Sets idp.home = classpath:</li>
 * <li>Sets idp.webflows = classpath*:/flows</li>
 * <li>Sets idp.authn.flows = Password</li>
 * </ul>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TestEnvironmentApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(TestEnvironmentApplicationContextInitializer.class);

    /** {@inheritDoc} */
    @Override public void initialize(@Nonnull final ConfigurableApplicationContext applicationContext) {
        final MockPropertySource mock = new MockPropertySource();
        mock.setProperty("idp.home", "classpath:");
        mock.setProperty("idp.webflows", "classpath*:/flows");
        mock.setProperty("idp.authn.flows", "Password");
        applicationContext.getEnvironment().getPropertySources().addFirst(mock);
        log.info("Prepending properties '{}'", mock.getSource());
    }
}