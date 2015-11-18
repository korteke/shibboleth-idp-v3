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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

/** test for {@link LocalizedStringAttributeValue}.  Derived from coverage info.
 *
 */
public class LocalizedStringAttributeValueTest {

    @Test public void localizedStringAttributeValue() {
        Set foo = new HashSet();
        
        LocalizedStringAttributeValue val = new LocalizedStringAttributeValue("for", new Locale("en"));
        
        foo.add(val);
        foo.add(null);
        foo.add(val);
        foo.add(new LocalizedStringAttributeValue("for", new Locale("en")));
        foo.add(new LocalizedStringAttributeValue("for", new Locale("fr")));
        Assert.assertEquals(val.getValueLocale().getLanguage(), "en");
        Assert.assertFalse(val.equals(null));
        Assert.assertTrue(val.equals(val));
        Assert.assertFalse(val.equals(new Integer(2)));
    }
}
