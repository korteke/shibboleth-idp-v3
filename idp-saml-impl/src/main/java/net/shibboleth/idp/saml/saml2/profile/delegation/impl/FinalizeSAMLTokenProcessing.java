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

package net.shibboleth.idp.saml.saml2.profile.delegation.impl;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.utilities.java.support.annotation.Prototype;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Post-process the results of {@link org.opensaml.saml.saml2.core.Assertion} token subject canonicalization.
 * 
 * @event {@link AuthnEventIds#INVALID_SUBJECT_C14N_CTX}
 * @pre <pre>profileRequestContext.getSubcontext(SubjectCanonicalizationContext.class).getPrincipalName() != null</pre>
 * @post <pre>profileRequestContext.getSubcontext(SubjectCanonicalizationContext.class) == null</pre>
 * @post <pre>profileRequestContext.getSubcontext(SubjectContext.class).getPrincipalName() != null</pre>
 */
@Prototype
public class FinalizeSAMLTokenProcessing extends AbstractProfileAction {
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(FinalizeSAMLTokenProcessing.class);
    
    /** The canonicalized principal name. */
    private String principalName;

    /** {@inheritDoc} */
    protected boolean doPreExecute(ProfileRequestContext profileRequestContext) {
        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        SubjectCanonicalizationContext c14nContext = 
                profileRequestContext.getSubcontext(SubjectCanonicalizationContext.class);
        
        if (c14nContext == null) {
            log.warn("{} SubjectCanonicalizationContext was missing", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT_C14N_CTX);
            return false;
        }
        
        principalName = c14nContext.getPrincipalName();
        if (principalName == null) {
            log.warn("{} SubjectCanonicalizationContext principal name was null", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT_C14N_CTX);
            return false;
        }
        
        log.debug("{} Subject c14n from inbound delegated Assertion token produced principal name: {}",
                principalName, getLogPrefix());
        
        return true;
    }
    
    /** {@inheritDoc} */
    protected void doExecute(ProfileRequestContext profileRequestContext) {
        profileRequestContext.removeSubcontext(SubjectCanonicalizationContext.class);
        
        SubjectContext subjectContext = profileRequestContext.getSubcontext(SubjectContext.class, true);
        subjectContext.setPrincipalName(principalName);
    }

}
