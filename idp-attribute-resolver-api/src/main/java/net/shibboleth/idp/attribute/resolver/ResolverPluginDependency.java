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

package net.shibboleth.idp.attribute.resolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Represents the dependency of one {@link ResolverPlugin} upon another plugin. Note that this serves for
 * dependencies both on data connectors (in which case the {@link #dependencyAttributeId} member will be null) or
 * attributes. The dependency analysis stages is aware of this difference and relies on it.
 */
@ThreadSafe
public final class ResolverPluginDependency {

    /** ID of the plugin that will produce the attribute. */
    private final String dependencyPluginId;

    /** ID of the attribute, produced by the identified plugin, whose values will be used by the dependent plugin. */
    private String dependencyAttributeId;

    /**
     * Constructor.
     * 
     * @param pluginId ID of the plugin that will produce the attribute, never null or empty
     */
    public ResolverPluginDependency(@Nonnull @NotEmpty final String pluginId) {
        dependencyPluginId =
                Constraint.isNotNull(StringSupport.trimOrNull(pluginId),
                        "Dependency plugin ID may not be null or empty");
    }

    /**
     * Gets the ID of the plugin that will produce the attribute.
     * 
     * @return ID of the plugin that will produce the attribute, never null or empty
     */
    @Nonnull public String getDependencyPluginId() {
        return dependencyPluginId;
    }

    /**
     * Set the attributeId.
     * 
     * @param attributeId ID of the attribute, produced by the identified plugin, whose values will be used by the
     *            dependent plugin
     */
    public void setDependencyAttributeId(@Nullable String attributeId) {
        dependencyAttributeId = StringSupport.trimOrNull(attributeId);
    }

    /**
     * Gets the ID of the attribute, produced by the identified plugin, whose values will be used by the dependent
     * plugin.
     * 
     * @return ID of the attribute, produced by the identified plugin, whose values will be used by the dependent
     *         plugin, never null or empty
     */
    @Nullable public String getDependencyAttributeId() {
        return dependencyAttributeId;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return Objects.hashCode(dependencyPluginId, dependencyAttributeId);
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        ResolverPluginDependency other = (ResolverPluginDependency) obj;
        if (java.util.Objects.equals(getDependencyPluginId(), other.getDependencyPluginId())
                && java.util.Objects.equals(getDependencyAttributeId(), other.getDependencyAttributeId())) {
            return true;
        }

        return false;
    }

    /** {@inheritDoc} */
    public String toString() {
        return MoreObjects.toStringHelper(this).add("pluginId", dependencyPluginId)
                .add("attributeId", dependencyAttributeId).toString();
    }
}