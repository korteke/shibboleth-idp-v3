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

package net.shibboleth.idp.profile.audit.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.IdPAuditFields;
import net.shibboleth.idp.profile.context.AuditContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

/**
 * Action that produces audit log entries based on an {@link AuditContext} and one or more formatting strings. 
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 */
public class WriteAuditLog extends AbstractProfileAction {

    /** Formatter used to convert timestamps to strings. */
    private static DateTimeFormatter v2Formatter = ISODateTimeFormat.basicDateTimeNoMillis();

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(WriteAuditLog.class);
    
    /** Strategy used to locate the {@link AuditContext} associated with a given {@link ProfileRequestContext}. */
    @Nonnull private Function<ProfileRequestContext,AuditContext> auditContextLookupStrategy;
    
    /** Map of log category to formatting tokens and literals to output. */
    @Nonnull @NotEmpty private Map<String,List<String>> formattingMap;

    /** The Spring RequestContext to operate on. */
    @Nullable private RequestContext requestContext;

    /** The AuditContext to operate on. */
    @Nullable private AuditContext auditCtx;

    /** HttpServletRequest object. */
    @Nullable private HttpServletRequest httpRequest;
    
    /** Constructor. */
    public WriteAuditLog() {
        auditContextLookupStrategy = new ChildContextLookup<>(AuditContext.class);
        formattingMap = Collections.emptyMap();
    }

    /**
     * Set the strategy used to locate the {@link AuditContext} associated with a given
     * {@link ProfileRequestContext}.
     * 
     * @param strategy lookup strategy
     */
    public void setAuditContextLookupStrategy(@Nonnull final Function<ProfileRequestContext,AuditContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        auditContextLookupStrategy = Constraint.isNotNull(strategy, "AuditContext lookup strategy cannot be null");
    }
    
    /**
     * Get the map of logging category to formatting tokens for log entries.
     * 
     * @return map of formatting tokens
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public Map<String,List<String>> getFormattingMap() {
        return ImmutableMap.copyOf(formattingMap);
    }
    
// Checkstyle: CyclomaticComplexity OFF
    /**
     * Set the map of logging category to formatting strings for log entries.
     * 
     * <p>A formatting string consists of tokens prefixed by '%' separated by any non-alphanumeric or whitespace.
     * Tokens can contain any letter or number or a hypen. Anything other than a token, including whitespace, is
     * a literal.</p>
     * 
     * @param map map of categories to formatting strings
     */
    public void setFormattingMap(@Nonnull @NonnullElements final Map<String,String> map) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(map, "Audit formatting map cannot be null");
        
        formattingMap = new HashMap<>(map.size());
        
        for (final Map.Entry<String,String> entry : map.entrySet()) {
            final String category = StringSupport.trimOrNull(entry.getKey());
            final String s = StringSupport.trimOrNull(entry.getValue());
            if (category == null || s == null) {
                continue;
            }
            
            int len = s.length();
            boolean inToken = false;
            final List<String> format = new ArrayList<>();
            final StringBuilder field = new StringBuilder();
            for (int pos = 0; pos < len; ++pos) {
                char ch = s.charAt(pos);
                if (inToken) {
                    if (!Character.isLetterOrDigit(ch) && ch != '-' && ch != '%') {
                        format.add(field.toString());
                        field.setLength(0);
                        inToken = false;
                    }
                } else if (ch == '%') {
                    if (field.length() > 0) {
                        format.add(field.toString());
                        field.setLength(0);
                    }
                    inToken = true;
                }
                
                field.append(ch);
            }
            
            if (field.length() > 0) {
                format.add(field.toString());
            }
            
            formattingMap.put(category, format);
        }
    }
// Checkstyle: CyclomaticComplexity ON


    /** {@inheritDoc} */
    @Override
    @Nonnull protected Event doExecute(@Nonnull final RequestContext springRequestContext,
            @Nonnull final ProfileRequestContext profileRequestContext) {
        requestContext = springRequestContext;
        return super.doExecute(springRequestContext, profileRequestContext);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        } else if (formattingMap.isEmpty()) {
            log.debug("No formatting for audit records supplied, nothing to do");
            return false;
        }
        
        auditCtx = auditContextLookupStrategy.apply(profileRequestContext);
        httpRequest = getHttpServletRequest();
        return true;
    }
    
// Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        for (Map.Entry<String,List<String>> entry : formattingMap.entrySet()) {
        
            final StringBuilder record = new StringBuilder();
    
            for (final String token : entry.getValue()) {
                if (token.startsWith("%")) {
                    if (token.length() == 1 || token.charAt(1) == '%') {
                        record.append('%');
                    } else {
                        final String field = token.substring(1);
                        
                        if (IdPAuditFields.EVENT_TIME.equals(field)) {
                            record.append(new DateTime().toString(v2Formatter.withZone(DateTimeZone.UTC)));
                        } else if (IdPAuditFields.EVENT_TYPE.equals(field)) {
                            final Event event = requestContext.getCurrentEvent();
                            if (event != null && !event.getId().equals(EventIds.PROCEED_EVENT_ID)) {
                                record.append(event.getId());
                            }
                        } else if (IdPAuditFields.PROFILE.equals(field)) {
                            record.append(profileRequestContext.getProfileId());
                        } else if (IdPAuditFields.REMOTE_ADDR.equals(field) && httpRequest != null) {
                            record.append(httpRequest.getRemoteAddr());
                        } else if (IdPAuditFields.URI.equals(field) && httpRequest != null) {
                            record.append(httpRequest.getRequestURI());
                        } else if (IdPAuditFields.URL.equals(field) && httpRequest != null) {
                            record.append(httpRequest.getRequestURL());
                        } else if (IdPAuditFields.USER_AGENT.equals(field) && httpRequest != null) {
                            record.append(httpRequest.getHeader("User-Agent"));
                        } else if (auditCtx != null) {
                            final Iterator<String> iter = auditCtx.getFieldValues(field).iterator();
                            while (iter.hasNext()) {
                                record.append(iter.next());
                                if (iter.hasNext()) {
                                    record.append(',');
                                }
                            }
                        }
                    }
                } else {
                    record.append(token);
                }
            }
            
            filter(record);
            
            LoggerFactory.getLogger(entry.getKey() + '.'
                    + profileRequestContext.getLoggingId()).info(record.toString());
        }
    }
// Checkstyle: CyclomaticComplexity ON
    
    /**
     * Optional override to filter the outgoing log message, does nothing by default.
     * 
     * @param entry log entry
     */
    protected void filter(@Nonnull final StringBuilder entry) {
        
    }
    
}