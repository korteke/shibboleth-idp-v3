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

import net.shibboleth.ext.spring.resource.PreferFileSystemResourceLoader;
import net.shibboleth.idp.spring.IdPPropertiesApplicationContextInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * An {@link ApplicationContextInitializer} which configures a given {@link GenericApplicationContext} to use a
 * {@link PreferFileSystemResourceLoader}.
 * 
 * This initializer allows the {@link IdPPropertiesApplicationContextInitializer} to resolve "classpath*:" resources,
 * and consequently has the highest priority order so that it is called before the
 * {@link IdPPropertiesApplicationContextInitializer}.
 * 
 * This initializer performs the same function as the {@link PreferFileSystemContextLoader}, which unfortunately is
 * called after application context initializers.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PreferFileSystemApplicationContextInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PreferFileSystemApplicationContextInitializer.class);

    /** {@inheritDoc} */
    @Override public void initialize(@Nonnull final ConfigurableApplicationContext applicationContext) {
        if (applicationContext instanceof GenericApplicationContext) {
            log.debug("Initializing application context '{}'", applicationContext);
            ((GenericApplicationContext) applicationContext).setResourceLoader(new PreferFileSystemResourceLoader());
        }
    }

}