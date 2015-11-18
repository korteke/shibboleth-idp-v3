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

package net.shibboleth.idp.authn;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;

import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.AuthenticationErrorContext;
import net.shibboleth.idp.authn.context.AuthenticationWarningContext;
import net.shibboleth.idp.authn.context.RequestedPrincipalContext;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.authn.principal.PrincipalEvalPredicate;
import net.shibboleth.idp.authn.principal.PrincipalEvalPredicateFactory;
import net.shibboleth.idp.authn.principal.PrincipalSupportingComponent;
import net.shibboleth.idp.profile.context.navigate.RelyingPartyIdLookupFunction;
import net.shibboleth.idp.profile.context.navigate.ResponderIdLookupFunction;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * A base class for authentication related actions that validate credentials and produce an
 * {@link AuthenticationResult}.
 * 
 * @param <InboundMessageType> type of in-bound message
 * @param <OutboundMessageType> type of out-bound message
 * 
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link AuthnEventIds#REQUEST_UNSUPPORTED}
 * @pre <pre>ProfileRequestContext.getSubcontext(AuthenticationContext.class).getAttemptedFlow() != null</pre>
 */
public abstract class AbstractValidationAction<InboundMessageType, OutboundMessageType>
        extends AbstractAuthenticationAction<InboundMessageType, OutboundMessageType>
            implements PrincipalSupportingComponent {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractValidationAction.class);
    
    /** Basis for {@link AuthenticationResult}. */
    @Nonnull private final Subject authenticatedSubject;
    
    /** Whether to inject the authentication flow's default custom principals into the subject. */
    private boolean addDefaultPrincipals;
    
    /** Indicates whether to clear any existing {@link AuthenticationErrorContext} before execution. */
    private boolean clearErrorContext;
    
    /** Error messages associated with a specific error condition token. */
    @Nonnull @NonnullElements private Map<String,Collection<String>> classifiedMessages;
    
    /** Predicate to apply when setting AuthenticationResult cacheability. */
    @Nullable private Predicate<ProfileRequestContext> resultCachingPredicate;

    /** Function used to obtain the requester ID. */
    @Nullable private Function<ProfileRequestContext,String> requesterLookupStrategy;

    /** Function used to obtain the responder ID. */
    @Nullable private Function<ProfileRequestContext,String> responderLookupStrategy;
    
    /** Constructor. */
    public AbstractValidationAction() {
        addDefaultPrincipals = true;
        authenticatedSubject = new Subject();
        clearErrorContext = true;
        classifiedMessages = Collections.emptyMap();
        requesterLookupStrategy = new RelyingPartyIdLookupFunction();
        responderLookupStrategy = new ResponderIdLookupFunction();
    }

    /**
     * Get whether to inject the authentication flow's default custom principals into the subject.
     * 
     * <p>This is the default behavior, and works for static flows in which the principal set can
     * be statically determined from the flow.</p>
     * 
     * @return whether to inject the authentication flow's default custom principals into the subject
     */
    public boolean addDefaultPrincipals() {
        return addDefaultPrincipals;
    }
    
    /**
     * Set whether to inject the authentication flow's default custom principals into the subject.
     * 
     * @param flag flag to set
     */
    public void setAddDefaultPrincipals(final boolean flag) {
        addDefaultPrincipals = flag;
    }
    
    /**
     * Get the error messages classified by specific error conditions.
     * 
     * @return classified error message map
     */
    @Nonnull @NonnullElements @Unmodifiable @NotLive public Map<String,Collection<String>> getClassifiedErrors() {
        return ImmutableMap.copyOf(classifiedMessages);
    }
    
    /**
     * Set the error messages indicating an unknown username.
     * 
     * @param messages the "unknown username" error messages to set
     */
    public void setClassifiedMessages(@Nonnull @NonnullElements final Map<String,Collection<String>> messages) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(messages, "Map of classified messages cannot be null");
        
        classifiedMessages = new HashMap<>();
        for (final Map.Entry<String, Collection<String>> entry : messages.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty()
                    && entry.getValue() != null && !entry.getValue().isEmpty()) {
                classifiedMessages.put(entry.getKey(),
                        ImmutableList.copyOf(Collections2.filter(entry.getValue(), Predicates.notNull())));
            }
        }
    }

    /**
     * Get predicate to apply to determine cacheability of {@link AuthenticationResult}.
     * 
     * @return predicate to apply, or null
     */
    @Nullable public Predicate<ProfileRequestContext> getResultCachingPredicate() {
        return resultCachingPredicate;
    }

    /**
     * Set predicate to apply to determine cacheability of {@link AuthenticationResult}.
     * 
     * @param predicate predicate to apply, or null
     */
    public void setResultCachingPredicate(@Nullable final Predicate<ProfileRequestContext> predicate) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        resultCachingPredicate = predicate;
    }

    /**
     * Set the strategy used to locate the requester ID for canonicalization.
     * 
     * @param strategy lookup strategy
     */
    public void setRequesterLookupStrategy(
            @Nullable final Function<ProfileRequestContext,String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        requesterLookupStrategy = strategy;
    }

    /**
     * Set the strategy used to locate the responder ID for canonicalization.
     * 
     * @param strategy lookup strategy
     */
    public void setResponderLookupStrategy(
            @Nullable final Function<ProfileRequestContext,String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        responderLookupStrategy = strategy;
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull @NonnullElements @Unmodifiable @NotLive public <T extends Principal> Set<T> getSupportedPrincipals(
            @Nonnull final Class<T> c) {
        return authenticatedSubject.getPrincipals(c);
    }
    
    /**
     * Set supported non-user-specific principals that the action will include in the subjects
     * it generates, in place of any default principals from the flow.
     * 
     * <p>Setting to a null or empty collection will maintain the default behavior of relying on the flow.</p>
     * 
     * @param <T> a type of principal to add, if not generic
     * @param principals supported principals to include
     */
    public <T extends Principal> void setSupportedPrincipals(
            @Nullable @NonnullElements final Collection<T> principals) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        authenticatedSubject.getPrincipals().clear();
        
        if (principals != null && !principals.isEmpty()) {
            addDefaultPrincipals = false;
            authenticatedSubject.getPrincipals().addAll(Collections2.filter(principals, Predicates.notNull()));
        } else {
            addDefaultPrincipals = true;
        }
    }
 
    /**
     * Get the subject to be produced by successful execution of this action.
     * 
     * @return  the subject meant as the result of this action
     */
    @Nonnull protected Subject getSubject() {
        return authenticatedSubject;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        if (!super.doPreExecute(profileRequestContext, authenticationContext)) {
            return false;
        } else if (authenticationContext.getAttemptedFlow() == null) {
            log.info("{} No attempted flow within authentication context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }

        if (clearErrorContext) {
            authenticationContext.removeSubcontext(AuthenticationErrorContext.class);
        }
        
        // If the request mandates particular principals, evaluate this validating component to see if it
        // can produce a matching principal. This skips validators chained together in flows that aren't
        // able to satisfy the request. This step only applies if the validator has been injected with
        // specific principals, otherwise the flow's capabilities have already been examined.
        final RequestedPrincipalContext rpCtx = authenticationContext.getSubcontext(RequestedPrincipalContext.class);
        if (rpCtx != null && rpCtx.getOperator() != null && !authenticatedSubject.getPrincipals().isEmpty()) {
            log.debug("{} Request contains principal requirements, evaluating for compatibility", getLogPrefix());
            for (Principal p : rpCtx.getRequestedPrincipals()) {
                final PrincipalEvalPredicateFactory factory =
                        authenticationContext.getPrincipalEvalPredicateFactoryRegistry().lookup(
                                p.getClass(), rpCtx.getOperator());
                if (factory != null) {
                    PrincipalEvalPredicate predicate = factory.getPredicate(p);
                    if (predicate.apply(this)) {
                        log.debug("{} Compatible with principal type '{}' and operator '{}'", getLogPrefix(),
                                p.getClass(), rpCtx.getOperator());
                        rpCtx.setMatchingPrincipal(predicate.getMatchingPrincipal());
                        return true;
                    } else {
                        log.debug("{} Not compatible with principal type '{}' and operator '{}'", getLogPrefix(),
                                p.getClass(), rpCtx.getOperator());
                    }
                } else {
                    log.debug("{} No comparison logic registered for principal type '{}' and operator '{}'",
                            getLogPrefix(), p.getClass(), rpCtx.getOperator());
                }
            }
            
            log.info("{} Skipping validator, not compatible with request's principal requirements", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.REQUEST_UNSUPPORTED);
            return false;
        }
        
        return true;
    }
    
    /**
     * Normally called upon successful completion of credential validation, calls the {@link #populateSubject(Subject)}
     * abstract method, stores an {@link AuthenticationResult} in the {@link AuthenticationContext}, and attaches a
     * {@link SubjectCanonicalizationContext} to the {@link ProfileRequestContext} in preparation for c14n to occur.
     * 
     * @param profileRequestContext the current profile request context
     * @param authenticationContext the current authentication context
     */
    protected void buildAuthenticationResult(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        if (addDefaultPrincipals && authenticationContext.getAttemptedFlow() != null) {
            log.debug("{} Adding custom Principal(s) defined on underlying flow descriptor", getLogPrefix());
            authenticatedSubject.getPrincipals().addAll(
                    authenticationContext.getAttemptedFlow().getSupportedPrincipals());
        }
        
        final AuthenticationResult result = new AuthenticationResult(authenticationContext.getAttemptedFlow().getId(),
                populateSubject(authenticatedSubject));
        authenticationContext.setAuthenticationResult(result);
        
        // Override cacheability if a predicate is installed.
        if (authenticationContext.isResultCacheable() && resultCachingPredicate != null) {
            authenticationContext.setResultCacheable(resultCachingPredicate.apply(profileRequestContext));
            log.info("{} Predicate indicates authentication result {} be cacheable in a session", getLogPrefix(),
                    authenticationContext.isResultCacheable() ? "will" : "will not");
        }
        
        // Transfer the subject to a new c14n context.
        final SubjectCanonicalizationContext c14n = new SubjectCanonicalizationContext();
        c14n.setSubject(result.getSubject());
        if (requesterLookupStrategy != null) {
            c14n.setRequesterId(requesterLookupStrategy.apply(profileRequestContext));
        }
        if (responderLookupStrategy != null) {
            c14n.setResponderId(responderLookupStrategy.apply(profileRequestContext));
        }
        profileRequestContext.addSubcontext(c14n, true);
    }
    
    /**
     * Subclasses must override this method to complete the population of the {@link Subject} with
     * {@link Principal} and credential information based on the validation they perform.
     * 
     * <p>Typically this will include attaching a {@link net.shibboleth.idp.authn.principal.UsernamePrincipal},
     * but this is not a requirement if other components are suitably overridden.</p>
     * 
     * @param subject subject to populate
     * @return  the input subject
     */
    @Nonnull protected abstract Subject populateSubject(@Nonnull final Subject subject);
    
    /**
     * Adds an exception encountered during the action to an {@link AuthenticationErrorContext}, creating one if
     * necessary, beneath the {@link AuthenticationContext}.
     * 
     * <p>The exception message is evaluated as a potential match as a "classified" error and if matched,
     * the classification label is attached to the {@link AuthenticationErrorContext} and used as the
     * resulting event for the action.
     * 
     * @param profileRequestContext the current profile request context
     * @param authenticationContext the current authentication context
     * @param e the exception to process
     * @param eventId the event to "return" via an {@link org.opensaml.profile.context.EventContext} if
     *  the exception message is not classified
     */
    protected void handleError(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext, @Nonnull final Exception e,
            @Nonnull @NotEmpty final String eventId) {

        final AuthenticationErrorContext errorCtx =
                authenticationContext.getSubcontext(AuthenticationErrorContext.class, true);
        errorCtx.addException(e);

        handleError(profileRequestContext, authenticationContext, e.getMessage(), eventId);
    }
    
    /**
     * Evaluates a message as a potential match as a "classified" error and if matched, the classification
     * label is attached to an {@link AuthenticationErrorContext} and used as the resulting event for the action.
     * 
     * <p>If no match, the supplied eventId is used as the result.</p>
     * 
     * <p>If multiple matches, the first matching label is used as the result, but each match is added to the
     * context.</p>
     * 
     * @param profileRequestContext the current profile request context
     * @param authenticationContext the current authentication context
     * @param message to process
     * @param eventId the event to "return" via an {@link org.opensaml.profile.context.EventContext} if
     *  the message is not classified
     */
    protected void handleError(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext, @Nullable final String message,
            @Nonnull @NotEmpty final String eventId) {

        boolean eventSet = false;

        if (!Strings.isNullOrEmpty(message)) {
            final MessageChecker checker = new MessageChecker(message);
            
            for (final Map.Entry<String, Collection<String>> entry : classifiedMessages.entrySet()) {
                if (Iterables.any(entry.getValue(), checker)) {
                    authenticationContext.getSubcontext(AuthenticationErrorContext.class,
                            true).getClassifiedErrors().add(entry.getKey());
                    if (!eventSet) {
                        eventSet = true;
                        ActionSupport.buildEvent(profileRequestContext, entry.getKey());
                    }
                }
            }
        }
        
        if (!eventSet) {
            ActionSupport.buildEvent(profileRequestContext, eventId);
        }
    }
    
    /**
     * Evaluates a message as a potential match as a "classified" warning and if matched, the classification
     * label is attached to an {@link AuthenticationWarningContext} and used as the resulting event for the action.
     * 
     * <p>If no match, the supplied eventId is used as the result.</p>
     * 
     * <p>If multiple matches, the first matching label is used as the result, but each match is added to the
     * context.</p>
     * 
     * @param profileRequestContext the current profile request context
     * @param authenticationContext the current authentication context
     * @param message to process
     * @param eventId the event to "return" via an {@link org.opensaml.profile.context.EventContext} if
     *  the message is not classified
     */
    protected void handleWarning(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext, @Nullable final String message,
            @Nonnull @NotEmpty final String eventId) {
        
        boolean eventSet = false;
        
        if (!Strings.isNullOrEmpty(message)) {
            final MessageChecker checker = new MessageChecker(message);
            
            for (Map.Entry<String, Collection<String>> entry : classifiedMessages.entrySet()) {
                if (Iterables.any(entry.getValue(), checker)) {
                    authenticationContext.getSubcontext(AuthenticationWarningContext.class,
                            true).getClassifiedWarnings().add(entry.getKey());
                    if (!eventSet) {
                        eventSet = true;
                        ActionSupport.buildEvent(profileRequestContext, entry.getKey());
                    }
                }
            }
        }
        
        if (!eventSet) {
            ActionSupport.buildEvent(profileRequestContext, eventId);
        }
    }
    
    /** A predicate that examines a message to see if it contains a particular String. */
    private class MessageChecker implements Predicate<String> {

        /** Message to operate on. */
        @Nonnull @NotEmpty private final String s;
        
        /**
         * Constructor.
         *
         * @param msg to operate on
         */
        public MessageChecker(@Nonnull @NotEmpty final String msg) {
            Constraint.isFalse(Strings.isNullOrEmpty(msg), "Message cannot be null or empty");
            s = msg;
        }
        
        /** {@inheritDoc} */
        public boolean apply(String input) {
            return s.contains(input);
        }
    }
    
}