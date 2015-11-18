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
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;


/**
 * An action that populates a principal name obtained from a lookup function into a {@link SubjectContext}
 * child of the {@link ProfileRequestContext}.
 * 
 * <p>Used in specialized cases to directly populate a context to drive subsequent behavior for flows that
 * don't involve the full authentication machinery and the protections involved.</p> 
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 */
public class PopulateSubjectContext extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PopulateSubjectContext.class);
    
    /** Lookup strategy for principal name to populate. */
    @NonnullAfterInit private Function<ProfileRequestContext,String> principalNameLookupStrategy;
    
    /** The principal name extracted from the context tree. */
    @Nullable private String principalName;
    
    /**
     * Set lookup strategy for the principal name to use.
     * 
     * @param strategy  lookup strategy
     */
    public void setPrincipalNameLookupStrategy(@Nonnull final Function<ProfileRequestContext,String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        principalNameLookupStrategy = Constraint.isNotNull(strategy, "Principal name lookup strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (principalNameLookupStrategy == null) {
            throw new ComponentInitializationException("Principal name lookup strategy cannot be null");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        principalName = principalNameLookupStrategy.apply(profileRequestContext);
        if (principalName == null) {
            log.error("No principal name returned from lookup function");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        final SubjectContext subjectCtx = new SubjectContext();
        subjectCtx.setPrincipalName(principalName);
        profileRequestContext.addSubcontext(subjectCtx, true);
    }

}