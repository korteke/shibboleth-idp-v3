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

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.action.ProfileAction;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.webflow.execution.Action;

/**
 * Post-processes {@link ProfileAction} beans by wrapping them in a Spring Web Flow adaptor.
 */
public class ProfileActionBeanPostProcessor implements BeanPostProcessor {

    /** {@inheritDoc} */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /** {@inheritDoc} */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof ProfileAction && !(bean instanceof Action)) {
            final WebFlowProfileActionAdaptor wrapper = new WebFlowProfileActionAdaptor((ProfileAction) bean);
            try {
                wrapper.initialize();
            } catch (ComponentInitializationException e) {
                throw new BeanCreationException("WebFlowProfileActionAdaptor failed to initialize around ProfileAction "
                        + beanName, e);
            }
            return wrapper;
        } else {
            return bean;
        }
    }
}