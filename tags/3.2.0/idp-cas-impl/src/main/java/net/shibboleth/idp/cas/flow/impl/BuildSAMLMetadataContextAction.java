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
package net.shibboleth.idp.cas.flow.impl;

import javax.annotation.Nonnull;

import net.shibboleth.idp.cas.protocol.ProtocolError;
import net.shibboleth.idp.cas.service.impl.ServiceEntityDescriptor;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Builds a {@link SAMLMetadataContext} child of {@link RelyingPartyContext} to facilitate relying party selection
 * by group name. Possible outcomes:
 * <ul>
 *     <li><code>null</code> on success</li>
 *     <li>{@link ProtocolError#IllegalState IllegalState}</li>
 * </ul>
 *
 * @author Marvin S. Addison
 */
public class BuildSAMLMetadataContextAction extends AbstractCASProtocolAction {

    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {
        final RelyingPartyContext rpCtx = profileRequestContext.getSubcontext(RelyingPartyContext.class, false);
        if (rpCtx == null) {
            throw new IllegalStateException("RelyingPartyContext not found");
        }
        final SAMLMetadataContext mdCtx = new SAMLMetadataContext();
        mdCtx.setEntityDescriptor(new ServiceEntityDescriptor(getCASService(profileRequestContext)));
        rpCtx.setRelyingPartyIdContextTree(mdCtx);

        return null;
    }
}
