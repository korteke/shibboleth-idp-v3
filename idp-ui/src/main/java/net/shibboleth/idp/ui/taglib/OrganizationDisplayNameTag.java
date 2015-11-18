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

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;

import net.shibboleth.utilities.java.support.codec.HTMLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service OrganizationDisplayName - directly from the metadata if present. */
public class OrganizationDisplayNameTag extends ServiceTagSupport {

    /** Serial ID. */
    private static final long serialVersionUID = -196716418770324981L;

    /** Class logger. */
    private static Logger log = LoggerFactory.getLogger(OrganizationDisplayNameTag.class);

    /**
     * look for the &lt;OrganizationDisplayName&gt;.
     * 
     * @return null or an appropriate string
     */
    @Nullable private String getOrganizationDisplayName() {
        if (getRelyingPartyUIContext() == null) {
            return null;
        }
        return getRelyingPartyUIContext().getOrganizationDisplayName();
    }

    /** {@inheritDoc} */
    @Override public int doEndTag() throws JspException {

        final String name = getOrganizationDisplayName();

        try {
            if (null == name) {
                BodyContent bc = getBodyContent();
                if (null != bc) {
                    JspWriter ew = bc.getEnclosingWriter();
                    if (ew != null) {
                        bc.writeOut(ew);
                    }
                }
            } else {
                pageContext.getOut().print(HTMLEncoder.encodeForHTML(name));
            }
        } catch (IOException e) {
            log.warn("Error generating OrganizationDisplayName", e);
            throw new JspException("EndTag", e);
        }
        return super.doEndTag();
    }

}