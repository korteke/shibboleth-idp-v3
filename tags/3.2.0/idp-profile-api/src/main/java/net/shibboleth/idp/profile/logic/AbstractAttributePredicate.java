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

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Abstract base class for predicates operating on an {@link AttributeContext}.
 */
public abstract class AbstractAttributePredicate implements Predicate<ProfileRequestContext> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractAttributePredicate.class);

    /** Strategy function to lookup {@link AttributeContext}. */
    @Nonnull private Function<ProfileRequestContext,AttributeContext> attributeContextLookupStrategy;

    /** Whether to look at filtered or unfiltered attributes. */
    private boolean useUnfilteredAttributes;

    /** Constructor. */
    public AbstractAttributePredicate() {
        attributeContextLookupStrategy = Functions.compose(new ChildContextLookup<>(AttributeContext.class),
                new ChildContextLookup<ProfileRequestContext,RelyingPartyContext>(RelyingPartyContext.class));
        useUnfilteredAttributes = true;
    }

    /**
     * Set the lookup strategy to use to locate the {@link AttributeContext}.
     * 
     * @param strategy lookup function to use
     */
    public void setAttributeContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,AttributeContext> strategy) {

        attributeContextLookupStrategy =
                Constraint.isNotNull(strategy, "AttributeContext lookup strategy cannot be null");
    }
    
    /**
     * Set whether to source the input attributes from the unfiltered set.
     * 
     * <p>Defaults to true.</p>
     * 
     * @param flag flag to set
     */
    public void setUseUnfilteredAttributes(final boolean flag) {
        useUnfilteredAttributes = flag;
    }
    

    /** {@inheritDoc} */
    @Override
    public boolean apply(@Nullable final ProfileRequestContext input) {
        
        final AttributeContext attributeCtx = attributeContextLookupStrategy.apply(input);
        if (attributeCtx == null) {
            log.warn("No AttributeContext located for evaluation");
            return allowNullAttributeContext();
        }
        
        final Map<String,IdPAttribute> attributes = useUnfilteredAttributes
                ? attributeCtx.getUnfilteredIdPAttributes()
                : attributeCtx.getIdPAttributes();

        if (hasMatch(attributes)) {
            log.debug("Context satisfied requirements");
            return true;
        }
        return false;
    }

    /**
     * Get the result of the predicate in the case the attribute context is null.
     * 
     * @return null context result
     */
    protected boolean allowNullAttributeContext() {
        return false;
    }

    /**
     * Abstract implementation of the condition to evaluate.
     * 
     * @param attributeMap  the attributes to evaluate
     * 
     * @return the condition result
     */
    protected abstract boolean hasMatch(@Nonnull @NonnullElements final Map<String,IdPAttribute> attributeMap);
    
}