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

package net.shibboleth.idp.attribute.filter.spring.impl;

import java.util.Collection;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.AttributeFilter;
import net.shibboleth.idp.attribute.filter.AttributeFilterPolicy;
import net.shibboleth.idp.attribute.filter.impl.AttributeFilterImpl;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.service.ServiceException;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Function;

/**
 * Strategy for summoning up an {@link AttributeFilterImpl} from a populated {@link ApplicationContext}. We do this by
 * finding all the configured {@link AttributeFilterPolicy} beans and bunging them into the Attribute Filter which we
 * then initialize.
 */
public class AttributeFilterServiceStrategy extends AbstractIdentifiableInitializableComponent implements
        Function<ApplicationContext, ServiceableComponent<AttributeFilter>> {

    /** log. */
    private final Logger log = LoggerFactory.getLogger(AttributeFilterServiceStrategy.class);

    /** {@inheritDoc} */
    @Override @Nullable public ServiceableComponent<AttributeFilter>
            apply(@Nullable final ApplicationContext appContext) {

        final Collection<AttributeFilterPolicy> afps = appContext.getBeansOfType(AttributeFilterPolicy.class).values();
        log.debug("Creating Attribute Filter {} with  {} Policies", getId(), afps.size());

        final AttributeFilterImpl filter = new AttributeFilterImpl(getId(), afps);
        filter.setApplicationContext(appContext);

        try {
            filter.initialize();
        } catch (ComponentInitializationException e) {
            throw new ServiceException("Unable to initialize attribute filter for " + appContext.getDisplayName(), e);
        }
        return filter;
    }
}