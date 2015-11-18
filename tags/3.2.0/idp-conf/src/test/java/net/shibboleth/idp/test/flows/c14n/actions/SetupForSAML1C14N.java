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

package net.shibboleth.idp.test.flows.c14n.actions;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.authn.principal.NameIdentifierPrincipal;
import net.shibboleth.idp.saml.nameid.SAML1NameIdentifierAttributeEncoder;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml1.core.NameIdentifier;

/**
 * Action to mock up a Subject for C14N.
 */
public class SetupForSAML1C14N extends AbstractProfileAction {
    
    private String attributeName;

    /**
     * @return Returns the attributeName.
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @param attributeName The attributeName to set.
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        final RelyingPartyContext rpc = profileRequestContext.getSubcontext(RelyingPartyContext.class);

        NameIdentifier nid = null;
        final AttributeContext ac = rpc.getSubcontext(AttributeContext.class, false);
        for (final AttributeEncoder enc : ac.getIdPAttributes().get(getAttributeName()).getEncoders()) {
            if (enc instanceof SAML1NameIdentifierAttributeEncoder
                    && enc.getActivationCondition().apply(profileRequestContext)) {
                try {
                    nid = ((SAML1NameIdentifierAttributeEncoder) enc).encode(ac.getIdPAttributes().get(getAttributeName()));
                } catch (AttributeEncodingException e) {
                    ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
                    return;
                }
            }
        }
        
        if (nid == null) {
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return;
        }

        final NameIdentifierPrincipal nidp = new NameIdentifierPrincipal(nid);
        final Subject sub = new Subject();
        sub.getPrincipals().add(nidp);

        final SubjectCanonicalizationContext scc =
                profileRequestContext.getSubcontext(SubjectCanonicalizationContext.class, true);
        scc.setSubject(sub);
        scc.setRequesterId(rpc.getRelyingPartyId());
        scc.setResponderId(rpc.getConfiguration().getResponderId());
    }
}
