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

package net.shibboleth.idp.authn.context;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

/**
 * A {@link BaseContext} that holds information about an authentication request's
 * requirement for a specific custom {@link Principal}.
 * 
 * <p>Authentication protocols with features for requesting specific forms of
 * authentication will populate this context type, typically as a child of the
 * {@link AuthenticationContext}, with an expression of those requirements in the
 * form of a protocol-specific operator string and an ordered list of custom
 * {@link Principal} objects.</p>
 * 
 * <p>During the authentication process, interactions with
 * {@link net.shibboleth.idp.authn.principal.PrincipalSupportingComponent}-supporting objects
 * will depend on them satisfying context requirements, via the use of registered
 * {@link net.shibboleth.idp.authn.principal.PrincipalEvalPredicateFactory} objects.</p>
 * 
 * <p>Upon successful authentication the most appropriate "matching" {@link Principal} will be
 * saved back to this context for use in generating a protocol response.</p>
 */
public class RequestedPrincipalContext extends BaseContext {

    /** Comparison operator specific to request protocol. */
    @Nullable private String operatorString;

    /** The principals reflecting the request requirements. */
    @Nonnull @NonnullElements private List<Principal> requestedPrincipals;
    
    /** The principal that satisfied the request, if any. */
    @Nullable private Principal matchingPrincipal;
    
    /** Constructor. */
    public RequestedPrincipalContext() {
        requestedPrincipals = Collections.emptyList();
    }

    /**
     * Get the comparison operator for matching requested principals. 
     * 
     * @return comparison operator
     */
    @Nonnull @NotEmpty public String getOperator() {
        return operatorString;
    }
    
    /**
     * Set the comparison operator for matching requested principals.
     * 
     * @param operator comparison operator
     */
    public void setOperator(@Nullable final String operator) {
        operatorString = StringSupport.trimOrNull(operator);
    }

    /**
     * Get an immutable list of principals reflecting the request requirements.
     * 
     * @return  immutable list of principals 
     */
    @Nonnull @NonnullElements @Unmodifiable @NotLive public List<Principal> getRequestedPrincipals() {
        return ImmutableList.copyOf(requestedPrincipals);
    }
    
    /**
     * Set list of principals reflecting the request requirements.
     * 
     * @param principals list of principals
     */
    public void setRequestedPrincipals(@Nonnull @NonnullElements final List<Principal> principals) {
        Constraint.isNotNull(principals, "Principal list cannot be null");
        
        requestedPrincipals = new ArrayList<>(Collections2.filter(principals, Predicates.notNull()));
    }
    
    /**
     * Get the principal that matched the request's requirements, if any.
     * 
     * @return  a matching principal, or null
     */
    @Nullable public Principal getMatchingPrincipal() {
        return matchingPrincipal;
    }
    
    /**
     * Set the principal that matched the request's requirements, if any.
     * 
     * @param principal a matching principal, or null
     */
    public void setMatchingPrincipal(@Nullable final Principal principal) {
       matchingPrincipal = principal;
    }
    
}