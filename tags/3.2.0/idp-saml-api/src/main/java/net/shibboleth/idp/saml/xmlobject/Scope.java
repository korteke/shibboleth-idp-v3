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

package net.shibboleth.idp.saml.xmlobject;

import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.core.xml.schema.XSString;

/** XMLObject for the Shibboleth Scope metadata extension. */
public interface Scope extends XSString {

    /** Element local name. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME = "Scope";

    /** Default element name. */
    public static final QName DEFAULT_ELEMENT_NAME = new QName(ExtensionsConstants.SHIB_MDEXT10_NS,
            DEFAULT_ELEMENT_LOCAL_NAME, ExtensionsConstants.SHIB_MDEXT10_PREFIX);

    /** regexp attribute name. */
    public static final String REGEXP_ATTRIB_NAME = "regexp";

    /**
     * Get the regexp attribute value.
     * 
     * @return the regexp attribute value
     */
    public Boolean getRegexp();

    /**
     * Get the regexp attribute value.
     * 
     * @return the regexp attribute value
     */
    public XSBooleanValue getRegexpXSBoolean();

    /**
     * Set the regexp attribute value.
     * 
     * @param newRegexp the new regexp attribute value
     */
    public void setRegexp(Boolean newRegexp);

    /**
     * Set the regexp attribute value.
     * 
     * @param newRegexp the new regexp attribute value
     */
    public void setRegexp(XSBooleanValue newRegexp);

    /**
     * Gets the match pattern used to evaluate if a scope matches the scope criteria given by this extension. If regular
     * expressions are not used in the scope criteria then this pattern must simply perform a direct match of the
     * string.
     * 
     * @return match pattern used to evaluate if a scope matches the scope criteria
     */
    public Pattern getMatchPattern();
}
