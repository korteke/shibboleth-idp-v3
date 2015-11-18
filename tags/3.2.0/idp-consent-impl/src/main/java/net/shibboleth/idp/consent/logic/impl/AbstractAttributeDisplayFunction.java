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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import com.google.common.base.Function;

/**
 * Abstract Function which returns {@link Locale}-aware information about an attribute. The abstract method
 * {@link #getDisplayInfo(IdPAttribute)} returns the information selected from the attribute. This function defaults to
 * returning the attribute ID if no information is selected from the attribute for the desired locales.
 */
public abstract class AbstractAttributeDisplayFunction implements Function<IdPAttribute, String> {

    /** Desired locales in order of preference. */
    @Nonnull private final List<Locale> locales;

    /**
     * Constructor.
     * 
     * @param request {@link HttpServletRequest} used to get preferred languages
     * @param defaultLanguages list of fallback languages in order of decreasing preference
     */
    public AbstractAttributeDisplayFunction(@Nonnull final HttpServletRequest request,
            @Nullable final List<String> defaultLanguages) {

        final List<Locale> newLocales = new ArrayList<>();

        final Enumeration<Locale> requestLocales = request.getLocales();
        while (requestLocales.hasMoreElements()) {
            newLocales.add(requestLocales.nextElement());
        }
        if (null != defaultLanguages) {
            for (final String s : defaultLanguages) {
                if (null != s) {
                    newLocales.add(new Locale(s));
                }
            }
        }
        locales = newLocales;
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NotEmpty public String apply(@Nullable final IdPAttribute input) {
        if (input == null) {
            return "N/A";
        }
        final Map<Locale, String> displayInfo = getDisplayInfo(input);
        if (null != displayInfo && !displayInfo.isEmpty()) {
            for (final Locale locale : locales) {
                String toBeDisplayed = displayInfo.get(locale);
                if (toBeDisplayed != null) {
                    return toBeDisplayed;
                }
                toBeDisplayed = displayInfo.get(Locale.forLanguageTag(locale.getLanguage()));
                if (toBeDisplayed != null) {
                    return toBeDisplayed;
                }
            }
        }
        return input.getId();
    }

    /**
     * Get the information to be displayed from the attribute.
     * 
     * @param input the attribute to consider
     * @return the map of locale dependent information to be displayed
     */
    @Nonnull protected abstract Map<Locale, String> getDisplayInfo(@Nonnull final IdPAttribute input);
}
