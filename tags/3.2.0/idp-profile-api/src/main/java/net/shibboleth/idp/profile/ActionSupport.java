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

package net.shibboleth.idp.profile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.execution.Event;

/** Helper class for {@link org.springframework.webflow.execution.Action} operations. */
public final class ActionSupport {

    /** Constructor. */
    private ActionSupport() {
        
    }

    /**
     * Signals a successful outcome by an action.
     * 
     * @param source the source of the event
     * 
     * @return the proceed event (which may be null, based on implementation details)
     */
    @Nullable public static Event buildProceedEvent(@Nonnull final Object source) {
        return null;
    }

    /**
     * Builds an event with a given ID but no related attributes.
     * 
     * @param source the source of the event
     * @param eventId the ID of the event
     * 
     * @return the constructed event
     */
    @Nonnull public static Event buildEvent(@Nonnull final Object source,
            @Nonnull final String eventId) {
        
        return buildEvent(source, eventId, null);
    }

    /**
     * Builds an event, to be returned by the given component.
     * 
     * @param source IdP component that will return the constructed event
     * @param eventId ID of the event
     * @param eventAttributes attributes associated with the event
     * 
     * @return the constructed {@link Event}
     */
    @Nonnull public static Event buildEvent(@Nonnull final Object source, @Nonnull final String eventId,
            @Nonnull final AttributeMap eventAttributes) {
        Constraint.isNotNull(source, "Component cannot be null");

        final String trimmedEventId =
                Constraint.isNotNull(StringSupport.trimOrNull(eventId), "ID of event cannot be null or empty");

        if (eventAttributes == null || eventAttributes.isEmpty()) {
            return new Event(source, trimmedEventId);
        } else {
            return new Event(source, trimmedEventId, eventAttributes);
        }
    }
    
}