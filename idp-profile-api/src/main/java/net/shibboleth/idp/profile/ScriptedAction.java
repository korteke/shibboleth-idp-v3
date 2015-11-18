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

package net.shibboleth.idp.profile;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * An action which calls out to a supplied script.
 * 
 * <p>
 * The return value must be an event ID to signal. As this is a generic wrapper, the action may return any event
 * depending on the context of the activity, and may manipulate the profile context tree as required.
 * </p>
 * 
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 */
public class ScriptedAction extends AbstractProfileAction {

    /** The default language is Javascript. */
    @Nonnull @NotEmpty public static final String DEFAULT_ENGINE = "JavaScript";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ScriptedAction.class);

    /** The script we care about. */
    @Nonnull private final EvaluableScript script;

    /** Debugging info. */
    @Nullable private final String logPrefix;

    /** The custom object we can inject. */
    @Nullable private Object customObject;

    /**
     * Constructor.
     * 
     * @param theScript the script we will evaluate.
     * @param extraInfo debugging information.
     */
    public ScriptedAction(@Nonnull final EvaluableScript theScript, @Nullable final String extraInfo) {
        script = Constraint.isNotNull(theScript, "Supplied script should not be null");
        logPrefix = "Scripted Action from " + extraInfo + " :";
    }

    /**
     * Constructor.
     * 
     * @param theScript the script we will evaluate.
     */
    public ScriptedAction(@Nonnull final EvaluableScript theScript) {
        script = Constraint.isNotNull(theScript, "Supplied script should not be null");
        logPrefix = "Anonymous Scripted Action :";
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

    /** {@inheritDoc} */
    @Override public void doExecute(@Nullable final ProfileRequestContext profileContext) {
        final SimpleScriptContext scriptContext = new SimpleScriptContext();
        scriptContext.setAttribute("profileContext", profileContext, ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute("custom", getCustomObject(), ScriptContext.ENGINE_SCOPE);

        try {
            final Object result = script.eval(scriptContext);
            if (null == result) {
                ActionSupport.buildProceedEvent(profileContext);
                return;
            } else if (result instanceof String) {
                log.debug("{} signaled Event: {}", logPrefix, result);
                ActionSupport.buildEvent(profileContext, (String) result);
            } else {
                log.error("{} returned a {}, not a java.lang.String", logPrefix, result.getClass().toString());
                ActionSupport.buildEvent(profileContext, EventIds.INVALID_PROFILE_CTX);
            }
        } catch (final ScriptException e) {
            log.error("{} Error while executing Action script", logPrefix, e);
            ActionSupport.buildEvent(profileContext, EventIds.INVALID_PROFILE_CTX);
        }
    }

    /**
     * Factory to create {@link ScriptedAction} from a {@link Resource}.
     * 
     * @param resource the resource to look at
     * @param engineName the language
     * @return the predicate
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static ScriptedAction resourceScript(@Nonnull @NotEmpty final String engineName, @Nonnull final Resource resource)
            throws ScriptException, IOException {
        EvaluableScript script = new EvaluableScript(engineName, resource.getFile());
        return new ScriptedAction(script, resource.getDescription());
    }

    /**
     * Factory to create {@link ScriptedAction} from a {@link Resource}.
     * 
     * @param resource the resource to look at
     * @return the predicate
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static ScriptedAction resourceScript(@Nonnull final Resource resource) throws ScriptException, IOException {
        return resourceScript(DEFAULT_ENGINE, resource);
    }

    /**
     * Factory to create {@link ScriptedAction} from inline data.
     * 
     * @param scriptSource the script, as a string
     * @param engineName the language
     * @return the predicate
     * @throws ScriptException if the compile fails
     */
    static ScriptedAction inlineScript(@Nonnull @NotEmpty final String engineName,
            @Nonnull @NotEmpty final String scriptSource) throws ScriptException {
        EvaluableScript script = new EvaluableScript(engineName, scriptSource);
        return new ScriptedAction(script, "Inline");
    }

    /**
     * Factory to create {@link ScriptedAction} from inline data.
     * 
     * @param scriptSource the script, as a string
     * @return the predicate
     * @throws ScriptException if the compile fails
     */
    static ScriptedAction inlineScript(@Nonnull @NotEmpty final String scriptSource) throws ScriptException {
        EvaluableScript script = new EvaluableScript(DEFAULT_ENGINE, scriptSource);
        return new ScriptedAction(script, "Inline");
    }

}