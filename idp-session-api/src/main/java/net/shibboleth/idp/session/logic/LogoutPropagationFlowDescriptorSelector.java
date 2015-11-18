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

package net.shibboleth.idp.session.logic;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import net.shibboleth.idp.session.LogoutPropagationFlowDescriptor;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Selection function to retrieve the logout propagation flow descriptor that is suitable for a given {@link SPSession}.
 */
public class LogoutPropagationFlowDescriptorSelector implements Function<SPSession, LogoutPropagationFlowDescriptor> {

    /** List of available flows. */
    private final List<LogoutPropagationFlowDescriptor> availableFlows;

    /**
     * Constructor.
     *
     * @param flows the logout propagation flows to select from
     */
    public LogoutPropagationFlowDescriptorSelector(
            @Nonnull @NonnullElements final List<LogoutPropagationFlowDescriptor> flows) {
        Constraint.isNotNull(flows, "Flows cannot be null");

        availableFlows = new ArrayList<>(Collections2.filter(flows, Predicates.notNull()));
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public LogoutPropagationFlowDescriptor apply(@Nonnull final SPSession input) {
        for (LogoutPropagationFlowDescriptor flowDescriptor : availableFlows) {
            if (flowDescriptor.getSessionType().isInstance(input)) {
                return flowDescriptor;
            }
        }
        return null;
    }
    
}