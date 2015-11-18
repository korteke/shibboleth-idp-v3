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

package net.shibboleth.idp.consent.context.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.base.MoreObjects;

/**
 * Context representing the state of a consent flow.
 * 
 * Holds consent previously given as well as obtained from user input.
 */
public class ConsentContext extends BaseContext {

    /** Map of previous consent read from storage and keyed by consent id. */
    @Nonnull @NonnullElements @Live private Map<String, Consent> previousConsents;

    /** Map of current consent extracted from user input and keyed by consent id. */
    @Nonnull @NonnullElements @Live private Map<String, Consent> currentConsents;

    /** Constructor. */
    public ConsentContext() {
        previousConsents = new LinkedHashMap<>();
        currentConsents = new LinkedHashMap<>();
    }

    /**
     * Get map of current consent extracted from user input and keyed by consent id.
     * 
     * @return map of current consent extracted from user input and keyed by consent id
     */
    @Nonnull @NonnullElements @Live public Map<String, Consent> getCurrentConsents() {
        return currentConsents;
    }

    /**
     * Get map of previous consent read from storage and keyed by consent id.
     * 
     * @return map of previous consent read from storage and keyed by consent id
     */
    @Nonnull @NonnullElements @Live public Map<String, Consent> getPreviousConsents() {
        return previousConsents;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("previousConsents", previousConsents)
                .add("chosenConsents", currentConsents)
                .toString();
    }

}
