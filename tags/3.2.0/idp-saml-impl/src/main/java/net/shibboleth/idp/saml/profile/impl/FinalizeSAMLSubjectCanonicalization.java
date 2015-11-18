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

package net.shibboleth.idp.saml.profile.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.AbstractProfileAction;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An action that runs after a completed canonicalization of a SAML Subject and transfers
 * information into a {@link SubjectContext} child of the {@link ProfileRequestContext}.
 * 
 * <p>The context is populated based on the presence of a canonical principal name in
 * a {@link SubjectCanonicalizationContext}. Any {@link SubjectCanonicalizationContext}
 * found will be removed.</p>
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link AuthnEventIds#INVALID_SUBJECT_C14N_CTX}
 * 
 * @post <pre>ProfileRequestContext.getSubcontext(SubjectCanonicalizationContext.class) == null</pre>
 */
public class FinalizeSAMLSubjectCanonicalization extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(FinalizeSAMLSubjectCanonicalization.class);
    
    /** The principal name extracted from the context tree. */
    @Nullable private String canonicalPrincipalName;
    
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        final SubjectCanonicalizationContext c14nCtx =
                profileRequestContext.getSubcontext(SubjectCanonicalizationContext.class);
        if (c14nCtx == null) {
            log.debug("{} No SubjectCanonicalizationContext available", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT_C14N_CTX);
            return false;
        }
        
        canonicalPrincipalName = c14nCtx.getPrincipalName();
        profileRequestContext.removeSubcontext(c14nCtx);
        if (canonicalPrincipalName == null) {
            log.debug("{} No principal name in SubjectCanonicalizationContext", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT_C14N_CTX);
            return false;
        }
        
        return super.doPreExecute(profileRequestContext);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        final SubjectContext subjectCtx = new SubjectContext();
        subjectCtx.setPrincipalName(canonicalPrincipalName);
        profileRequestContext.addSubcontext(subjectCtx, true);
    }

}