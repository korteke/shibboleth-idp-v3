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

package net.shibboleth.idp.attribute.resolver.spring.impl;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.ext.spring.factory.AbstractComponentAwareFactoryBean;
import net.shibboleth.idp.attribute.resolver.AbstractResolverPlugin;
import net.shibboleth.idp.attribute.resolver.ResolverPluginDependency;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;

import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * A factory bean to collect the parameterization that goes onto a {@link AbstractResolverPlugin}.
 * 
 * @param <T> The exact type being deployed.
 */
public abstract class AbstractResolverPluginFactoryBean<T extends AbstractResolverPlugin> extends
        AbstractComponentAwareFactoryBean<T> {

    /** The component Id. */
    @Nullable private String componentId;
    
    /** Data Connector property "propagateResolutionExceptions". */
    @Nullable private Boolean propagateResolutionExceptions;

    /** Data Connector property "profileContextStrategy". */
    @Nullable private Function<AttributeResolutionContext, ProfileRequestContext> profileContextStrategy;

    /** Data Connector property "activationCondition". */
    @Nullable private Predicate<ProfileRequestContext> activationCondition;

    /** Data Connector property "dependencies". */
    @Nullable private Set<ResolverPluginDependency> dependencies;

    /** Get the component Id.
     * @return the id.
     */
    @Nullable public String getId() {
        return componentId;
    }
    
    /** Set the component Id.
     * @param id the id.
     */
    @Nullable public void setId(@Nullable final String id) {
        componentId = id;
    }
    
    /**
     * Bean setter in support of {@link AbstractResolverPlugin#setPropagateResolutionExceptions(boolean)}.
     * 
     * @param propagate value to be set
     */
    public void setPropagateResolutionExceptions(final boolean propagate) {
        propagateResolutionExceptions = propagate;
    }

    /**
     * Bean getter in support of {@link AbstractResolverPlugin#setPropagateResolutionExceptions(boolean)}.
     * 
     * @return The value to be set
     */
    @Nullable public Boolean getPropagateResolutionExceptions() {
        return propagateResolutionExceptions;
    }

    /**
     * Bean setter in support of {@link AbstractResolverPlugin#setProfileContextStrategy(Function)}.
     * 
     * @param strategy value to be set
     */
    public void setProfileContextStrategy(
            @Nullable final Function<AttributeResolutionContext, ProfileRequestContext> strategy) {
        profileContextStrategy = strategy;
    }

    /**
     * Bean getter in support of {@link AbstractResolverPlugin#setProfileContextStrategy(Function)}.
     * 
     * @return The value to be set
     */
    public Function<AttributeResolutionContext, ProfileRequestContext> getProfileContextStrategy() {
        return profileContextStrategy;
    }

    /**
     * Bean setter in support of {@link AbstractResolverPlugin#setActivationCondition(Predicate)}.
     * 
     * @param pred what to set
     */
    public void setActivationCondition(@Nullable final Predicate<ProfileRequestContext> pred) {
        activationCondition = pred;
    }

    /**
     * Bean getter in support of {@link AbstractResolverPlugin#setActivationCondition(Predicate)}.
     * 
     * @return The value to be set
     */
    @Nullable public Predicate<ProfileRequestContext> getActivationCondition() {
        return activationCondition;
    }

    /**
     * Bean setter in support of {@link AbstractResolverPlugin#setDependencies(Set)}.
     * 
     * @param pluginDependencies value to set
     */
    public void setDependencies(@Nullable final Set<ResolverPluginDependency> pluginDependencies) {

        dependencies = pluginDependencies;
    }

    /**
     * Bean getter in support of {@link AbstractResolverPlugin#setActivationCondition(Predicate)}.
     * 
     * @return The value to be set
     */
    @Nullable public Set<ResolverPluginDependency> getDependencies() {
        return dependencies;
    }

    /** 
     * Set the locally define values into the object under construction.
     * @param what the object being built.
     */
    protected void setValues(@Nonnull final T what) {   
        if (null != getId()) {
            what.setId(getId());
        }
        if (null != getActivationCondition()) {
            what.setActivationCondition(getActivationCondition());
        }
        if (null != getDependencies()) {
            what.setDependencies(getDependencies());
        }
        if (null != getProfileContextStrategy()) {
            what.setProfileContextStrategy(getProfileContextStrategy());
        }
        if (null != getPropagateResolutionExceptions()) {
            what.setPropagateResolutionExceptions(getPropagateResolutionExceptions());
        }
    }
    
}