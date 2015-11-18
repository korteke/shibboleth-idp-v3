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

package net.shibboleth.idp.saml.attribute.mapping;

import net.shibboleth.idp.attribute.IdPAttribute;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;

/**
 * Container for reverse mapped attributes. This gives us a distinguished class to look for in the
 * {@link org.opensaml.core.xml.XMLObject#getObjectMetadata()}.
 * 
 * @param <OutType> The type of attribute we are mapping to.
 */
public class AttributesMapContainer<OutType extends IdPAttribute> implements Supplier<Multimap<String, OutType>> {

    /** The map we are encapsulating.*/
    private final Multimap<String, OutType> providedValue;

    /**
     * Constructor.
     * 
     * @param value the value to return.
     */
    public AttributesMapContainer(Multimap<String, OutType> value) {
        providedValue = value;
    }

    /** {@inheritDoc} */
    @Override public Multimap<String, OutType> get() {
        return providedValue;
    }

}
