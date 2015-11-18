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

package net.shibboleth.idp.saml.profile.config.logic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.logic.NoIntegrityMessageChannelPredicate;

/**
 * A predicate implementation that supports the legacy V2 configuration options of
 * "always", "conditional", and "never" for signing.
 */
public class LegacySigningRequirementPredicate extends NoIntegrityMessageChannelPredicate {

    /** Internal enum for the options supported. */
    private enum SigningRequirementSetting {
        /** Always sign. */
        ALWAYS,
        
        /** Sign only if integrity is not otherwise assumed. */
        CONDITIONAL,
        
        /** Never sign. */
        NEVER,
    }
    
    /** The setting in effect. */
    private final SigningRequirementSetting settingToApply; 
    
    /**
     * Constructor.
     * 
     * @param setting  the setting to apply
     */
    public LegacySigningRequirementPredicate(@Nonnull @NotEmpty final String setting) {
        Constraint.isNotNull(setting, "Signing requirement setting cannot be null");
        
        if ("always".equals(setting)) {
            settingToApply = SigningRequirementSetting.ALWAYS;
        } else if ("conditional".equals(setting)) {
            settingToApply = SigningRequirementSetting.CONDITIONAL;
        } else if ("never".equals(setting)) {
            settingToApply = SigningRequirementSetting.NEVER;
        } else {
            throw new IllegalArgumentException("Signing requirement setting not one of the supported values");
        }
    }

    /** {@inheritDoc} */
    public boolean apply(@Nullable final ProfileRequestContext input) {
        switch (settingToApply) {
            case ALWAYS:
                return true;
                
            case NEVER:
                return false;
                
            case CONDITIONAL:
                return super.apply(input);
                
            default:
                throw new IllegalArgumentException("Signing requirement setting not one of the supported values");
        }
    }

}