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

package edu.internet2.middleware.shibboleth.common.attribute.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.ad.impl.ScriptedIdPAttributeImpl;

/**
 * A class which is here solely to provide compatibility for V2 scripted attribute definitions. The assumption is that a
 * constructor will be called with a string and that only {@link #getValues()} would be called from then on.
 */
public class BasicAttribute extends ScriptedIdPAttributeImpl {

    /** Log. */
    private Logger log = LoggerFactory.getLogger(ScriptedIdPAttributeImpl.class);

    /**
     * Constructor.
     * 
     * @param id The attribute Id.
     */
    public BasicAttribute(String id) {
        super(new IdPAttribute(id), "Scripted Attribute Definition: ");
        log.info("{}  Use of V2 emulated class \"BasicAttribute\", consider replacing this code", getLogPrefix());
    }
}
