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

package net.shibboleth.idp.attribute;

import java.util.Arrays;
import java.util.Collections;

import net.shibboleth.idp.attribute.context.AttributeContext;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link AttributeContext} class. */

public class AttributeContextTest {
    
    /** 
     * Test that the attributes from the supplied context cannot be modified
     * and that there as many as we expected. 
     */
    private void contextAttributes( AttributeContext context, int expectedSize) {
        Assert.assertEquals(context.getIdPAttributes().size(), expectedSize);
        try {
            context.getIdPAttributes().put("attr", new IdPAttribute("attr") );
            Assert.fail();
        } catch (UnsupportedOperationException e) {

        }
    }
    
    @Test public void attributeContext() {
        AttributeContext context = new AttributeContext();
        
        context.setIdPAttributes(Arrays.asList((IdPAttribute)null, null));
        contextAttributes(context, 0);

        context.setIdPAttributes(Arrays.asList(new IdPAttribute("foo"), null));
        contextAttributes(context, 1);
        
        context.setIdPAttributes(null);
        contextAttributes(context, 0);
        
        context.setIdPAttributes(Collections.EMPTY_SET);
        contextAttributes(context, 0);
    }

}
