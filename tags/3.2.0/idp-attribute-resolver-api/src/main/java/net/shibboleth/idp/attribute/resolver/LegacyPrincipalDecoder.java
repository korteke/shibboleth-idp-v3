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

package net.shibboleth.idp.attribute.resolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;

/**
 * Definition of the legacy Principal Connectors. <br/>
 * This is the component which takes a context and produces the unique principal.
 * 
 * Code will only be implemented by the legacy parsing of the &lt;PrinicipalConnector&gt; statements and will consume
 * CanonicalizationContexts.
 * 
 */
public interface LegacyPrincipalDecoder {

    /**
     * Resolve the principal with respect to the provided context. This is expected to strip out the
     * NameID or NameIdentifier and match it against the connector definitions configured.
     * 
     * @param context what to look at.
     * @return the IdP principal, or null if no definitions were applicable
     * @throws ResolutionException if we recognise the definition but could not decode it (data out of date and so
     *             forth)
     */
    @Nullable String canonicalize(@Nonnull final  SubjectCanonicalizationContext context) throws ResolutionException;
    
    /** 
     * Report on whether this decoder has any configured connectors.
     * @return whether there are any decoders.
     */

    boolean hasValidConnectors();
}
