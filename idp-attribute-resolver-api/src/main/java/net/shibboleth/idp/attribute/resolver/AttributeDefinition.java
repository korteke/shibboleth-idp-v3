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

package net.shibboleth.idp.attribute.resolver;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;

/** Definition of attribute definition resolver plugins. */
@ThreadSafe
public interface AttributeDefinition extends ResolverPlugin<IdPAttribute> {

    /**
     * Gets whether this attribute definition is only a dependency and thus its values should never be released outside
     * the resolver.
     * 
     * @return true if this attribute is only used as a dependency, false otherwise
     */
    boolean isDependencyOnly();

    /**
     * Gets the localized human readable descriptions of attribute.
     * 
     * @return human readable descriptions of attribute
     */
    @NonnullAfterInit @NonnullElements @Unmodifiable Map<Locale, String> getDisplayDescriptions();

    /**
     * Gets the localized human readable names of the attribute.
     * 
     * @return human readable names of the attribute
     */
    @NonnullAfterInit @NonnullElements @Unmodifiable Map<Locale, String> getDisplayNames();

    /**
     * Gets the unmodifiable encoders used to encode the values of this attribute in to protocol specific formats. The
     * returned collection is never null nor contains any null.
     * 
     * @return encoders used to encode the values of this attribute in to protocol specific formats, never null
     */
    @NonnullAfterInit @NonnullElements @Unmodifiable Set<AttributeEncoder<?>> getAttributeEncoders();

}