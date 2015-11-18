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

package net.shibboleth.idp.profile.context.navigate;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.google.common.base.Function;

/**
 * A {@link Function} over a {@link BaseContext} which calls out to a supplied script.
 * 
 * @param <T> The specific type of context (either {@link ProfileRequestContext} or {@link MessageContext})
 */
public class ScriptedContextLookupFunction<T extends BaseContext> implements Function<T, Object> {

    /** The default language is Javascript. */
    @Nonnull @NotEmpty public static final String DEFAULT_ENGINE = "JavaScript";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ScriptedContextLookupFunction.class);

    /** The script we care about. */
    @Nonnull private final EvaluableScript script;

    /** Debugging info. */
    @Nullable private final String logPrefix;

    /** What class we want the output to test against. */
    @Nullable private Class outputClass;

    /** What class we want the output to test against. */
    @Nonnull private final Class<T> inputClass;

    /** The custom object we can be injected into the script. */
    @Nullable private Object customObject;

    /**
     * Constructor.
     * 
     * @param inClass the class we accept as input.
     * @param theScript the script we will evaluate.
     * @param extraInfo debugging information.
     */
    protected ScriptedContextLookupFunction(@Nonnull Class<T> inClass, @Nonnull EvaluableScript theScript,
            @Nullable String extraInfo) {
        inputClass = Constraint.isNotNull(inClass, "Supplied inputClass cannot be null");
        script = Constraint.isNotNull(theScript, "Supplied script cannot be null");
        logPrefix = "Scripted Function from " + extraInfo + ":";
    }

    /**
     * Constructor.
     * 
     * @param inClass the class we accept as input.
     * @param theScript the script we will evaluate.
     */
    protected ScriptedContextLookupFunction(@Nonnull Class<T> inClass, @Nonnull EvaluableScript theScript) {
        inputClass = Constraint.isNotNull(inClass, "Supplied inputClass cannot be null");
        script = Constraint.isNotNull(theScript, "Supplied script should not be null");
        logPrefix = "Anonymous Scripted Function:";
    }

    /**
     * Constructor.
     * 
     * @param inClass the class we accept as input.
     * @param theScript the script we will evaluate.
     * @param extraInfo debugging information.
     * @param outputType the type to test against.
     */
    protected ScriptedContextLookupFunction(@Nonnull Class<T> inClass, @Nonnull EvaluableScript theScript,
            @Nullable String extraInfo, @Nullable Class outputType) {
        this(inClass, theScript, extraInfo);
        outputClass = outputType;
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
    @Override public Object apply(@Nullable T context) {

        if (null != context && !inputClass.isInstance(context)) {
            throw new ClassCastException(logPrefix + " Input was type " + context.getClass()
                    + " which is not an instance of " + inputClass);
        }

        final SimpleScriptContext scriptContext = new SimpleScriptContext();
        scriptContext.setAttribute("profileContext", context, ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute("custom", getCustomObject(), ScriptContext.ENGINE_SCOPE);

        try {
            Object output = script.eval(scriptContext);
            if (null != outputClass && null != output && !outputClass.isInstance(output)) {
                log.error("{} Output of type {} was not of type {}", logPrefix, output.getClass(), outputClass);
                return null;
            }
            return output;

        } catch (final ScriptException e) {
            log.error("{} Error while executing Function script", logPrefix, e);
            return null;
        }
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link ProfileRequestContext}s from a
     * {@link Resource}.
     * 
     * @param resource the resource to look at
     * @param engineName the language
     * @return the function
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static ScriptedContextLookupFunction<ProfileRequestContext> resourceScript(@Nonnull @NotEmpty String engineName,
            @Nonnull Resource resource) throws ScriptException, IOException {
        return resourceScript(engineName, resource, null);
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link ProfileRequestContext}s from a
     * {@link Resource}.
     * 
     * @param resource the resource to look at
     * @param engineName the language
     * @param outputType the type to test against.
     * @return the function
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static ScriptedContextLookupFunction<ProfileRequestContext> resourceScript(@Nonnull @NotEmpty String engineName,
            @Nonnull Resource resource, @Nullable Class outputType) throws ScriptException, IOException {
        final EvaluableScript script = new EvaluableScript(engineName, resource.getFile());
        return new ScriptedContextLookupFunction(ProfileRequestContext.class, script, resource.getDescription(),
                outputType);
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} from a {@link Resource}.
     * 
     * @param resource the resource to look at
     * @return the function
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static ScriptedContextLookupFunction<ProfileRequestContext> resourceScript(Resource resource)
            throws ScriptException, IOException {
        return resourceScript(DEFAULT_ENGINE, resource, null);
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link ProfileRequestContext}s from a
     * {@link Resource}.
     * 
     * @param resource the resource to look at
     * @param outputType the type to test against.
     * @return the function
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static ScriptedContextLookupFunction<ProfileRequestContext> resourceScript(Resource resource,
            @Nullable Class outputType) throws ScriptException, IOException {
        return resourceScript(DEFAULT_ENGINE, resource, outputType);
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link ProfileRequestContext}s from inline data.
     * 
     * @param scriptSource the script, as a string
     * @param engineName the language
     * @return the function
     * @throws ScriptException if the compile fails
     */
    static ScriptedContextLookupFunction<ProfileRequestContext> inlineScript(@Nonnull @NotEmpty String engineName,
            @Nonnull @NotEmpty String scriptSource) throws ScriptException {
        final EvaluableScript script = new EvaluableScript(engineName, scriptSource);
        return new ScriptedContextLookupFunction(ProfileRequestContext.class, script, "Inline");
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link ProfileRequestContext}s from inline data.
     * 
     * @param scriptSource the script, as a string
     * @param engineName the language
     * @param outputType the type to test against.
     * @return the function
     * @throws ScriptException if the compile fails
     */
    static ScriptedContextLookupFunction<ProfileRequestContext> inlineScript(@Nonnull @NotEmpty String engineName,
            @Nonnull @NotEmpty String scriptSource, @Nullable Class outputType) throws ScriptException {
        final EvaluableScript script = new EvaluableScript(engineName, scriptSource);
        return new ScriptedContextLookupFunction(ProfileRequestContext.class, script, "Inline", outputType);
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link ProfileRequestContext}s from inline data.
     * 
     * @param scriptSource the script, as a string
     * @return the function
     * @throws ScriptException if the compile fails
     */
    static ScriptedContextLookupFunction<ProfileRequestContext> inlineScript(@Nonnull @NotEmpty String scriptSource)
            throws ScriptException {
        final EvaluableScript script = new EvaluableScript(DEFAULT_ENGINE, scriptSource);
        return new ScriptedContextLookupFunction(ProfileRequestContext.class, script, "Inline");
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link ProfileRequestContext}s from inline data.
     * 
     * @param scriptSource the script, as a string
     * @param outputType the type to test against.
     * @return the function
     * @throws ScriptException if the compile fails
     */
    static ScriptedContextLookupFunction<ProfileRequestContext> inlineScript(@Nonnull @NotEmpty String scriptSource,
            @Nullable Class outputType) throws ScriptException {
        final EvaluableScript script = new EvaluableScript(DEFAULT_ENGINE, scriptSource);
        return new ScriptedContextLookupFunction(ProfileRequestContext.class, script, "Inline", outputType);
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link MessageContext}s from a {@link Resource}.
     * 
     * @param resource the resource to look at
     * @param engineName the language
     * @return the function
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static ScriptedContextLookupFunction<MessageContext> resourceMessageContextScript(
            @Nonnull @NotEmpty String engineName, @Nonnull Resource resource) throws ScriptException, IOException {
        return resourceMessageContextScript(engineName, resource, null);
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link MessageContext}s from a {@link Resource}.
     * 
     * @param resource the resource to look at
     * @param engineName the language
     * @param outputType the type to test against.
     * @return the function
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static ScriptedContextLookupFunction<MessageContext> resourceMessageContextScript(
            @Nonnull @NotEmpty String engineName, @Nonnull Resource resource, @Nullable Class outputType)
            throws ScriptException, IOException {
        final EvaluableScript script = new EvaluableScript(engineName, resource.getFile());
        return new ScriptedContextLookupFunction(MessageContext.class, script, resource.getDescription(), outputType);
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link MessageContext}s from a {@link Resource}.
     * 
     * @param resource the resource to look at
     * @return the function
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static ScriptedContextLookupFunction resourceMessageContextScript(Resource resource) throws ScriptException,
            IOException {
        return resourceMessageContextScript(DEFAULT_ENGINE, resource, null);
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link MessageContext}s from a {@link Resource}.
     * 
     * @param resource the resource to look at
     * @param outputType the type to test against.
     * @return the function
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    static ScriptedContextLookupFunction<MessageContext> resourceMessageContextScript(Resource resource,
            @Nullable Class outputType) throws ScriptException, IOException {
        return resourceMessageContextScript(DEFAULT_ENGINE, resource, outputType);
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link MessageContext}s from inline data.
     * 
     * @param scriptSource the script, as a string
     * @param engineName the language
     * @return the function
     * @throws ScriptException if the compile fails
     */
    static ScriptedContextLookupFunction<MessageContext> inlineMessageContextScript(
            @Nonnull @NotEmpty String engineName, @Nonnull @NotEmpty String scriptSource) throws ScriptException {
        final EvaluableScript script = new EvaluableScript(engineName, scriptSource);
        return new ScriptedContextLookupFunction(MessageContext.class, script, "Inline");
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link MessageContext}s from inline data.
     * 
     * @param scriptSource the script, as a string
     * @param engineName the language
     * @param outputType the type to test against.
     * @return the function
     * @throws ScriptException if the compile fails
     */
    static ScriptedContextLookupFunction<MessageContext> inlineMessageContextScript(
            @Nonnull @NotEmpty String engineName, @Nonnull @NotEmpty String scriptSource, @Nullable Class outputType)
            throws ScriptException {
        final EvaluableScript script = new EvaluableScript(engineName, scriptSource);
        return new ScriptedContextLookupFunction(MessageContext.class, script, "Inline", outputType);
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link MessageContext}s from inline data.
     * 
     * @param scriptSource the script, aMessageContexts a string
     * @return the function
     * @throws ScriptException if the compile fails
     */
    static ScriptedContextLookupFunction<MessageContext> inlineMessageContextScript(
            @Nonnull @NotEmpty String scriptSource) throws ScriptException {
        final EvaluableScript script = new EvaluableScript(DEFAULT_ENGINE, scriptSource);
        return new ScriptedContextLookupFunction(MessageContext.class, script, "Inline");
    }

    /**
     * Factory to create {@link ScriptedContextLookupFunction} for {@link MessageContext}s from inline data.
     * 
     * @param scriptSource the script, as a string
     * @param outputType the type to test against.
     * @return the function
     * @throws ScriptException if the compile fails
     */
    static ScriptedContextLookupFunction<MessageContext> inlineMessageContextScript(
            @Nonnull @NotEmpty String scriptSource, @Nullable Class outputType) throws ScriptException {
        final EvaluableScript script = new EvaluableScript(DEFAULT_ENGINE, scriptSource);
        return new ScriptedContextLookupFunction(MessageContext.class, script, "Inline", outputType);
    }

}