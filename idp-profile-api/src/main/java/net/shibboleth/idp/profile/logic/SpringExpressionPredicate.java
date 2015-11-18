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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.common.base.Predicate;

/**
 * Predicate that can be used as an "activationCondition" for an authentication flow.
 * The condition is defined by an Spring EL expression.
 * The expression can use the built-in #ipRange function and any variable
 * that is included in expressionVariables during construction
 * of this object.
 * 
 * @author Daniel Lutz
 *
 */
public class SpringExpressionPredicate implements Predicate<ProfileRequestContext<?,?>> {
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SpringExpressionPredicate.class);

    /** SpEL expression to evaluate. */
    @Nullable private String springExpression;
    
    /** A custom object to inject into the expression context. */
    @Nullable private Object customObject;

    /**
     * Constructor.
     *
     * @param expression the expression to evaluate
     */
    public SpringExpressionPredicate(@Nonnull @NotEmpty final String expression) {
        springExpression = Constraint.isNotNull(StringSupport.trimOrNull(expression),
                "Expression cannot be null or empty");
    }


    /**
     * Set a custom (externally provided) object.
     * 
     * @param object the custom object
     */
    public void setCustomObject(@Nullable final Object object) {
        customObject = object;
    }

    /** {@inheritDoc} */
    @Override
    public boolean apply(@Nullable final ProfileRequestContext<?,?> input) {

        try {
            final ExpressionParser parser = new SpelExpressionParser();
            final StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("custom", customObject);
            context.setVariable("profileContext", input);
            
            return parser.parseExpression(springExpression).getValue(context, Boolean.class);
        } catch (final ParseException|EvaluationException e) {
            log.error("Error evaluating Spring expression", e);
            return false;
        }
    }

}