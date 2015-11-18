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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.collect.ImmutableList;

/**
 * A {@link BaseContext}, usually attached to {@link AuthenticationContext},
 * that holds information about authentication failures.
 *
 * <p>The login process is particularly prone to requiring detailed error
 * information to provide appropriate user feedback and auditing, and this
 * context tracks errors that occur and preserves detailed information about
 * the kind of errors encountered in multi-part authentication flows.
 */
public class AuthenticationErrorContext extends BaseContext {

    /** Ordered list of exceptions encountered. */
    @Nonnull @NonnullElements private List<Exception> exceptions;
    
    /** Error conditions detected through classified error messages. */
    private Collection<String> classifiedErrors;
    
    /** Constructor. */
    public AuthenticationErrorContext() {
        super();
        
        exceptions = new ArrayList<>();
        classifiedErrors = new HashSet<>();
    }

    /**
     * Get an immutable list of the exceptions encountered.
     * 
     * @return  immutable list of exceptions
     */
    @Nonnull @NonnullElements @Unmodifiable public List<Exception> getExceptions() {
        return ImmutableList.copyOf(exceptions);
    }
    
    /**
     * Add an exception to the list.
     * 
     * @param e exception to add
     */
    public void addException(@Nonnull final Exception e) {
        Constraint.isNotNull(e, "Exception cannot be null");
        
        exceptions.add(e);
    }
    
    /**
     * Get a mutable collection of error "tokens" associated with the context.
     * 
     * @return mutable collection of error strings
     */
    @Nonnull @NonnullElements @Live public Collection<String> getClassifiedErrors() {
        return classifiedErrors;
    }
    
    /**
     * Check for the presence of a particular error condition in the context.
     * 
     * @param error the condition to check for
     * @return  true iff the context contains the error condition specified
     */
    public boolean isClassifiedError(@Nonnull @NotEmpty final String error) {
        return classifiedErrors.contains(error);
    }
}
