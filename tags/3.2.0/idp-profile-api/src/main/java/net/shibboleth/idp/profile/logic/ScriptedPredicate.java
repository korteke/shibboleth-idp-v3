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

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.google.common.base.Predicate;

/**
 * A {@link Predicate} which calls out to a supplied script.
 */
public class ScriptedPredicate implements Predicate<ProfileRequestContext> {

    /** The default language is Javascript. */
    public static final String DEFAULT_ENGINE = "JavaScript";

    /** log. */
    private final Logger log = LoggerFactory.getLogger(ScriptedPredicate.class);

    /** The script we care about. */
    @Nonnull private final EvaluableScript script;

    /** Debugging info. */
    @Nullable private final String logPrefix;

    /** A custom object to inject into the script. */
    @Nullable private Object customObject;

    /**
     * Constructor.
     * 
     * @param theScript the script we will evaluate.
     * @param extraInfo debugging information.
     */
    public ScriptedPredicate(@Nonnull EvaluableScript theScript, @Nullable String extraInfo) {
        script = Constraint.isNotNull(theScript, "Supplied script should not be null");
        logPrefix = "Scripted Predicate from " + extraInfo + " :";
    }

    /**
     * Constructor.
     * 
     * @param theScript the script we will evaluate.
     */
    public ScriptedPredicate(@Nonnull EvaluableScript theScript) {
        script = Constraint.isNotNull(theScript, "Supplied script should not be null");
        logPrefix = "Anonymous Scripted Predicate :";
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
    public void setCustomObject(Object object) {
        customObject = object;
    }

    /** {@inheritDoc} */
    @Override public boolean apply(@Nullable ProfileRequestContext profileContext) {
        final SimpleScriptContext scriptContext = new SimpleScriptContext();
        scriptContext.setAttribute("profileContext", profileContext, ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute("custom", getCustomObject(), ScriptContext.ENGINE_SCOPE);

        try {
            final Object result = script.eval(scriptContext);
            if (null == result) {
                log.error("{} No result returned", logPrefix);
                return false;
            }

            if (result instanceof Boolean) {
                log.debug("{} returned {}", logPrefix, result);
                return ((Boolean) result).booleanValue();
            } else {
                log.error("{} returned a {}, not a java.lang.Boolean", logPrefix, result.getClass().toString());
                return false;
            }
        } catch (ScriptException e) {
            log.error("{} Error while executing Predicate script", logPrefix, e);
            return false;
        }
    }

    /**
     * Factory to create {@link ScriptedPredicate} from a {@link Resource}.
     * 
     * @param resource the resource to look at
     * @param engineName the language
     * @return the predicate
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static ScriptedPredicate resourceScript(@Nonnull @NotEmpty String engineName, @Nonnull Resource resource)
            throws ScriptException, IOException {
        EvaluableScript script = new EvaluableScript(engineName, resource.getFile());
        return new ScriptedPredicate(script, resource.getDescription());
    }

    /**
     * Factory to create {@link ScriptedPredicate} from a {@link Resource}.
     * 
     * @param resource the resource to look at
     * @return the predicate
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static ScriptedPredicate resourceScript(Resource resource) throws ScriptException, IOException {
        return resourceScript(DEFAULT_ENGINE, resource);
    }

    /**
     * Factory to create {@link ScriptedPredicate} from inline data.
     * 
     * @param scriptSource the script, as a string
     * @param engineName the language
     * @return the predicate
     * @throws ScriptException if the compile fails
     */
    static ScriptedPredicate inlineScript(@Nonnull @NotEmpty String engineName, @Nonnull @NotEmpty String scriptSource)
            throws ScriptException {
        EvaluableScript script = new EvaluableScript(engineName, scriptSource);
        return new ScriptedPredicate(script, "Inline");
    }

    /**
     * Factory to create {@link ScriptedPredicate} from inline data.
     * 
     * @param scriptSource the script, as a string
     * @return the predicate
     * @throws ScriptException if the compile fails
     */
    static ScriptedPredicate inlineScript(@Nonnull @NotEmpty String scriptSource) throws ScriptException {
        EvaluableScript script = new EvaluableScript(DEFAULT_ENGINE, scriptSource);
        return new ScriptedPredicate(script, "Inline");
    }

}
