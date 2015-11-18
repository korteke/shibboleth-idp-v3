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

package net.shibboleth.idp.consent.flow.impl;

import javax.annotation.Nonnull;

import net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NonNegative;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;

/**
 * Descriptor for a consent flow.
 * 
 * A consent flow models a sequence of actions which retrieves consent from storage as well as extracts consent from
 * user input.
 */
public class ConsentFlowDescriptor extends ProfileInterceptorFlowDescriptor {

    /** Whether consent equality includes comparing consent values. */
    private boolean compareValues;

    /** Time in milliseconds to expire consent storage records. Default value: 1 year. */
    @Nonnull @Duration @NonNegative private Long lifetime;

    /** Maximum number of records stored in the storage service. */
    private int maxStoredRecords;

    /** Expanded maximum number of records stored in the storage service. */
    private int expandedMaxStoredRecords;
    
    /** Value size at which expanded maxium takes effect. */
    private long expandedStorageThreshold;

    /** Constructor. */
    public ConsentFlowDescriptor() {
        lifetime = DOMTypeSupport.durationToLong("P1Y");
        expandedStorageThreshold = 1024 * 1024;
    }

    /**
     * Whether consent equality includes comparing consent values.
     * 
     * @return true if consent equality includes comparing consent values
     */
    public boolean compareValues() {
        return compareValues;
    }

    /**
     * Time in milliseconds to expire consent storage records.
     * 
     * @return time in milliseconds to expire consent storage records
     */
    @Nonnull @NonNegative public Long getLifetime() {
        return lifetime;
    }

    /**
     * Get the maximum number of records to keep in the storage service if the expanded size threshold is not met.
     * 
     * @return the maximum number of records, or <=0 for no limit
     */
    public int getMaximumNumberOfStoredRecords() {
        return maxStoredRecords;
    }

    /**
     * Get the maximum number of records to keep in the storage service if the expanded size threshold is met.
     * 
     * @return the maximum number of records, or <=0 for no limit
     */
    public int getExpandedNumberOfStoredRecords() {
        return expandedMaxStoredRecords;
    }
    
    /**
     * Get the storage value size at which the expanded maximum record size kicks in.
     * 
     * @return  storage value size to enable expanded record maximum
     */
    public long getExpandedStorageThreshold() {
        return expandedStorageThreshold;
    }

    /**
     * Set whether consent equality includes comparing consent values.
     * 
     * @param flag true if consent equality includes comparing consent values
     */
    public void setCompareValues(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        compareValues = flag;
    }

    /**
     * Set time in milliseconds to expire consent storage records.
     * 
     * @param consentLifetime time in milliseconds to expire consent storage records
     */
    public void setLifetime(@Nonnull @Duration @NonNegative final Long consentLifetime) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(consentLifetime, "Lifetime cannot be null");

        lifetime = Constraint.isGreaterThanOrEqual(0, consentLifetime, "Lifetime must be greater than or equal to 0");
    }

    /**
     * Set the maximum number of records to keep in the storage service if the expanded size threshold is not met.
     * 
     * @param maximum the maximum number of records, or <=0 for no limit
     */
    public void setMaximumNumberOfStoredRecords(final int maximum) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
    
        maxStoredRecords = maximum;
    }

    /**
     * Set the maximum number of records to keep in the storage service if the expanded size threshold is met.
     * 
     * @param maximum the maximum number of records, or <=0 for no limit
     */
    public void setExpandedNumberOfStoredRecords(final int maximum) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
    
        expandedMaxStoredRecords = maximum;
    }

    /**
     * Set the storage value size at which the expanded maximum record size kicks in.
     * 
     * <p>Defaults to 1024^2</p>
     * 
     * @param size size threshold
     */
    public void setExpandedStorageThreshold(final long size) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
    
        expandedStorageThreshold = size;
    }
    
}