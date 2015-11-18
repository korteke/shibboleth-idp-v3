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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link IdPAttribute} class. */
public class AttributeTest {

    /** Tests that the attribute has its expected state after instantiation. */
    @Test public void instantiation() {
        IdPAttribute attrib = new IdPAttribute("foo");

        Assert.assertEquals(attrib.getId(), "foo");

        Assert.assertNotNull(attrib.getDisplayDescriptions());
        Assert.assertTrue(attrib.getDisplayDescriptions().isEmpty());

        Assert.assertNotNull(attrib.getDisplayNames());
        Assert.assertTrue(attrib.getDisplayNames().isEmpty());

        Assert.assertNotNull(attrib.getEncoders());
        Assert.assertTrue(attrib.getEncoders().isEmpty());

        Assert.assertNotNull(attrib.getValues());
        Assert.assertTrue(attrib.getValues().isEmpty());

        Assert.assertNotNull(attrib.hashCode());

        Assert.assertTrue(attrib.equals(new IdPAttribute("foo")));
    }

    /** Tests that null/empty IDs aren't accepted. */
    @Test public void nullEmptyId() {
        try {
            new IdPAttribute(null);
            Assert.fail("able to create attribute with null ID");
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            new IdPAttribute("");
            Assert.fail("able to create attribute with empty ID");
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            new IdPAttribute(" ");
            Assert.fail("able to create attribute with empty ID");
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

    /** Tests that display names are properly added and modified. */
    @Test public void displayNames() {
        Locale en = new Locale("en");
        Locale enbr = new Locale("en", "br");

        IdPAttribute attrib = new IdPAttribute("foo");
        attrib.setDisplayNames(null);
        Assert.assertTrue(attrib.getDisplayNames().isEmpty());

        attrib.setDisplayNames(Collections.EMPTY_MAP);
        Assert.assertTrue(attrib.getDisplayNames().isEmpty());

        Map<Locale, String> displayNames = new HashMap<>();
        displayNames.put(null, "wibble");
        attrib.setDisplayNames(displayNames);
        Assert.assertTrue(attrib.getDisplayNames().isEmpty());

        displayNames.clear();
        displayNames.put(en, null);
        attrib.setDisplayNames(displayNames);
        Assert.assertTrue(attrib.getDisplayNames().isEmpty());

        displayNames.clear();
        // test adding one entry
        displayNames.put(en, " english ");
        attrib.setDisplayNames(displayNames);
        
        Assert.assertFalse(attrib.getDisplayNames().isEmpty());
        Assert.assertEquals(attrib.getDisplayNames().size(), 1);
        Assert.assertTrue(attrib.getDisplayNames().containsKey(en));
        Assert.assertEquals(attrib.getDisplayNames().get(en), "english");

        // test adding another entry
        displayNames.put(enbr, "british");
        displayNames.put(en, " englishX ");
        attrib.setDisplayNames(displayNames);
        Assert.assertFalse(attrib.getDisplayNames().isEmpty());
        Assert.assertEquals(attrib.getDisplayNames().size(), 2);
        Assert.assertTrue(attrib.getDisplayNames().containsKey(enbr));
        Assert.assertEquals(attrib.getDisplayNames().get(enbr), "british");
        Assert.assertEquals(attrib.getDisplayNames().get(en), "englishX");

        // test replacing an entry
        String replacedName = displayNames.put(en, "english ");
        Assert.assertEquals(replacedName, " englishX ");

        attrib.setDisplayNames(displayNames);
        Assert.assertFalse(attrib.getDisplayNames().isEmpty());
        Assert.assertEquals(attrib.getDisplayNames().size(), 2);
        Assert.assertTrue(attrib.getDisplayNames().containsKey(en));
        Assert.assertEquals(attrib.getDisplayNames().get(en), "english");

        try {
            // test removing an entry
            attrib.getDisplayNames().remove(en);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            // test removing an entry
            attrib.getDisplayNames().put(en, "foo");
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    /** Tests that display descriptions are properly added and modified. */
    @Test public void displayDescriptions() {
        Locale en = new Locale("en");
        Locale enbr = new Locale("en", "br");

        IdPAttribute attrib = new IdPAttribute("foo");
        attrib.setDisplayDescriptions(null);
        Assert.assertTrue(attrib.getDisplayNames().isEmpty());

        attrib.setDisplayNames(Collections.EMPTY_MAP);
        Assert.assertTrue(attrib.getDisplayDescriptions().isEmpty());

        Map<Locale, String> displayDescriptions = new HashMap<>();
        displayDescriptions.put(null, "wibble");
        attrib.setDisplayDescriptions(displayDescriptions);
        Assert.assertTrue(attrib.getDisplayDescriptions().isEmpty());

        displayDescriptions.clear();
        displayDescriptions.put(en, null);
        attrib.setDisplayDescriptions(displayDescriptions);
        Assert.assertTrue(attrib.getDisplayDescriptions().isEmpty());

        displayDescriptions.clear();
        // test adding one entry
        displayDescriptions.put(en, " english ");
        attrib.setDisplayDescriptions(displayDescriptions);
        
        Assert.assertFalse(attrib.getDisplayDescriptions().isEmpty());
        Assert.assertEquals(attrib.getDisplayDescriptions().size(), 1);
        Assert.assertTrue(attrib.getDisplayDescriptions().containsKey(en));
        Assert.assertEquals(attrib.getDisplayDescriptions().get(en), "english");

        // test adding another entry
        displayDescriptions.put(enbr, "british");
        displayDescriptions.put(en, " englishX ");
        attrib.setDisplayDescriptions(displayDescriptions);
        Assert.assertFalse(attrib.getDisplayDescriptions().isEmpty());
        Assert.assertEquals(attrib.getDisplayDescriptions().size(), 2);
        Assert.assertTrue(attrib.getDisplayDescriptions().containsKey(enbr));
        Assert.assertEquals(attrib.getDisplayDescriptions().get(enbr), "british");
        Assert.assertEquals(attrib.getDisplayDescriptions().get(en), "englishX");

        // test replacing an entry
        String replacedName = displayDescriptions.put(en, "english ");
        Assert.assertEquals(replacedName, " englishX ");

        attrib.setDisplayDescriptions(displayDescriptions);
        Assert.assertFalse(attrib.getDisplayDescriptions().isEmpty());
        Assert.assertEquals(attrib.getDisplayDescriptions().size(), 2);
        Assert.assertTrue(attrib.getDisplayDescriptions().containsKey(en));
        Assert.assertEquals(attrib.getDisplayDescriptions().get(en), "english");

        try {
            // test removing an entry
            attrib.getDisplayDescriptions().remove(en);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            // test removing an entry
            attrib.getDisplayDescriptions().put(en, "foo");
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    /** Tests that values are properly added and modified. */
    @Test(enabled=false) public void values() {
        LocalizedStringAttributeValue value1 = new LocalizedStringAttributeValue("value1", null);
        LocalizedStringAttributeValue value2 = new LocalizedStringAttributeValue("value2", null);

        IdPAttribute attrib = new IdPAttribute("foo");
        Assert.assertTrue(attrib.getValues().isEmpty());

        attrib.setValues(null);
        Assert.assertTrue(attrib.getValues().isEmpty());

        attrib.setValues(Collections.EMPTY_SET);
        Assert.assertTrue(attrib.getValues().isEmpty());
        
        Collection attribValues = new HashSet();
        attrib.setValues(attribValues);
        Assert.assertTrue(attrib.getValues().isEmpty());
        
        attribValues.add(null);
        attrib.setValues(attribValues);
        Assert.assertTrue(attrib.getValues().isEmpty());
        
        Assert.assertTrue(attrib.getValues().add(value1));
        Assert.assertEquals(attrib.getValues().size(), 1);

        // test adding another entry
        Assert.assertTrue(attrib.getValues().add(value2));
        Assert.assertFalse(attrib.getValues().isEmpty());
        Assert.assertEquals(attrib.getValues().size(), 2);
        Assert.assertTrue(attrib.getValues().contains(value1));
        Assert.assertTrue(attrib.getValues().contains(value2));

        // test adding null
        try {
            Assert.assertFalse(attrib.getValues().add(null));
            Assert.fail();
        } catch (NullPointerException e) {
            // THis is OK by the annotation
        }

        // test adding an existing value
        Assert.assertFalse(attrib.getValues().add(value2));
        Assert.assertFalse(attrib.getValues().isEmpty());
        Assert.assertEquals(attrib.getValues().size(), 2);
        Assert.assertTrue(attrib.getValues().contains(value1));
        Assert.assertTrue(attrib.getValues().contains(value2));

        // test removing an entry
        Assert.assertTrue(attrib.getValues().remove(value1));
        Assert.assertFalse(attrib.getValues().isEmpty());
        Assert.assertEquals(attrib.getValues().size(), 1);
        Assert.assertFalse(attrib.getValues().contains(value1));
        Assert.assertTrue(attrib.getValues().contains(value2));

        // test removing the same entry
        Assert.assertFalse(attrib.getValues().remove(value1));
        Assert.assertFalse(attrib.getValues().isEmpty());
        Assert.assertEquals(attrib.getValues().size(), 1);
        Assert.assertFalse(attrib.getValues().contains(value1));
        Assert.assertTrue(attrib.getValues().contains(value2));

        // test removing null
        Assert.assertFalse(attrib.getValues().remove(null));
        Assert.assertFalse(attrib.getValues().isEmpty());
        Assert.assertEquals(attrib.getValues().size(), 1);
        Assert.assertFalse(attrib.getValues().contains(value1));
        Assert.assertTrue(attrib.getValues().contains(value2));

        // test removing the second entry
        Assert.assertTrue(attrib.getValues().remove(value2));
        Assert.assertTrue(attrib.getValues().isEmpty());
        Assert.assertEquals(attrib.getValues().size(), 0);
        Assert.assertFalse(attrib.getValues().contains(value1));
        Assert.assertFalse(attrib.getValues().contains(value2));

        // test adding something once the collection has been drained
        Assert.assertTrue(attrib.getValues().add(value1));
        Assert.assertFalse(attrib.getValues().isEmpty());
        Assert.assertEquals(attrib.getValues().size(), 1);
        Assert.assertTrue(attrib.getValues().contains(value1));
        Assert.assertFalse(attrib.getValues().contains(value2));

        // test replacing all entries
        Collection<IdPAttributeValue<?>> values = new ArrayList<>();
        values.add(value2);
        attrib.setValues(values);
        Assert.assertFalse(attrib.getValues().isEmpty());
        Assert.assertEquals(attrib.getValues().size(), 1);
        Assert.assertFalse(attrib.getValues().contains(value1));
        Assert.assertTrue(attrib.getValues().contains(value2));
    }

    /** Tests that values are properly added and modified. */
    @Test public void encoders() {
        AttributeEncoder<String> enc1 = new MockEncoder<>();
        AttributeEncoder<String> enc2 = new MockEncoder<>();

        IdPAttribute attrib = new IdPAttribute("foo");
        Assert.assertTrue(attrib.getEncoders().isEmpty());
        attrib.setEncoders(null);
        Assert.assertTrue(attrib.getEncoders().isEmpty());
        Collection collection = Arrays.asList((AttributeEncoder)null); 
        attrib.setEncoders(collection);
        Assert.assertTrue(attrib.getEncoders().isEmpty());

        Set<AttributeEncoder<?>> attribEncoders = new HashSet<>();

        // test adding one entry
        Assert.assertTrue(attribEncoders.add(enc1));
        attrib.setEncoders(attribEncoders);
        Assert.assertFalse(attrib.getEncoders().isEmpty());
        Assert.assertEquals(attrib.getEncoders().size(), 1);
        Assert.assertTrue(attrib.getEncoders().contains(enc1));

        // test adding another entry
        Assert.assertTrue(attribEncoders.add(enc2));
        attrib.setEncoders(attribEncoders);
        Assert.assertFalse(attrib.getEncoders().isEmpty());
        Assert.assertEquals(attrib.getEncoders().size(), 2);
        Assert.assertTrue(attrib.getEncoders().contains(enc1));
        Assert.assertTrue(attrib.getEncoders().contains(enc2));

        // test adding null
        
        attribEncoders.add(null);
        attrib.setEncoders(attribEncoders);
        Assert.assertFalse(attrib.getEncoders().isEmpty());
        Assert.assertEquals(attrib.getEncoders().size(), 2);
        Assert.assertTrue(attrib.getEncoders().contains(enc1));
        Assert.assertTrue(attrib.getEncoders().contains(enc2));

        // test adding an existing Encoder
        Assert.assertFalse(attribEncoders.add(enc2));
        attrib.setEncoders(attribEncoders);
        Assert.assertFalse(attrib.getEncoders().isEmpty());
        Assert.assertEquals(attrib.getEncoders().size(), 2);
        Assert.assertTrue(attrib.getEncoders().contains(enc1));
        Assert.assertTrue(attrib.getEncoders().contains(enc2));

        try {
            attrib.getEncoders().add(null);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }
        
    }
    
    @Test public void cloneToString() {
        IdPAttribute attrib = new IdPAttribute("foo");
        IdPAttribute dupl = new IdPAttribute("foo");
        IdPAttribute diff = new IdPAttribute("bar");

        Assert.assertTrue(attrib.equals(attrib));
        Assert.assertTrue(attrib.equals(dupl));
        Assert.assertFalse(attrib.equals(null));
        Assert.assertFalse(attrib.equals(new Integer(2)));
        
        Assert.assertEquals(attrib.hashCode(), dupl.hashCode());
        Assert.assertNotSame(attrib.hashCode(), diff.hashCode());
        
        Assert.assertTrue(attrib.compareTo(diff) > 0);
        Assert.assertEquals(attrib.compareTo(dupl) , 0);
        
        attrib.setValues(Collections.singleton(new LocalizedStringAttributeValue("value1", null)));
        attrib.setDisplayDescriptions(Collections.singletonMap(new Locale("en"), "Descrption"));
        attrib.setDisplayNames(Collections.singletonMap(new Locale("en"), "Name"));
        attrib.toString();
    }
}