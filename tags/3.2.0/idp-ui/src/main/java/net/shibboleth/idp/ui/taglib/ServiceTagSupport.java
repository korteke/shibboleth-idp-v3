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

package net.shibboleth.idp.ui.taglib;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.ui.context.RelyingPartyUIContext;
import net.shibboleth.utilities.java.support.codec.HTMLEncoder;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * Display the serviceName.
 * 
 * This is taken in order 1) From the mdui 2) AttributeConsumeService 3) HostName from the EntityId 4) EntityId.
 */
public class ServiceTagSupport extends BodyTagSupport {

    /** Serial ID. */
    private static final long serialVersionUID = 4405207268569727209L;

    /** Class logger. */
    private static Logger log = LoggerFactory.getLogger(ServiceTagSupport.class);

    /** Strategy function for access to {@link RelyingPartyUIContext} for input to resolver. */
    @Nonnull private static Function<ProfileRequestContext, RelyingPartyUIContext> uiContextLookupStrategy = Functions
            .compose(new ChildContextLookup<AuthenticationContext, RelyingPartyUIContext>(RelyingPartyUIContext.class),
                    new ChildContextLookup<ProfileRequestContext, AuthenticationContext>(AuthenticationContext.class));

    /** Bean storage. class reference */
    @Nullable private String cssClass;

    /** Bean storage. id reference */
    @Nullable private String cssId;

    /** Bean storage. style reference */
    @Nullable private String cssStyle;

    /** Cached RelyingPartyUIContext. */
    @Nullable private RelyingPartyUIContext relyingPartyUIContext;

    /**
     * Sets the {@link RelyingPartyUIContext}.
     *
     * @param value what to set
     */
    public void setUiContext(@Nullable final RelyingPartyUIContext value) {
        relyingPartyUIContext = value;
    }

    /**
     * Set the Css class to use.
     * 
     * @param value what to set
     */
    public void setCssClass(@Nullable final String value) {
        cssClass = value;
    }

    /**
     * Get the Css class to use.
     * 
     * @param value what to set
     */
    public void setCssId(@Nullable final String value) {
        cssId = value;
    }

    /**
     * Set the Css style to use.
     * 
     * @param value what to set
     */
    public void setCssStyle(@Nullable final String value) {
        cssStyle = value;
    }

    /**
     * Add the class and Id (if present) to the string under construction.
     * 
     * @param sb the {@link StringBuilder} to add to.
     */
    protected void addClassAndId(@Nonnull final StringBuilder sb) {
        if (cssClass != null) {
            sb.append(" class=\"").append(cssClass).append('"');
        }
        if (cssId != null) {
            sb.append(" id=\"").append(cssId).append('"');
        }
        if (cssStyle != null) {
            sb.append(" style=\"").append(cssStyle).append('"');
        }
    }

    /**
     * Build a hyperlink from the parameters.
     * 
     * @param url the URL
     * @param text what to embed
     * @return the hyperlink.
     */
    @Nonnull protected String buildHyperLink(@Nonnull final String url, @Nonnull final String text) {
        final String encodedUrl;

        try {
            final URI theUrl = new URI(url);
            final String scheme = theUrl.getScheme();

            if (!"http".equals(scheme) && !"https".equals(scheme) && !"mailto".equals(scheme)) {
                log.warn("The URL '{}' contained an invalid scheme.", url);
                return "";
            }
            encodedUrl = HTMLEncoder.encodeForHTMLAttribute(url);
        } catch (URISyntaxException e) {
            //
            // It wasn't an URI.
            //
            log.warn("The URL '{}' was invalid: ", url, e);
            return "";
        }

        final StringBuilder sb = new StringBuilder("<a href=\"");
        sb.append(encodedUrl).append('"');
        addClassAndId(sb);
        sb.append(">").append(HTMLEncoder.encodeForHTML(text)).append("</a>");
        return sb.toString();
    }

    /**
     * Get the {@link RelyingPartyUIContext} for the request. We cache this if it exists (the usual case).
     * 
     * @return the context
     */
    @Nullable protected RelyingPartyUIContext getRelyingPartyUIContext() {

        if (null != relyingPartyUIContext) {
            return relyingPartyUIContext;
        }

        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        if (request == null) {
            return null;
        }

        final ProfileRequestContext pc = (ProfileRequestContext) request.getAttribute("profileRequestContext");
        relyingPartyUIContext = uiContextLookupStrategy.apply(pc);
        return relyingPartyUIContext;
    }

    /**
     * Get the identifier for the service name as per the rules above.
     * 
     * @return something sensible for display.
     */
    @Nullable protected String getServiceName() {

        if (getRelyingPartyUIContext() == null) {
            return null;
        }
        return getRelyingPartyUIContext().getServiceName();
    }

}