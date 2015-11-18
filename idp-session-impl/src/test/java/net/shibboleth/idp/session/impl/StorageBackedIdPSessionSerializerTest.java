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

package net.shibboleth.idp.session.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.session.BasicSPSession;
import net.shibboleth.idp.session.SPSessionSerializerRegistry;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.net.CookieManager;
import net.shibboleth.utilities.java.support.net.ThreadLocalHttpServletRequestProxy;
import net.shibboleth.utilities.java.support.net.ThreadLocalHttpServletResponseProxy;
import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;

import org.opensaml.storage.impl.MemoryStorageService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link StorageBackedIdPSessionSerializer} unit test. */
public class StorageBackedIdPSessionSerializerTest {

    private static final String DATAPATH = "/data/net/shibboleth/idp/session/impl/";
    
    private static final long INSTANT = 1378827849463L;
    
    private static final String CONTEXT = "context";
    
    private static final String KEY = "key";
    
    private StorageBackedSessionManager manager;
    
    private StorageBackedIdPSessionSerializer serializer;
    
    @BeforeMethod public void setUp() throws ComponentInitializationException {
        final MemoryStorageService storageService = new MemoryStorageService();
        storageService.setId("TestStorageService");
        storageService.setCleanupInterval(0);
        storageService.initialize();

        CookieManager cookieManager = new CookieManager();
        cookieManager.setHttpServletRequest(new ThreadLocalHttpServletRequestProxy());
        cookieManager.setHttpServletResponse(new ThreadLocalHttpServletResponseProxy());
        cookieManager.initialize();
        
        manager = new StorageBackedSessionManager();
        manager.setStorageService(storageService);
        manager.setIDGenerator(new SecureRandomIdentifierGenerationStrategy());
        manager.setHttpServletRequest(new ThreadLocalHttpServletRequestProxy());
        manager.setHttpServletResponse(new ThreadLocalHttpServletResponseProxy());
        manager.setCookieManager(cookieManager);
        manager.setId("Test Session Manager");
        manager.setTrackSPSessions(true);
        manager.setSPSessionSerializerRegistry(new SPSessionSerializerRegistry());
        manager.initialize();

        serializer = new StorageBackedIdPSessionSerializer(manager, null);
        serializer.initialize();
    }

    @Test public void testInvalid() throws Exception {
        try {
            serializer.deserialize(1, CONTEXT, KEY, fileToString(DATAPATH + "invalid.json"), INSTANT);
            Assert.fail();
        } catch (IOException e) {
            
        }

        try {
            serializer.deserialize(1, CONTEXT, KEY, fileToString(DATAPATH + "noInstant.json"), INSTANT);
            Assert.fail();
        } catch (IOException e) {
            
        }

        try {
            serializer.deserialize(1, CONTEXT, KEY, fileToString(DATAPATH + "noName.json"), INSTANT);
            Assert.fail();
        } catch (IOException e) {
            
        }
        
        try {
            // Tests expiration being null.
            serializer.deserialize(1, CONTEXT, KEY, fileToString(DATAPATH + "basicIdPSession.json"), null);
            Assert.fail();
        } catch (IOException e) {
            
        }
    }
    
    @Test public void testBasic() throws Exception {
        long exp = INSTANT + (60 * 60 * 1000);
        
        StorageBackedIdPSession session = new StorageBackedIdPSession(manager, "test", "foo", INSTANT);
        session.doBindToAddress("127.0.0.1");
        
        String s = serializer.serialize(session);
        String s2 = fileToString(DATAPATH + "basicIdPSession.json");
        Assert.assertEquals(s, s2);
        
        StorageBackedIdPSession session2 = serializer.deserialize(1, "test", KEY, s2, exp);

        Assert.assertEquals(session.getId(), session2.getId());
        Assert.assertEquals(session.getPrincipalName(), session2.getPrincipalName());
        Assert.assertEquals(session.getCreationInstant(), session2.getCreationInstant());
        Assert.assertEquals(session.getLastActivityInstant(), session2.getLastActivityInstant());
    }

    @Test public void testComplex() throws Exception {
        long exp = INSTANT + (60 * 60 * 1000);
        
        StorageBackedIdPSession session = new StorageBackedIdPSession(manager, "test", "foo", INSTANT);
        session.doBindToAddress("127.0.0.1");
        session.doAddAuthenticationResult(new AuthenticationResult("a", new UsernamePrincipal("jdoe")));
        session.doAddAuthenticationResult(new AuthenticationResult("b", new UsernamePrincipal("jdoe")));
        session.doAddAuthenticationResult(new AuthenticationResult("c", new UsernamePrincipal("jdoe")));
        session.doAddSPSession(new BasicSPSession("bar", INSTANT, exp));
        session.doAddSPSession(new BasicSPSession("baz", INSTANT, exp));
        
        String s = serializer.serialize(session);
        String s2 = fileToString(DATAPATH + "complexIdPSession." + (isV8() ? "jdk8" : "json"));
        Assert.assertEquals(s, s2);
        
        StorageBackedIdPSession session2 = serializer.deserialize(1, "test", KEY, s2, exp);

        Assert.assertEquals(session.getId(), session2.getId());
        Assert.assertEquals(session.getPrincipalName(), session2.getPrincipalName());
        Assert.assertEquals(session.getCreationInstant(), session2.getCreationInstant());
        Assert.assertEquals(session.getLastActivityInstant(), session2.getLastActivityInstant());
    }
    
    private boolean isV8() {
        final String ver = System.getProperty("java.version");
        return ver.startsWith("1.8");
    }
    
    private String fileToString(String pathname) throws URISyntaxException, IOException {
        try (FileInputStream stream = new FileInputStream(
                new File(StorageBackedIdPSessionSerializerTest.class.getResource(pathname).toURI()))) {
            int avail = stream.available();
            byte[] data = new byte[avail];
            int numRead = 0;
            int pos = 0;
            do {
              if (pos + avail > data.length) {
                byte[] newData = new byte[pos + avail];
                System.arraycopy(data, 0, newData, 0, pos);
                data = newData;
              }
              numRead = stream.read(data, pos, avail);
              if (numRead >= 0) {
                pos += numRead;
              }
              avail = stream.available();
            } while (avail > 0 && numRead >= 0);
            return new String(data, 0, pos, "UTF-8");
        }
    }
}