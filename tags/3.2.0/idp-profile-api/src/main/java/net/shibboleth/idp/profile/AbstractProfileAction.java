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

import java.text.MessageFormat;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.profile.context.SpringRequestContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.action.AbstractConditionalProfileAction;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.action.ProfileAction;
import org.opensaml.profile.context.EventContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.google.common.base.Function;

//TODO perf metrics

/**
 * Base class for Spring-aware profile actions.
 * 
 * This base class takes care of the following:
 * <ul>
 * <li>retrieving the {@link ProfileRequestContext} from the current request environment</li>
 * <li>ensuring the {@link javax.servlet.http.HttpServletRequest} and {@link javax.servlet.http.HttpServletResponse} are
 * available on the {@link ProfileRequestContext}, if they exist</li>
 * <li>tracking performance metrics for the action</li>
 * </ul>
 * 
 * Action implementations should override {@link #doExecute(RequestContext, ProfileRequestContext)}.
 * 
 * @param <InboundMessageType> type of in-bound message
 * @param <OutboundMessageType> type of out-bound message
 */
@ThreadSafe
public abstract class AbstractProfileAction<InboundMessageType, OutboundMessageType>
        extends AbstractConditionalProfileAction<InboundMessageType, OutboundMessageType>
        implements Action, MessageSource, MessageSourceAware {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractProfileAction.class);

    /** Strategy used to lookup the {@link ProfileRequestContext} from a given WebFlow {@link RequestContext}. */
    @Nonnull private Function<RequestContext, ProfileRequestContext> profileContextLookupStrategy;

    /** MessageSource injected by Spring, typically the parent ApplicationContext itself. */
    @Nonnull private MessageSource messageSource;
    
    /**
     * Constructor.
     * 
     * Initializes the ID of this action to the class name. Initializes {@link #profileContextLookupStrategy} to
     * {@link WebflowRequestContextProfileRequestContextLookup}.
     */
    public AbstractProfileAction() {
        profileContextLookupStrategy = new WebflowRequestContextProfileRequestContextLookup();
    }

    /**
     * Get the strategy used to lookup the {@link ProfileRequestContext} from a given WebFlow {@link RequestContext}.
     * 
     * @return lookup strategy
     */
    @Nonnull public Function<RequestContext, ProfileRequestContext> getProfileContextLookupStrategy() {
        return profileContextLookupStrategy;
    }

    /**
     * Set the strategy used to lookup the {@link ProfileRequestContext} from a given WebFlow {@link RequestContext}.
     * 
     * @param strategy lookup strategy
     */
    public void setProfileContextLookupStrategy(
            @Nonnull final Function<RequestContext,ProfileRequestContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        profileContextLookupStrategy =
                Constraint.isNotNull(strategy, "ProfileRequestContext lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public Event execute(@Nonnull final RequestContext springRequestContext) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext =
                profileContextLookupStrategy.apply(springRequestContext);
        if (profileRequestContext == null) {
            log.error("{} IdP profile request context is not available", getLogPrefix());
            return ActionSupport.buildEvent(this, EventIds.INVALID_PROFILE_CTX);
        }

        return doExecute(springRequestContext, profileRequestContext);
    }

    /**
     * Spring-aware actions can override this method to fully control the execution of an Action
     * by the Web Flow engine.
     * 
     * <p>Alternatively they may override {@link #doExecute(ProfileRequestContext)} and access
     * Spring information via a {@link SpringRequestContext} attached to the profile request context.</p>
     * 
     * <p>The default implementation attaches the Spring Web Flow request context to the profile
     * request context tree to "narrow" the execution signature to the basic OpenSAML {@link ProfileAction}
     * interface. After execution, an {@link EventContext} is sought, and used to return a result back to
     * the Web Flow engine. If no context exists, a "proceed" event is signaled.</p>
     * 
     * @param springRequestContext the Spring request context
     * @param profileRequestContext a profile request context
     * @return a Web Flow event produced by the action
     */
    @Nonnull protected Event doExecute(@Nonnull final RequestContext springRequestContext,
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext) {
        
        // Attach the Spring context to the context tree.
        SpringRequestContext springSubcontext =
                profileRequestContext.getSubcontext(SpringRequestContext.class, true);      
        springSubcontext.setRequestContext(springRequestContext);

        try {
            execute(profileRequestContext);
        } finally {     
            // Remove the Spring context from the context tree.     
            profileRequestContext.removeSubcontext(springSubcontext);
        }
        
        return getResult(this, profileRequestContext);
    }
    
    /**
     * Examines the profile context for an event to return, or signals a successful outcome if
     * no {@link EventContext} is located; the EventContext will be removed upon completion.
     * 
     * <p>The EventContext must contain a Spring Web Flow {@link Event} or a {@link String}.
     * Any other type of context data will be ignored.</p>
     * 
     * @param action    the action signaling the event
     * @param profileRequestContext the profile request context to examine
     * @return  an event based on the profile request context, or "proceed"
     */
    @Nonnull protected Event getResult(@Nonnull final ProfileAction action,
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext) {
        
        // Check for an EventContext on output.
        final EventContext eventCtx = profileRequestContext.getSubcontext(EventContext.class, false);
        if (eventCtx != null) {
            final Object event = eventCtx.getEvent();
            
            if (event instanceof Event) {
                return (Event) eventCtx.getEvent();
            } else if (event instanceof String) {
                return ActionSupport.buildEvent(action, (String) eventCtx.getEvent());
            } else if (event instanceof AttributeMap) {
                final AttributeMap map = (AttributeMap) eventCtx.getEvent();
                return ActionSupport.buildEvent(action, map.getString("eventId", EventIds.PROCEED_EVENT_ID), map); 
            }
        }
        
        // A null value can be used to implicitly continue evaluating an action-state until the last step.
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setMessageSource(MessageSource source) {
        messageSource = source;
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        if (messageSource != null) {
            return messageSource.getMessage(code, args, defaultMessage, locale);
        }
        return MessageFormat.format(defaultMessage, args);
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage(String code, Object[] args, Locale locale) {
        if (messageSource != null) {
            return messageSource.getMessage(code, args, locale);
        }
        throw new NoSuchMessageException("MessageSource was not set");
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) {
        if (messageSource != null) {
            return messageSource.getMessage(resolvable, locale);
        }
        throw new NoSuchMessageException("MessageSource was not set");
    }

    /**
     * Gets the Spring {@link RequestContext} from a {@link SpringRequestContext} stored in the context tree.
     *
     * @param profileRequestContext Profile request context.
     *
     * @return Spring request context.
     */
    @Nullable protected RequestContext getRequestContext(final ProfileRequestContext profileRequestContext) {
        final SpringRequestContext springRequestCtx = profileRequestContext.getSubcontext(SpringRequestContext.class);
        if (springRequestCtx == null) {
            return null;
        }
        return springRequestCtx.getRequestContext();
    }
}