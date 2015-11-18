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

package net.shibboleth.idp.attribute.resolver.dc;

import javax.annotation.Nullable;

import org.slf4j.LoggerFactory;

/** Used to determine whether a Data Connector initialized properly and continues to be fit for use. */
public class NonFailFastValidator implements Validator {

    /** Embedded validator to run. */
    @Nullable private final Validator embeddedValidator;

    /** Constructor. */
    public NonFailFastValidator() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param validator validator to run but trap exceptions from
     */
    public NonFailFastValidator(@Nullable final Validator validator) {
        embeddedValidator = validator;
    }
    
    /** {@inheritDoc} */
    @Override
    public void validate() throws ValidationException {
        if (embeddedValidator != null) {
            try {
                embeddedValidator.validate();
            } catch (final ValidationException e) {
                LoggerFactory.getLogger(NonFailFastValidator.class).warn(
                        "Non-fail-fast validator trapped an error from its embedded validator", e);
            }
        }
    }
    
}