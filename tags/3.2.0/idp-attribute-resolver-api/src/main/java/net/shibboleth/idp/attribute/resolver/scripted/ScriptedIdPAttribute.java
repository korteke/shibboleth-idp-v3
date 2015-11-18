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

package net.shibboleth.idp.attribute.resolver.scripted;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * This is the API which is available to ECMAScripted attributes. This API is not targetted at JAVA users.
 */
public interface ScriptedIdPAttribute {

    /**
     * Return all the values, but with {@link net.shibboleth.idp.attribute.StringAttributeValue} values returned as
     * strings.<br/>
     * This method is a helper method for V2 compatibility.
     * 
     * @return a modifiable collection of the string attributes (not the String
     * @throws ResolutionException if the script has called {@link #getNativeAttribute()}
     */
    @Nullable public Collection<Object> getValues() throws ResolutionException;

    /**
     * return the underlying attribute.
     * 
     * @return the attribute
     * @throws ResolutionException if the script has called getValues.
     */
    @Nonnull public IdPAttribute getNativeAttribute() throws ResolutionException;

    /**
     * Get the encapsulated attributeId.
     * 
     * @return the id
     */
    @Nonnull @NotEmpty public String getId();

    /**
     * Add the provided object to the attribute values, policing for type.
     * 
     * @param what a {@link String} or a {@link net.shibboleth.idp.attribute.IdPAttributeValue} to add.
     * @throws ResolutionException if the provided value is of the wrong type
     */
    public void addValue(@Nullable final Object what) throws ResolutionException;

}
