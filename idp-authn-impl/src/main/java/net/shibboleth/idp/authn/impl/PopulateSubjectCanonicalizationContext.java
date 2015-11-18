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

package net.shibboleth.idp.authn.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import net.shibboleth.idp.authn.AbstractSubjectCanonicalizationAction;
import net.shibboleth.idp.authn.SubjectCanonicalizationFlowDescriptor;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * An action that populates a {@link SubjectCanonicalizationContext} with the
 * {@link SubjectCanonicalizationFlowDescriptor} objects configured into the IdP.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @pre <pre>ProfileRequestContext.getSubcontext(SubjectCanonicalizationContext.class, false) != null</pre>
 * @post The SubjectCanonicalizationContext is modified as above.
 */
public class PopulateSubjectCanonicalizationContext extends AbstractSubjectCanonicalizationAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PopulateSubjectCanonicalizationContext.class);
    
    /** The flows to make available for possible use. */
    @Nonnull @NonnullElements private Collection<SubjectCanonicalizationFlowDescriptor> availableFlows;

    /** Constructor. */
    PopulateSubjectCanonicalizationContext() {
        availableFlows = Collections.emptyList();
    }
    
    /**
     * Set the flows available for possible use.
     * 
     * @param flows the flows available for possible use
     */
    public void setAvailableFlows(
            @Nonnull @NonnullElements final Collection<SubjectCanonicalizationFlowDescriptor> flows) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(flows, "Flow collection cannot be null");
        
        availableFlows = new ArrayList<>(Collections2.filter(flows, Predicates.notNull()));
    }
        
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final SubjectCanonicalizationContext c14nContext) {

        log.debug("{} Installing {} canonicalization flows into SubjectCanonicalizationContext", getLogPrefix(),
                availableFlows.size());
        for (final SubjectCanonicalizationFlowDescriptor desc : availableFlows) {
            c14nContext.getPotentialFlows().put(desc.getId(), desc);
        }
    }
    
}