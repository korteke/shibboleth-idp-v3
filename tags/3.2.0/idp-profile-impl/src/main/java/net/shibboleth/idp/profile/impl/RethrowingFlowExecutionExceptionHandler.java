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

package net.shibboleth.idp.profile.impl;

import org.springframework.webflow.engine.FlowExecutionExceptionHandler;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.execution.FlowExecutionException;

/**
 * This handler can be attached to view or end states that are used to respond to errors,
 * including RuntimeExceptions, so that if they themselves raise another RuntimeException,
 * it won't trigger the state again, but just fail the flow.
 */
public class RethrowingFlowExecutionExceptionHandler implements FlowExecutionExceptionHandler {

    /** {@inheritDoc} */
    @Override
    public boolean canHandle(FlowExecutionException exception) {
        return exception.getCause() instanceof RuntimeException;
    }

    /** {@inheritDoc} */
    @Override
    public void handle(FlowExecutionException exception, RequestControlContext context) {
        throw new RuntimeException(exception.getCause());
    }

}