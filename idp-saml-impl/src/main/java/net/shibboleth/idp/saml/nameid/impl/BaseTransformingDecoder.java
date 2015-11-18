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

package net.shibboleth.idp.saml.nameid.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Regular expression transform of an identifier. */
public abstract class BaseTransformingDecoder extends AbstractIdentifiableInitializableComponent {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(BaseTransformingDecoder.class);
    
    /** Match patterns and replacement strings to apply. */
    @Nonnull @NonnullElements private List<Pair<Pattern,String>> transforms;
    
    /** Constructor. */
    public BaseTransformingDecoder() {
        transforms = Collections.emptyList();
    }
    
    /**
     * A collection of regular expression and replacement pairs.
     * 
     * @param newTransforms collection of replacement transforms
     */
    public void setTransforms(@Nonnull @NonnullElements final Collection<Pair<String,String>> newTransforms) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(newTransforms, "Transforms collection cannot be null");
        
        transforms = new ArrayList();
        for (Pair<String,String> p : newTransforms) {
            Pattern pattern = Pattern.compile(StringSupport.trimOrNull(p.getFirst()));
            transforms.add(new Pair(pattern, Constraint.isNotNull(
                    StringSupport.trimOrNull(p.getSecond()), "Replacement expression cannot be null")));
        }
    }
    
    /**
     * Apply configured transforms to input identifier.
     * 
     * @param id the identifier to transform
     * @return transformed value
     */
    @Nullable protected String decode(@Nonnull @NotEmpty final String id) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        String s = id;
        
        for (final Pair<Pattern,String> p : transforms) {            
            final Matcher m = p.getFirst().matcher(s);
            log.debug("Applying replacement expression '{}' against input '{}'", p.getFirst().pattern(), s);
            s = m.replaceAll(p.getSecond());
            log.debug("Result of replacement is '{}'", s);
        }
        
        return s;
    }

}