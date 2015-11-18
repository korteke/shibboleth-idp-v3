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

package net.shibboleth.idp.attribute.filter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;

/** Interface that filters out attributes and values based upon loaded policies. */
@ThreadSafe
public interface AttributeFilter extends IdentifiedComponent {

    /**
     * Gets the immutable collection of filter policies.
     * 
     * @return immutable collection of filter policies
     */
    @Nonnull @NonnullElements @Unmodifiable List<AttributeFilterPolicy> getFilterPolicies();

    /**
     * Filters attributes and values. This filtering process may remove attributes and values but must never add them.
     * 
     * @param filterContext context containing the attributes to be filtered and collecting the results of the filtering
     *            process
     * 
     * @throws AttributeFilterException thrown if there is a problem retrieving or applying the attribute filter policy
     */
    void filterAttributes(@Nonnull final AttributeFilterContext filterContext) throws AttributeFilterException;
    
}