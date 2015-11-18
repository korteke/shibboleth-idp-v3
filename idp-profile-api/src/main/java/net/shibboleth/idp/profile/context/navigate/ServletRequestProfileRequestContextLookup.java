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

package net.shibboleth.idp.profile.context.navigate;

import com.google.common.base.Function;
import org.opensaml.profile.context.ProfileRequestContext;

import javax.annotation.Nullable;
import javax.servlet.ServletRequest;

/**
 * Looks up the profile request context from a servlet request attribute.
 *
 * @author Marvin S. Addison
 */
public class ServletRequestProfileRequestContextLookup implements Function<ServletRequest, ProfileRequestContext> {

    @Nullable
    @Override
    public ProfileRequestContext apply(final ServletRequest input) {
        return (ProfileRequestContext) input.getAttribute(ProfileRequestContext.BINDING_KEY);
    }
}
