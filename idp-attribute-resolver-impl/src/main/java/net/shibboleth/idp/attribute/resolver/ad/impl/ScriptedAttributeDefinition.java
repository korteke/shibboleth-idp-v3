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

package net.shibboleth.idp.attribute.resolver.ad.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.resolver.AbstractAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.PluginDependencySupport;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import org.opensaml.messaging.context.navigate.ParentContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

import edu.internet2.middleware.shibboleth.common.attribute.provider.V2SAMLProfileRequestContext;

/**
 * An {@link net.shibboleth.idp.attribute.resolver.AttributeDefinition} that executes a script in order to populate the
 * values of the generated attribute.
 * 
 * <p>
 * The evaluated script has access to the following information:
 * <ul>
 * <li>A script attribute whose name is the ID of this attribute definition and whose value is a newly constructed
 * {@link IdPAttribute}.</li>
 * <li>A script attribute whose name is <code>context</code> and whose value is the current
 * {@link AttributeResolutionContext}</li>
 * <li>A script attribute for every attribute produced by the dependencies of this attribute definition. The name of the
 * script attribute is the ID of the {@link IdPAttribute} and its value is the {@link List} of {@link IdPAttributeValue}
 * for the attribute.</li>
 * </ul>
 * </p>
 * <p>
 * The evaluated script should populate the values of the newly constructed {@link IdPAttribute} mentioned above. No
 * other information from the script will be taken in to account.
 * </p>
 */
@ThreadSafe
public class ScriptedAttributeDefinition extends AbstractAttributeDefinition {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ScriptedAttributeDefinition.class);

    /** Script to be evaluated. */
    @NonnullAfterInit private EvaluableScript script;

    /** Strategy used to locate the {@link ProfileRequestContext} to use. */
    @Nonnull private Function<AttributeResolutionContext, ProfileRequestContext> prcLookupStrategy;

    /** The custom object we inject into all scripts. */
    @Nullable private Object customObject;

    /** Constructor. */
    public ScriptedAttributeDefinition() {
        // Defaults to ProfileRequestContext -> AttributeContext.
        prcLookupStrategy = new ParentContextLookup<>();
    }

    /**
     * Return the custom (externally provided) object.
     * 
     * @return the custom object
     */
    @Nullable public Object getCustomObject() {
        return customObject;
    }

    /**
     * Set the custom (externally provided) object.
     * 
     * @param object the custom object
     */
    @Nullable public void setCustomObject(Object object) {
        customObject = object;
    }

    /**
     * Gets the script to be evaluated.
     * 
     * @return the script to be evaluated
     */
    @NonnullAfterInit public EvaluableScript getScript() {
        return script;
    }

    /**
     * Sets the script to be evaluated.
     * 
     * @param definitionScript the script to be evaluated
     */
    public void setScript(@Nonnull final EvaluableScript definitionScript) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        script = Constraint.isNotNull(definitionScript, "Attribute definition script cannot be null");
    }

    /**
     * Set the strategy used to locate the {@link ProfileRequestContext} associated with a given
     * {@link AttributeResolutionContext}.
     * 
     * @param strategy strategy used to locate the {@link ProfileRequestContext} associated with a given
     *            {@link AttributeResolutionContext}
     */
    public void setProfileRequestContextLookupStrategy(
            @Nonnull final Function<AttributeResolutionContext, ProfileRequestContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        prcLookupStrategy = Constraint.isNotNull(strategy, "ProfileRequestContext lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == script) {
            throw new ComponentInitializationException(getLogPrefix() + " no script was configured");
        }
    }

    /** {@inheritDoc} */
    @Override @Nullable protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
        Constraint.isNotNull(resolutionContext, "AttributeResolutionContext cannot be null");
        Constraint.isNotNull(workContext, "AttributeResolverWorkContext cannot be null");

        final ScriptContext context = getScriptContext(resolutionContext, workContext);

        try {
            script.eval(context);
        } catch (final ScriptException e) {
            throw new ResolutionException(getLogPrefix() + " unable to execute script", e);
        }
        final Object result = context.getAttribute(getId());

        if (null == result) {
            log.info("{} no value returned", getLogPrefix());
            return null;
        }

        if (result instanceof ScriptedIdPAttributeImpl) {

            final ScriptedIdPAttributeImpl scriptedAttribute = (ScriptedIdPAttributeImpl) result;
            return scriptedAttribute.getResultingAttribute();

        } else {

            throw new ResolutionException(getLogPrefix() + " returned variable was of wrong type ("
                    + result.getClass().toString() + ")");
        }

    }

    /**
     * Constructs the {@link ScriptContext} used when evaluating the script.
     * 
     * @param resolutionContext current resolution context
     * @param workContext current work context
     * 
     * @return constructed script context
     * @throws ResolutionException thrown if dependent data connectors or attribute definitions can not be resolved
     */
    @Nonnull private ScriptContext getScriptContext(@Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {

        final SimpleScriptContext scriptContext = new SimpleScriptContext();
        final Map<String, List<IdPAttributeValue<?>>> dependencyAttributes =
                PluginDependencySupport.getAllAttributeValues(workContext, getDependencies());

        if (dependencyAttributes.containsKey(getId())) {
            log.debug("{} to-be-populated attribute is a dependency.  Not created", getLogPrefix());
        } else {
            log.debug("{} adding to-be-populated attribute to script context", getLogPrefix());
            final IdPAttribute newAttribute = new IdPAttribute(getId());
            scriptContext.setAttribute(getId(), new ScriptedIdPAttributeImpl(newAttribute, getLogPrefix()),
                    ScriptContext.ENGINE_SCOPE);
        }

        log.debug("{} adding contexts to script context", getLogPrefix());
        scriptContext.setAttribute("resolutionContext", resolutionContext, ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute("workContext", workContext, ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute("custom", getCustomObject(), ScriptContext.ENGINE_SCOPE);
        final ProfileRequestContext prc = prcLookupStrategy.apply(resolutionContext);
        if (null == prc) {
            log.error("{} ProfileRequestContext could not be located", getLogPrefix());
        }
        scriptContext.setAttribute("profileContext", prc, ScriptContext.ENGINE_SCOPE);

        log.debug("{} adding emulated V2 request context to script context", getLogPrefix());
        scriptContext.setAttribute("requestContext", new V2SAMLProfileRequestContext(resolutionContext, getId()),
                ScriptContext.ENGINE_SCOPE);

        for (final Entry<String, List<IdPAttributeValue<?>>> dependencyAttribute : dependencyAttributes.entrySet()) {
            log.debug("{} adding dependent attribute '{}' with the following values to the script context: {}",
                    new Object[] {getLogPrefix(), dependencyAttribute.getKey(), dependencyAttribute.getValue(),});
            final IdPAttribute pseudoAttribute = new IdPAttribute(dependencyAttribute.getKey());
            pseudoAttribute.setValues(dependencyAttribute.getValue());

            scriptContext.setAttribute(dependencyAttribute.getKey(), new ScriptedIdPAttributeImpl(pseudoAttribute,
                    getLogPrefix()), ScriptContext.ENGINE_SCOPE);
        }

        return scriptContext;
    }
}