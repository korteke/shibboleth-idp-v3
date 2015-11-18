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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.cryptacular.util.CodecUtil;
import org.cryptacular.util.HashUtil;

import com.google.common.base.Function;

/**
 * Function whose output value is a hash of the input value.
 * 
 * Returns <code>null</code> for a <code>null</code> input.
 * 
 * The hash returned is the Base64 encoded representation of the SHA-256 digest.
 */
public class HashFunction implements Function<String, String> {

    /** {@inheritDoc} */
    @Override
    @Nullable public String apply(@Nonnull final String input) {

        if (input == null) {
            return null;
        }

        return CodecUtil.b64(HashUtil.sha256(input));
    }
}
