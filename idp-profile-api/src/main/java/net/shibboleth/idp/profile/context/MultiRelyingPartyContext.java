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

package net.shibboleth.idp.profile.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;

/**
 * {@link BaseContext} representing multiple relying parties involved in a request, usually a
 * subcontext of {@link org.opensaml.profile.context.ProfileRequestContext}.
 * 
 * <p>The multiple parties may be accessed as a collection, by their name, or by "labels",
 * which are specific to a given profile/scenario.</p>
 * 
 * <p>The context also provides state management for flows to iterate over the relying parties
 * in the context using an iterator and a "current" pointer.
 */
public final class MultiRelyingPartyContext extends BaseContext {

    /** Map of RP contexts indexed by name. */
    @Nonnull @NonnullElements private Map<String,RelyingPartyContext> relyingPartyIdMap;
    
    /** Multimap of RP contexts indexed by role. */
    @Nonnull @NonnullElements private ListMultimap<String,RelyingPartyContext> relyingPartyLabelMap;
    
    /** An iterator to track progress through the set of relying parties. */
    @Nullable private Iterator<RelyingPartyContext> relyingPartyIterator;
    
    /** Tracks the context being operated on. */
    @Nullable private RelyingPartyContext relyingPartyCtx;
    
    /** Constructor. */
    public MultiRelyingPartyContext() {
        relyingPartyIdMap = new HashMap<>();
        relyingPartyLabelMap = ArrayListMultimap.create();
    }
    
    /**
     * Get an immutable collection of the RP contexts.
     * 
     * @return  immutable collection of RP contexts
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public Collection<RelyingPartyContext> getRelyingPartyContexts() {
        return ImmutableList.copyOf(relyingPartyIdMap.values());
    }
    
    /**
     * Get an immutable collection of RP contexts associated with a label.
     * 
     * @param label the label to search for
     * 
     * @return  corresponding RP contexts
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public Collection<RelyingPartyContext> getRelyingPartyContexts(
            @Nonnull @NotEmpty final String label) {
        return ImmutableList.copyOf(relyingPartyLabelMap.get(
                Constraint.isNotNull(StringSupport.trimOrNull(label), "Label cannot be null or empty")));
    }
    
    /**
     * Get a RP context by name/id.
     * 
     * @param id the identifier to search for
     * 
     * @return  a corresponding RP context
     */
    @Nullable public RelyingPartyContext getRelyingPartyContextById(@Nonnull @NotEmpty final String id) {
        return relyingPartyIdMap.get(Constraint.isNotNull(StringSupport.trimOrNull(id), "ID cannot be null or empty"));
    }
    
    /**
     * Get an iterator over the relying parties contained in the context.
     * 
     * <p>The first time this method is called, or if the parameter is set,
     * it will return a fresh iterator; subsequent calls will return the same iterator.</p>
     * 
     * <p>Modification of the underlying collection while iterating is not supported.</p>
     * 
     * @param fresh if true, a new iterator will be created and returned
     * 
     * @return an iterator over the relying parties contained in the context
     */
    @Nonnull public Iterator<RelyingPartyContext> getRelyingPartyContextIterator(final boolean fresh) {
        if (fresh || relyingPartyIterator == null) {
            relyingPartyIterator = new RelyingPartyContextIterator(this);
        }
        return relyingPartyIterator;
    }

    /**
     * Equivalent to calling {@link #getRelyingPartyContextIterator(boolean)} with a parameter of "false".
     * 
     * @return an iterator over the relying parties contained in the context
     */
    @Nonnull public Iterator<RelyingPartyContext> getRelyingPartyContextIterator() {
        return getRelyingPartyContextIterator(false);
    }
    
    /**
     * Get the {@link RelyingPartyContext} pointed to by an iterator.
     * 
     * @return  the current position of the last iterator returned by {@link #getRelyingPartyContextIterator()}.
     */
    @Nullable public RelyingPartyContext getCurrentRelyingPartyContext() {
        
        if (relyingPartyIterator != null) {
            return ((RelyingPartyContextIterator) relyingPartyIterator).current;
        } else {
            return null;
        }
    }
    
    /**
     * Add a RP context associated with a label.
     * 
     * @param label the label to associate with the context
     * @param context context to add
     */
    public void addRelyingPartyContext(@Nonnull @NotEmpty final String label,
            @Nonnull final RelyingPartyContext context) {
        final String trimmed = Constraint.isNotNull(StringSupport.trimOrNull(label), "Label cannot be null or empty");
        Constraint.isNotNull(context, "Context cannot be null");
        Constraint.isNotNull(context.getRelyingPartyId(), "RelyingParty ID cannot be null");
        
        relyingPartyIdMap.put(context.getRelyingPartyId(), context);
        relyingPartyLabelMap.put(trimmed, context);
    }
    
    /**
     * Remove a RP context associated with a label.
     * 
     * @param label the label associated with the context
     * @param context context to remove
     */
    public void removeRelyingPartyContext(@Nonnull @NotEmpty final String label,
            @Nonnull final RelyingPartyContext context) {
        final String trimmed = Constraint.isNotNull(StringSupport.trimOrNull(label), "Label cannot be null or empty");
        Constraint.isNotNull(context, "Context cannot be null");
        Constraint.isNotNull(context.getRelyingPartyId(), "RelyingParty ID cannot be null");
        
        relyingPartyIdMap.remove(context.getRelyingPartyId());
        relyingPartyLabelMap.remove(trimmed, context);
    }
    
    /**
     * Wrapper for an iterator that tracks the current object.
     */
    private class RelyingPartyContextIterator implements Iterator<RelyingPartyContext> {

        /** Outer ctx. */
        @Nonnull private final MultiRelyingPartyContext multiCtx;
        
        /** Embedded iterator. */
        @Nonnull private final Iterator<RelyingPartyContext> iterator;
        
        /** Current marker. */
        @Nullable private RelyingPartyContext current;
        
        /**
         * Constructor.
         * 
         * @param ctx outer context
         */
        public RelyingPartyContextIterator(@Nonnull final MultiRelyingPartyContext ctx) {
            multiCtx = ctx;
            iterator = ctx.relyingPartyIdMap.values().iterator();
        }
        
        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /** {@inheritDoc} */
        @Override
        public RelyingPartyContext next() {
            current = iterator.next();
            return current;
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            iterator.remove();
            current = null;
        }
    }
    
}