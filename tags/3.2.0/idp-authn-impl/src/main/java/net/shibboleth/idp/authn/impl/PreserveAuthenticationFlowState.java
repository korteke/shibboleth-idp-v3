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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.AbstractAuthenticationAction;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action that extracts configured parameters from a servlet request and populates
 * {@link AuthenticationContext#getAuthenticationStateMap()} with the data.
 * 
 * <p>Multiple copies of a parameter result in a list of strings being stored in the map.</p>
 * 
 * <p>Any existing state in the map is cleared.</p>
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @post The AuthenticationContext is modified as above.
 */
public class PreserveAuthenticationFlowState extends AbstractAuthenticationAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PopulateAuthenticationContext.class);
    
    /** Parameter names to look for. */
    @Nonnull @NonnullElements private Collection<String> parameterNames;
    
    /** Constructor. */
    PreserveAuthenticationFlowState() {
        parameterNames = Collections.emptyList();
    }
    
    /**
     * Set the parameter names to look for.
     * 
     * @param names parameter names
     */
    public void setParameterNames(@Nullable @NonnullElements final Collection<String> names) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        if (names == null) {
            parameterNames = Collections.emptyList();
        } else {
            parameterNames = new ArrayList<>(StringSupport.normalizeStringCollection(names));
        }
    }
    

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        if (!super.doPreExecute(profileRequestContext, authenticationContext)) {
            return false;
        } else if (getHttpServletRequest() == null) {
            log.debug("No HttpServletRequest available");
            return false;
        }
        
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        final Map<String,Object> state = authenticationContext.getAuthenticationStateMap();
        state.clear();

        final Map<String,String[]> params = getHttpServletRequest().getParameterMap();
        for (final String name : parameterNames) {
            final String[] values = params.get(name);
            if (values != null) {
                if (values.length == 0) {
                    state.put(name, null);
                } else if (values.length == 1) {
                    state.put(name, values[0]);
                } else {
                    state.put(name, Arrays.asList(values));
                }
            }
        }
    }
    
}