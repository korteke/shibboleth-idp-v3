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

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import org.opensaml.messaging.context.BaseContext;

/**
 * A {@link BaseContext}, usually attached to {@link AuthenticationContext},
 * that holds information about authentication warnings.
 *
 * <p>The login process is particularly prone to requiring detailed warning
 * information to provide appropriate user feedback and auditing, and this
 * context tracks warnings that occur and preserves detailed information about
 * the kind of warnings encountered in multi-part authentication flows.
 */
public class AuthenticationWarningContext extends BaseContext {

    /** Warning conditions detected through classified warning messages. */
    private Collection<String> classifiedWarnings;
    
    /** Constructor. */
    public AuthenticationWarningContext() {
        super();
        
        classifiedWarnings = new HashSet<>();
    }

    /**
     * Get a mutable collection of warning "tokens" associated with the context.
     * 
     * @return mutable collection of warning strings
     */
    @Nonnull @NonnullElements @Live public Collection<String> getClassifiedWarnings() {
        return classifiedWarnings;
    }
    
    /**
     * Check for the presence of a particular warning condition in the context.
     * 
     * @param warning the condition to check for
     * @return  true if the context contains the warning condition specified
     */
    public boolean isClassifiedWarning(@Nonnull @NotEmpty final String warning) {
        return classifiedWarnings.contains(warning);
    }
}