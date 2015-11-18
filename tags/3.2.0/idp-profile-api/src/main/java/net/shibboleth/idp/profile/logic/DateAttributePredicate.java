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

package net.shibboleth.idp.profile.logic;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Provides a date/time matching predicate that compares a date-based attribute value against
 * current system time with optional offset. By convention the predicate returns true if and only if
 * the date represented by the attribute value is after the current system time; false otherwise.
 * Thus the semantics are well-suited for cases such as evaluation of expiration dates.
 *
 * @author Marvin S. Addison
 */
public class DateAttributePredicate extends AbstractAttributePredicate {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(DateAttributePredicate.class);

    /** Name of attribute to query for. */
    @Nonnull private final String attributeName;

    /** Formatter used to parse string-based date attribute values. */
    @Nonnull private final DateTimeFormatter dateTimeFormatter;

    /** Offset from system time used for date comparisons. */
    @Nonnull private Duration systemTimeOffset = Duration.ZERO;

    /**
     * Creates a new instance that performs date comparisons against the given attribute
     * using ISO date/time format parser by default.
     *
     * @param attribute Attribute name that provides candidate date values to test.
     */
    public DateAttributePredicate(final String attribute) {
        this(attribute, ISODateTimeFormat.dateOptionalTimeParser());
    }

    /**
     * Creates a new instance that performs date comparisons against the given attribute
     * using the given date parser.
     *
     * @param attribute Attribute name that provides candidate date values to test.
     * @param formatter Date/time parser.
     */
    public DateAttributePredicate(final String attribute, final DateTimeFormatter formatter) {
        this.attributeName = Constraint.isNotNull(attribute, "Attribute cannot be null");
        this.dateTimeFormatter = Constraint.isNotNull(formatter, "Formatter cannot be null");
    }

    /**
     * Sets the system time offset, which affects the reference date for comparisons.
     * By default all comparisons are against system time, i.e. zero offset.
     *
     * @param offset System time offset. A negative value decreases the target date (sooner);
     *                         a positive value increases the target date (later).
     */
    public void setSystemTimeOffset(@Nonnull final Duration offset) {
        systemTimeOffset = Constraint.isNotNull(offset, "Offset cannot not be null");
    }

    @Override
    protected boolean hasMatch(final Map<String, IdPAttribute> attributeMap) {
        final IdPAttribute attribute = attributeMap.get(attributeName);
        if (attribute == null) {
            log.info("Attribute {} not found in context", attributeName);
            return false;
        }
        String dateString;
        for (final IdPAttributeValue<?> value : attribute.getValues()) {
            if (value instanceof StringAttributeValue) {
                dateString = ((StringAttributeValue) value).getValue();
                try {
                    if (dateTimeFormatter.parseDateTime(dateString).plus(systemTimeOffset).isAfterNow()) {
                        return true;
                    }
                } catch (RuntimeException e) {
                    log.info("{} is not a valid date for the configured date parser", dateString);
                }
            }
        }
        return false;
    }
    
}