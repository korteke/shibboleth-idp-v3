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

package net.shibboleth.idp.attribute.filter.matcher.logic.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.DestructableComponent;
import net.shibboleth.utilities.java.support.component.InitializableComponent;

import org.testng.Assert;
import org.testng.annotations.Test;

/** unit tests for {@link AbstractComposedMatcher}. */
public class AbstractComposedMatcherTest {

    @Test public void testInitDestroy() throws ComponentInitializationException {
        List<Matcher> firstList = new ArrayList<>(2);
        ComposedMatcher matcher = new ComposedMatcher(Collections.EMPTY_LIST);

        for (int i = 0; i < 2; i++) {
            firstList.add(new TestMatcher());
        }

        matcher.destroy();

        boolean thrown = false;
        try {
            matcher.initialize();
        } catch (DestroyedComponentException e) {
            thrown = true;
        }

        Assert.assertTrue(thrown, "Initialize after destroy");

        for (int i = 0; i < 2; i++) {
            firstList.add(new TestMatcher());
        }
        firstList.add(null);
        matcher = new ComposedMatcher(firstList);

        Assert.assertEquals(firstList.size() - 1, matcher.getComposedMatchers().size());

        thrown = false;
        try {
            matcher.getComposedMatchers().add(new TestMatcher());
        } catch (UnsupportedOperationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Set into the returned list");
        matcher.setId("Test");

        matcher.initialize();
    }

    @Test public void testParams() throws ComponentInitializationException {
        ComposedMatcher matcher = new ComposedMatcher(null);

        Assert.assertTrue(matcher.getComposedMatchers().isEmpty(), "Initial state - no matchers");
        Assert.assertTrue(matcher.getComposedMatchers().isEmpty(), "Add null - no matchers");

        List<Matcher> list = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            list.add(null);
        }

        matcher = new ComposedMatcher(list);
        Assert.assertTrue(matcher.getComposedMatchers().isEmpty(), "Add List<null> - no matchers");

        list.set(2, new TestMatcher());
        list.set(3, new TestMatcher());
        list.set(7, new TestMatcher());
        list.set(11, new TestMatcher());
        list.set(13, new TestMatcher());
        list.set(17, new TestMatcher());
        list.set(19, new TestMatcher());
        list.set(23, new TestMatcher());
        list.set(29, new TestMatcher());
        Assert.assertTrue(matcher.getComposedMatchers().isEmpty(), "Change to input list - no matchers");

        matcher = new ComposedMatcher(list);
        Assert.assertEquals(matcher.getComposedMatchers().size(), 9, "Add a List with nulls");

        list.clear();
        Assert.assertEquals(matcher.getComposedMatchers().size(), 9, "Change to input list");

        matcher = new ComposedMatcher(list);
        Assert.assertTrue(matcher.getComposedMatchers().isEmpty(), "Empty list");

    }

    private class ComposedMatcher extends AbstractComposedMatcher {

        /**
         * Constructor.
         * 
         * @param composedMatchers
         */
        public ComposedMatcher(Collection<Matcher> composedMatchers) {
            super(composedMatchers);
        }

        @Override public Set<IdPAttributeValue<?>> getMatchingValues(IdPAttribute attribute,
                AttributeFilterContext filterContext) {
            return null;
        }

    }

    public static class TestMatcher extends AbstractInitializableComponent implements Matcher, DestructableComponent,
            InitializableComponent {

        @Override public Set<IdPAttributeValue<?>> getMatchingValues(IdPAttribute attribute,
                AttributeFilterContext filterContext) {
            return null;
        }

        public boolean matches(@Nullable AttributeFilterContext arg0) {
            return false;
        }

        /** {@inheritDoc} */
        @Override @Nullable public String getId() {
            return "99";
        }

    }
}