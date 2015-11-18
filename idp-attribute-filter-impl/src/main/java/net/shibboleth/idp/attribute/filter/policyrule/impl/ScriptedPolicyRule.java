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

package net.shibboleth.idp.attribute.filter.policyrule.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import org.opensaml.messaging.context.navigate.ParentContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * A {@link net.shibboleth.idp.attribute.filter.PolicyRequirementRule} that delegates to a JSR-223 script for its actual
 * processing.
 * 
 */
@ThreadSafe
public class ScriptedPolicyRule extends AbstractIdentifiableInitializableComponent implements PolicyRequirementRule,
        UnmodifiableComponent {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ScriptedPolicyRule.class);

    /** Script to be evaluated. */
    @Nonnull private EvaluableScript script;

    /** Log prefix. */
    private String logPrefix;

    /** Strategy used to locate the {@link ProfileRequestContext} to use. */
    @Nonnull private Function<AttributeFilterContext, ProfileRequestContext> prcLookupStrategy;

    /** The custom object we inject into all scripts. */
    @Nullable private Object customObject;

    /**
     * Constructor.
     * 
     * @param matchingScript script used to determine matching attribute values
     */
    public ScriptedPolicyRule(@Nonnull final EvaluableScript matchingScript) {
        setScript(matchingScript);
        // Defaults to ProfileRequestContext -> RelyingPartyContext -> AttributeContext.
        prcLookupStrategy =
                Functions.compose(new ParentContextLookup<RelyingPartyContext, ProfileRequestContext>(),
                        new ParentContextLookup<AttributeFilterContext, RelyingPartyContext>());
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
    @Nonnull public EvaluableScript getScript() {
        return script;
    }

    /**
     * Sets the script to be evaluated.
     * 
     * @param matcherScript the script to be evaluated
     */
    protected void setScript(@Nonnull final EvaluableScript matcherScript) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        script = Constraint.isNotNull(matcherScript, "Attribute value matching script can not be null");
    }

    /**
     * Set the strategy used to locate the {@link ProfileRequestContext} associated with a given
     * {@link AttributeFilterContext}.
     * 
     * @param strategy strategy used to locate the {@link ProfileRequestContext} associated with a given
     *            {@link AttributeFilterContext}
     */
    public void setProfileRequestContextLookupStrategy(
            @Nonnull final Function<AttributeFilterContext, ProfileRequestContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        prcLookupStrategy = Constraint.isNotNull(strategy, "ProfileRequestContext lookup strategy cannot be null");
    }

    /**
     * Calculate the PolicyRule.
     * <p>
     * When the script is evaluated, the following property will be available via the {@link ScriptContext}:
     * <ul>
     * <li><code>filterContext</code> - the current instance of {@link AttributeFilterContext}</li>
     * </ul>
     * The script <strong>MUST</strong> return a {@link java.lang.Boolean}
     * </p>
     * {@inheritDoc}
     */
    @Override public Tristate matches(@Nonnull final AttributeFilterContext filterContext) {
        Constraint.isNotNull(filterContext, "Attribute filter context can not be null");

        final EvaluableScript currentScript = script;
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        final SimpleScriptContext scriptContext = new SimpleScriptContext();
        scriptContext.setAttribute("filterContext", filterContext, ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute("custom", getCustomObject(), ScriptContext.ENGINE_SCOPE);
        final ProfileRequestContext prc = prcLookupStrategy.apply(filterContext);
        if (null == prc) {
            log.error("{} Could not locate ProfileRequestContext", getLogPrefix());
        }
        scriptContext.setAttribute("profileContext", prc, ScriptContext.ENGINE_SCOPE);

        try {
            final Object result = currentScript.eval(scriptContext);
            if (null == result) {
                log.error("{} Matcher script did not return a result", getLogPrefix());
                return Tristate.FAIL;
            }

            if (result instanceof Boolean) {
                if (((Boolean) result).booleanValue()) {
                    return Tristate.TRUE;
                }
                return Tristate.FALSE;
            } else {
                log.error("{} Matcher script returned a {}, not a java.lang.Boolean", getLogPrefix(), result.getClass()
                        .toString());
                return Tristate.FAIL;
            }
        } catch (ScriptException e) {
            log.error("{} Error while executing value matching script", getLogPrefix(), e);
            return Tristate.FAIL;
        }
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        // clear cached name
        logPrefix = null;

        if (null == script) {
            // never met so long as we have the assert in the constructor
            throw new ComponentInitializationException("No script has been provided");
        }
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ScriptedPolicyRule)) {
            return false;
        }

        ScriptedPolicyRule other = (ScriptedPolicyRule) obj;

        return script.equals(other.getScript());
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return Objects.hashCode(script, getId());
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("Script", getScript()).toString();
    }

    /**
     * return a string which is to be prepended to all log messages.
     * 
     * @return "Scripted Attribute Filter '<filterID>' :"
     */
    protected String getLogPrefix() {
        // local cache of cached entry to allow unsynchronised clearing.
        String prefix = logPrefix;
        if (null == prefix) {
            StringBuilder builder = new StringBuilder("Scripted Attribute Filter '").append(getId()).append("':");
            prefix = builder.toString();
            if (null == logPrefix) {
                logPrefix = prefix;
            }
        }
        return prefix;
    }
}