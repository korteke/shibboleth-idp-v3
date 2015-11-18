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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * test for {@link PropertiesWithComments}.
 */
public class TestPropertiesWithComments {

    private File testFile;

    @BeforeClass public void setup() throws IOException {
        testFile = File.createTempFile("test", ".properties");
    }

    @AfterClass public void tearDown() {
        testFile.delete();
    }

    @Test public void test() throws FileNotFoundException, IOException {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("net/shibboleth/idp/installer/file.properties");
        
        final PropertiesWithComments pwc = new PropertiesWithComments();
        try {
            pwc.load(stream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Assert.assertTrue(pwc.replaceProperty("p", "321"));
        pwc.addComment("Comment");
        Assert.assertFalse(pwc.replaceProperty("nn", "123"));
        Assert.assertTrue(pwc.replaceProperty("yy", "123321"));
        
        pwc.store(new FileOutputStream(testFile));
        
        final Properties p = new Properties();
        
        p.load(new FileInputStream(testFile));
        
        Assert.assertEquals(p.stringPropertyNames().size(), 4);
        Assert.assertEquals(p.getProperty("p"), "321");
        Assert.assertEquals(p.getProperty("nn"), "123");
        Assert.assertEquals(p.getProperty("yy"), "123321");
        Assert.assertEquals(p.getProperty("q"), "elephants");
        
    }
}
