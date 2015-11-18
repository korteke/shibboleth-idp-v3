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

package net.shibboleth.idp.consent.logic.impl;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.attribute.IdPAttribute;

import com.google.common.base.Function;

/**
 * Function which returns the locale-aware display name of an attribute, defaulting to the
 * attribute ID if the attribute has no display name.
 */
public class AttributeDisplayNameFunction extends AbstractAttributeDisplayFunction implements
        Function<IdPAttribute, String> {

    /**
     * Constructor.
     * 
     * @param request {@link HttpServletRequest} used to get preferred languages
     * @param defaultLangauages list of fallback languages in order of decreasing preference
     */
    public AttributeDisplayNameFunction(@Nonnull final HttpServletRequest request,
            @Nullable final List<String> defaultLangauages) {
        super(request, defaultLangauages);
    }

    /** {@inheritDoc} */
    @Override @Nonnull protected Map<Locale, String> getDisplayInfo(@Nonnull final IdPAttribute attribute) {
        return attribute.getDisplayNames();
    }
}
