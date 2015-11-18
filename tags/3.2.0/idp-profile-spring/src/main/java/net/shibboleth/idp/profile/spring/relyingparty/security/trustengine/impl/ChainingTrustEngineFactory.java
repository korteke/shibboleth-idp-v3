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

package net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.ext.spring.factory.AbstractComponentAwareFactoryBean;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.security.trust.TrustEngine;
import org.opensaml.security.trust.impl.ChainingTrustEngine;

/**
 * Factory bean for {@link ChainingTrustEngine}. This finesses the issue that some parsers are not supported
 * and return a bean of type object and these cannot be injected into the trust engine. This factory just filters the
 * unsupported engines out. A warning has been issued at point of parse so no further logging is required.
 */
public class ChainingTrustEngineFactory extends
        AbstractComponentAwareFactoryBean<ChainingTrustEngine> {

    /** The unfiltered list of putative trust engines. */
    private final List<Object> engines;

    /**
     * Constructor.
     * 
     * @param list the putative trust engines.
     */
    public ChainingTrustEngineFactory(@Nonnull final List<Object> list) {
        engines = Constraint.isNotNull(list, "Engine list must be non null");
    }

    /** {@inheritDoc} */
    @Override public Class<?> getObjectType() {
        return ChainingTrustEngine.class;
    }

    /** {@inheritDoc} */
    @Override protected ChainingTrustEngine doCreateInstance() throws Exception {
        final List<TrustEngine> list = new ArrayList<>(engines.size());

        for (Object engine : engines) {
            if (engine instanceof TrustEngine) {
                list.add((TrustEngine) engine);

            }
        }
        return new ChainingTrustEngine(list);
    }

}
