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

package net.shibboleth.idp.profile.impl;

import javax.annotation.Nullable;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.SpringRequestContext;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

//Checkstyle: JavadocStyle OFF -- ignore extra HTML tag error
/**
 * Spring Web Flow utility action for logging on DEBUG a representation of the current
 * {@link ProfileRequestContext}.
 * 
 * <p>
 * You can contextualize the logging of the context tree either by setting {@link #setDescription(String)},
 * or more usefully by using an attribute on the specific action expression as below.  This allows using
 * just one declaration of the action bean, but parameterized differently depending on where it is placed.
 * 
 * <pre>
 * {@code
 * <evaluate expression="LogContextTree">
 *    <attribute name="contextTreeDescription" value="My Description" />
 * </evaluate>
 *  }
 * </pre>
 * 
 * </p>
 */
//Checkstyle: JavadocStyle ON
public class LogContextTree extends AbstractProfileAction {
    
    /** Name of Spring web flow attribute holding the description of the tree to log. */
    public static final String ATTRIB_DESC = "contextTreeDescription";
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger("CONTEXT_TREE");
    
    /** Contextual description to output at the start of the action. */
    private String description;
    
    /**
     * Set the contextual description to output at the start of the action.
     * 
     * @param value the description value
     */
    public void setDescription(@Nullable final String value) {
        description = StringSupport.trimOrNull(value);
    }

    /** {@inheritDoc} */
    protected void doExecute(ProfileRequestContext profileRequestContext) {
        if (!log.isDebugEnabled()) {
            // short-circuit if not logging at debug
            return;
        }
        
        String contextualDescription = null;
        
        SpringRequestContext springRequestContext = profileRequestContext.getSubcontext(SpringRequestContext.class);
        if (springRequestContext != null && springRequestContext.getRequestContext() != null) {
            RequestContext requestContext = springRequestContext.getRequestContext();
            contextualDescription = requestContext.getAttributes().getString(ATTRIB_DESC);
        }
        
        if (contextualDescription == null) {
            contextualDescription = description;
        }
        
        if (contextualDescription != null) {
            log.debug("Context tree contextual description: {}", contextualDescription) ;
        }
        
        logContext(profileRequestContext, 0);
    }

    /**
     * Recursively log the context tree.
     * 
     * @param current the current context to log
     * @param indent the amount of leading indent
     */
    private void logContext(BaseContext current, int indent) {
        if (current == null) {
            return;
        }
        
        String indentString = getIndent(indent);
        
        if (current instanceof ProfileRequestContext) {
            ProfileRequestContext<?,?> prc = (ProfileRequestContext) current;
            
            log.debug("{} PRC: {}", indentString, prc.getClass().getName());
            for (BaseContext subcontext : prc) {
                logContext(subcontext, indent+1);
            }
            
            MessageContext<?> inbound = prc.getInboundMessageContext();
            if (inbound != null) {
                log.debug("{} PRC InboundMessageContext: {}", indentString, inbound.getClass().getName());
                for (BaseContext subcontext : inbound) {
                    logContext(subcontext, indent+1);
                }
            } else {
                log.debug("{} PRC InboundMessageContext not present", indentString);
            }
            
            MessageContext<?> outbound = prc.getOutboundMessageContext();
            if (outbound != null) {
                log.debug("{} PRC OutboundMessageContext: {}", indentString, outbound.getClass().getName());
                for (BaseContext subcontext : outbound) {
                    logContext(subcontext, indent+1);
                }
            } else {
                log.debug("{} PRC OutboundMessageContext not present", indentString);
            }
            
        } else {
            log.debug("{} {}", indentString, current.getClass().getName());
            for (BaseContext subcontext : current) {
                logContext(subcontext, indent+1);
            }
        }
        
    }
    
    /**
     * Generate the leading indent string to print.
     * 
     * @param indent the amount of the indent
     * 
     * @return the leading indent string to print
     */
    private String getIndent(int indent) {
        StringBuffer buffer = new StringBuffer();
        for (int i=0; i<indent; i++) {
            buffer.append("----");
        }
        return buffer.toString();
    }

}
