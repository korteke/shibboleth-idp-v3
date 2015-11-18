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

package net.shibboleth.idp.log;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.shibboleth.idp.Version;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import org.slf4j.MDC;


/**
 * Servlet filter that sets some interesting MDC attributes as the request comes in and clears the MDC as the response
 * is returned.
 */
public class SLF4JMDCServletFilter implements Filter {
    
    /** MDC attribute name for host name of the server to which the current request was sent. */
    @Nonnull @NotEmpty public static final String SERVER_ADDRESS_MDC_ATTRIBUTE = "idp.server_hostname";

    /** MDC attribute name for port number to which the current request was sent. */
    @Nonnull @NotEmpty public static final String SERVER_PORT_MDC_ATTRIBUTE = "idp.server_port";
    
    /** MDC attribute name for client address. */
    @Nonnull @NotEmpty public static final String CLIENT_ADDRESS_MDC_ATTRIBUTE = "idp.remote_addr";

    /** MDC attribute name for container session ID. */
    @Nonnull @NotEmpty public static final String JSESSIONID_MDC_ATTRIBUTE = "idp.jsessionid";

    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        try {
            MDC.put(Version.MDC_ATTRIBUTE, Version.getVersion());
            MDC.put(CLIENT_ADDRESS_MDC_ATTRIBUTE, request.getRemoteAddr());
            MDC.put(SERVER_ADDRESS_MDC_ATTRIBUTE, request.getServerName());
            MDC.put(SERVER_PORT_MDC_ATTRIBUTE, Integer.toString(request.getServerPort()));
            if (request instanceof HttpServletRequest) {
                final HttpSession session = ((HttpServletRequest) request).getSession();
                if (session != null) {
                    MDC.put(JSESSIONID_MDC_ATTRIBUTE, session.getId());
                }
            }
            
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to do
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        // nothing to do
    }
}