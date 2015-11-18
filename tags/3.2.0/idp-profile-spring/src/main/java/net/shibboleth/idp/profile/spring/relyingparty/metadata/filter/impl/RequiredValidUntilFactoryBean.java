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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl;

import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;

import org.opensaml.saml.metadata.resolver.filter.impl.RequiredValidUntilFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Factory Bean to collect and specifically process (because of case IDP-646) parameters to a
 * {@link RequiredValidUntilFilter}.
 */
public class RequiredValidUntilFactoryBean extends AbstractFactoryBean<RequiredValidUntilFilter> {

    /** Where the (property replaced) value of maxValidityInterval goes. */
    @Nullable private String maxValidityIntervalDuration;

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(RequiredValidUntilFactoryBean.class);

    /**
     * setter for {@link #maxValidityIntervalDuration}.
     * 
     * @param s what to set.
     */
    public void setMaxValidityInterval(final String s) {
        maxValidityIntervalDuration = s;
    }

    /** {@inheritDoc} */
    @Override public Class<RequiredValidUntilFilter> getObjectType() {
        return RequiredValidUntilFilter.class;
    }

    /** {@inheritDoc} */
    @Override protected RequiredValidUntilFilter createInstance() throws Exception {
        final RequiredValidUntilFilter value = new RequiredValidUntilFilter();

        if (null != maxValidityIntervalDuration) {
            if (maxValidityIntervalDuration.startsWith("P")) {
                value.setMaxValidityInterval(DOMTypeSupport.durationToLong(maxValidityIntervalDuration));
            } else if (maxValidityIntervalDuration.startsWith("-P")) {
                throw new IllegalArgumentException("Negative durations are not supported");
            } else {
                // Treat as a Long and seconds.
                long durationInMs = 1000 * Long.valueOf(maxValidityIntervalDuration);
                log.warn("Numerical duration form is deprecated. The property 'maxValidityInterval'"
                        + " on RequiredValidUntil metadata filter should use the duration notation: {}",
                        DOMTypeSupport.longToDuration(durationInMs));
                value.setMaxValidityInterval(durationInMs);
            }
        }
        return value;
    }
}
