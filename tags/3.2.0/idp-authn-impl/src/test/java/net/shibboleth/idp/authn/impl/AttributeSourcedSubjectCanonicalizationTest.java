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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.security.auth.Subject;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link AttributeSourcedSubjectCanonicalization} unit test. */
public class AttributeSourcedSubjectCanonicalizationTest extends PopulateAuthenticationContextTest {
    
    private AttributeSourcedSubjectCanonicalization action; 
    
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        
        action = new AttributeSourcedSubjectCanonicalization();
        action.setAttributeSourceIds(Arrays.asList("attr1", "attr2"));
        action.initialize();
    }
    
    @Test(expectedExceptions=ComponentInitializationException.class)
    public void testNoSources() throws ComponentInitializationException {
        action = new AttributeSourcedSubjectCanonicalization();
        action.initialize();
    }
    
    @Test public void testNoContext() {
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_SUBJECT_C14N_CTX);
    }

    @Test public void testNoAttributes() {
        Subject subject = new Subject();
        prc.getSubcontext(SubjectCanonicalizationContext.class, true).setSubject(subject);
        
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_SUBJECT);
        Assert.assertNotNull(prc.getSubcontext(SubjectCanonicalizationContext.class, false).getException());
    }

    @Test public void testSuccess() {
        final Collection<? extends IdPAttributeValue<?>> values =
                Collections.singletonList(new StringAttributeValue("foo"));

        final IdPAttribute inputAttribute = new IdPAttribute("attr2");
        inputAttribute.setValues(values);
        final SubjectCanonicalizationContext sc = prc.getSubcontext(SubjectCanonicalizationContext.class, true);
        sc.setSubject(new Subject());
        sc.getSubcontext(AttributeContext.class, true).setIdPAttributes(Collections.singleton(inputAttribute));
        
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(sc.getPrincipalName(), "foo");
    }

}