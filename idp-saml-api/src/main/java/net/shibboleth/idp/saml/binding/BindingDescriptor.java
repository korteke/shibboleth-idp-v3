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

package net.shibboleth.idp.saml.binding;

import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Subclass that adds awareness of a Spring bean ID for a binding's
 * {@link org.opensaml.messaging.encoder.MessageEncoder}.
 */
public class BindingDescriptor extends org.opensaml.saml.common.binding.BindingDescriptor {

    /** Spring bean ID of message encoder. */
    @Nullable private String encoderBeanId;
    
    /**
     * Get the Spring bean ID of the binding's {@link org.opensaml.messaging.encoder.MessageEncoder}.
     * 
     * @return bean ID of message encoder
     */
    @Nullable public String getEncoderBeanId() {
        return encoderBeanId;
    }

    /**
     * Set the Spring bean ID of the binding's {@link org.opensaml.messaging.encoder.MessageEncoder}.
     * 
     * @param id bean ID of message encoder
     */
    public void setEncoderBeanId(@Nullable final String id) {
        encoderBeanId = StringSupport.trimOrNull(id);
    }
    
}