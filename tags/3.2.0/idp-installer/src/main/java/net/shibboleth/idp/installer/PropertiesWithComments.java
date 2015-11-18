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

package net.shibboleth.idp.installer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * A package which is similar to Properties, but allows comments to be preserved. We use the Properties package to parse
 * the non-comment lines.
 */
public class PropertiesWithComments {

    /**
     * The contents.
     * 
     * Each {@link Object} is either a string (a non-property line) or a {@link CommentedProperty}
     * (an optionally commented property definition).
     */
    private List<Object> contents;

    /** The properties bit. */
    private Map<String, CommentedProperty> properties;

    /**
     * Add a property, either as a key/value pair or as a key/comment pair.
     * 
     * @param line what to look at
     * @param isComment whether this is a comment or not.
     * @throws IOException when badness happens.
     */
    protected void addCommentedProperty(@Nonnull @NotEmpty final String line, boolean isComment) throws IOException {
        final Properties parser = new Properties();
        final String modifiedLine;

        if (isComment) {
            modifiedLine = line.substring(1);
        } else {
            modifiedLine = line;
        }

        parser.load(new ByteArrayInputStream(modifiedLine.getBytes()));
        if (!parser.isEmpty()) {
            final String propName = StringSupport.trimOrNull(parser.stringPropertyNames().iterator().next());
            if (propName != null) {
                final CommentedProperty commentedProperty;

                if (isComment) {
                    commentedProperty = new CommentedProperty(propName, line, true);

                } else {
                    commentedProperty = new CommentedProperty(propName, parser.getProperty(propName), false);

                }
                properties.put(propName, commentedProperty);
                contents.add(commentedProperty);
            }
        } else {
            contents.add(line);
        }
        parser.clear();

    }

    /**
     * Read the input stream into our structures.
     * 
     * @param input what to read
     * @throws IOException if readline fails
     */
    public void load(InputStream input) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        contents = new ArrayList<>();
        properties = new HashMap<>();

        String s = reader.readLine();

        while (s != null) {
            final String what = StringSupport.trimOrNull(s);
            if (what == null) {
                contents.add("");
            } else if (what.startsWith("#")) {
                if (what.contains("=")) {
                    addCommentedProperty(s, true);
                } else {
                    contents.add(what);
                }
            } else {

                addCommentedProperty(s, false);
            }
            s = reader.readLine();
        }
    }

    /**
     * Put the output to the supplied stream.
     * 
     * @param output where to write
     * @throws IOException is the write fails
     */
    public void store(OutputStream output) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));

        for (Object o : contents) {
            if (o instanceof String) {
                writer.write((String) o);
            } else if (o instanceof CommentedProperty) {
                final CommentedProperty commentedProperty = (CommentedProperty) o;
                commentedProperty.write(writer);
            }
            writer.newLine();
        }
        writer.flush();
        writer.close();
        output.close();
    }

    /**
     * Replace the supplied property or stuff it at the bottom of the list.
     * 
     * @param propName the name of the property to replace
     * @param newPropValue the value to replace
     * @return true if the property was replaced false if it was added
     */
    public boolean replaceProperty(String propName, String newPropValue) {

        CommentedProperty p = properties.get(propName);
        if (null != p) {
            p.setValue(newPropValue);
            return true;
        }
        p = new CommentedProperty(propName, newPropValue, false);
        contents.add(p);
        properties.put(propName, p);
        return false;
    }

    /**
     * Append a comment to the list.
     * 
     * @param what what to add
     */
    public void addComment(String what) {
        contents.add("# " + what);
    }

    /**
     * A POJO which looks like a property.
     * 
     * It may be a commented property from a line like this "#prop=value" or a property prop=value.
     * 
     */
    protected class CommentedProperty {

        /** The property name. */
        private final String property;

        /** The value - or the entire line if this is a comment. */
        private String value;

        /** Whether this is a comment or a value. */
        private boolean isComment;

        /**
         * Constructor.
         * 
         * @param prop the property name.
         * @param val the value or the entire line if this was a comment.
         * @param comment whether this is a comment.
         */
        CommentedProperty(final String prop, final String val, final boolean comment) {
            property = prop;
            value = val;
            isComment = comment;
        }

        /**
         * Set a new value.
         * 
         * @param newValue what to set
         */
        protected void setValue(String newValue) {
            value = newValue;
            isComment = false;
        }

        /**
         * Write ourselves to the writer.
         * 
         * @param writer what to write with
         * @throws IOException from the writer
         */
        protected void write(final BufferedWriter writer) throws IOException {

            if (isComment) {
                writer.write(value);
            } else {
                writer.write(property);
                writer.write("= ");
                writer.write(value);
            }
        }
    }
}
