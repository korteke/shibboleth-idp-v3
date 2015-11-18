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

package net.shibboleth.idp.authn.impl;

import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;

/** {@link SelectSubjectCanonicalizationFlow} unit test. */
public class SelectSubjectCanonicalizationFlowTest extends PopulateSubjectCanonicalizationContextTest {
    
    private SelectSubjectCanonicalizationFlow action;
    
    private SubjectCanonicalizationContext c14nCtx;
    
    @BeforeMethod public void setUp() throws Exception {
        super.setUp();
        
        action = new SelectSubjectCanonicalizationFlow();
        action.initialize();

        c14nCtx = prc.getSubcontext(SubjectCanonicalizationContext.class, false);
    }
    
    @Test public void testSelect() {
        
        final Event event = action.execute(src);
        
        Assert.assertEquals(c14nCtx.getAttemptedFlow(), c14nCtx.getPotentialFlows().get(event.getId()));
        Assert.assertEquals(c14nCtx.getAttemptedFlow().getId(), "test1");
    }

    @Test public void testIntermediate() {
        c14nCtx.getIntermediateFlows().put("test1", c14nCtx.getPotentialFlows().get("test1"));
        
        final Event event = action.execute(src);
        
        Assert.assertEquals(c14nCtx.getAttemptedFlow(), c14nCtx.getPotentialFlows().get(event.getId()));
        Assert.assertEquals(c14nCtx.getAttemptedFlow().getId(), "test2");
    }

    @Test public void testPredicate() {
        c14nCtx.getPotentialFlows().get("test1").setActivationCondition(Predicates.<ProfileRequestContext>alwaysFalse());
        
        final Event event = action.execute(src);
        
        Assert.assertEquals(c14nCtx.getAttemptedFlow(), c14nCtx.getPotentialFlows().get(event.getId()));
        Assert.assertEquals(c14nCtx.getAttemptedFlow().getId(), "test2");
    }

}