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

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.Prototype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Post-processes bean configuration metadata to ensure that stateful beans are scoped properly.
 * 
 * This post-processor will override the scope of any bean whose class or superclass is annotated with {@link Prototype}
 * and whose scope is not {@link BeanDefinition#SCOPE_PROTOTYPE} by setting the bean's scope to
 * {@link BeanDefinition#SCOPE_PROTOTYPE}.
 */
// TODO Implement Ordered ?
public class ProfileActionBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ProfileActionBeanFactoryPostProcessor.class);

    /** {@inheritDoc} */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        for (final String beanName : beanFactory.getBeanNamesForAnnotation(Prototype.class)) {
            final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            if (!beanDefinition.isPrototype()) {
                log.warn("Profile action '{}' is not '{}' scope but must be, please check your configuration.",
                        beanName, BeanDefinition.SCOPE_PROTOTYPE);
                beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            }
        }
    }
}
