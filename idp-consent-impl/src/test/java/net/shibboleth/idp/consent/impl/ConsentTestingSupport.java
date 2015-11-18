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

package net.shibboleth.idp.consent.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.consent.storage.impl.ConsentResult;

/**
 * Helper methods for creating test objects for consent action tests.
 */
public class ConsentTestingSupport {

    public static Map<String, Consent> newConsentMap() {
        final Consent consent1 = new Consent();
        consent1.setId("consent1");
        consent1.setValue("value1");

        final Consent consent2 = new Consent();
        consent2.setId("consent2");
        consent2.setValue("value2");

        final Map<String, Consent> map = new HashMap<>();
        map.put(consent1.getId(), consent1);
        map.put(consent2.getId(), consent2);
        return map;
    }

    public static final Map<String, IdPAttribute> newAttributeMap() {
        final IdPAttributeValue<?> value1 = new StringAttributeValue("value1");
        final IdPAttributeValue<?> value2 = new StringAttributeValue("value2");
        final IdPAttributeValue<?> value3 = new StringAttributeValue("value3");

        final IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Collections.singleton(value1));

        final IdPAttribute attribute2 = new IdPAttribute("attribute2");
        attribute2.setValues(Arrays.asList(value1, value2));

        final IdPAttribute attribute3 = new IdPAttribute("attribute3");
        attribute3.setValues(Collections.singleton(value3));

        final Map<String, IdPAttribute> map = new HashMap<>();
        map.put(attribute1.getId(), attribute1);
        map.put(attribute2.getId(), attribute2);
        map.put(attribute3.getId(), attribute3);
        return map;
    }

    public static final List<ConsentResult> newConsentResults() {
        final List<ConsentResult> consentResults = new ArrayList<>();
        consentResults.add(new ConsentResult("context1", "key1", "value1", null));
        consentResults.add(new ConsentResult("context2", "key1", "value1", null));
        consentResults.add(new ConsentResult("context2", "key2", "value2", null));
        return consentResults;
    }

    public static Map<String, Integer> newSymbolicsMap() {
        final Map<String, Integer> map = new HashMap<>();
        map.put("consent1", 101);
        map.put("consent2", 102);
        map.put("attribute1", 201);
        map.put("attribute2", 202);
        map.put("attribute3", 203);
        return map;
    }
}
