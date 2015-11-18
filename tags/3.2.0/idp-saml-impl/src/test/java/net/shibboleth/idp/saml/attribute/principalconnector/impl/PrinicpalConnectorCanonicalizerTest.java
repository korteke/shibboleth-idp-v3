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

package net.shibboleth.idp.saml.attribute.principalconnector.impl;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;

import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.saml.authn.principal.NameIDPrincipal;
import net.shibboleth.idp.saml.authn.principal.NameIdentifierPrincipal;
import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.idp.saml.nameid.NameIDDecoder;
import net.shibboleth.idp.saml.nameid.NameIdentifierDecoder;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml1.core.impl.NameIdentifierBuilder;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * tests for {@link PrinicpalConnectorCanonicalizer}.
 */
public class PrinicpalConnectorCanonicalizerTest extends OpenSAMLInitBaseTestCase {

    private final NameIDBuilder nameIDBuilder = new NameIDBuilder();

    private final NameIdentifierBuilder nameIdentifierBuilder = new NameIdentifierBuilder();

    private TestCanonicalizer testCanon;

    @BeforeClass public void setup() {
        final Collection<PrincipalConnector> connectors = new HashSet<>(3);

        MyDecoder decoder = new MyDecoder(NameID.KERBEROS);
        connectors.add(new PrincipalConnector(decoder, decoder, NameID.KERBEROS));

        decoder = new MyDecoder(NameID.UNSPECIFIED);
        connectors.add(new PrincipalConnector(decoder, decoder, NameIdentifier.UNSPECIFIED));

        testCanon = new TestCanonicalizer(connectors);
    }

    @Test public void testCanonicalize() throws ResolutionException {

        final Subject subject = new Subject();
        final SubjectCanonicalizationContext context = new SubjectCanonicalizationContext();

        context.setSubject(subject);
        Assert.assertNull(testCanon.canonicalize(context));

        subject.getPrincipals().add(new UsernamePrincipal("user"));
        context.setSubject(subject);
        Assert.assertNull(testCanon.canonicalize(context));

        context.setRequesterId("Requester");
        context.setResponderId("Responder");

        subject.getPrincipals().clear();
        NameIdentifier nameIdentifier = nameIdentifierBuilder.buildObject();
        subject.getPrincipals().add(new NameIdentifierPrincipal(nameIdentifier));
        Assert.assertEquals(testCanon.canonicalize(context), "nameIdentifier");

        nameIdentifier = nameIdentifierBuilder.buildObject();
        nameIdentifier.setFormat(NameIdentifier.WIN_DOMAIN_QUALIFIED);
        subject.getPrincipals().add(new NameIdentifierPrincipal(nameIdentifier));
        Assert.assertNull(testCanon.canonicalize(context), "too many NameIdentifiers");

        NameID nameID = nameIDBuilder.buildObject();
        subject.getPrincipals().add(new NameIDPrincipal(nameID));
        Assert.assertEquals(testCanon.canonicalize(context), "nameID");

        nameID = nameIDBuilder.buildObject();
        nameID.setFormat(NameIdentifier.UNSPECIFIED);
        subject.getPrincipals().add(new NameIDPrincipal(nameID));
        Assert.assertNull(testCanon.canonicalize(context), "Too many NameIDs");
    }

    @Test public void testSAML1() throws ResolutionException {
        final Subject subject = new Subject();
        final SubjectCanonicalizationContext context = new SubjectCanonicalizationContext();
        NameIdentifier nameIdentifier = nameIdentifierBuilder.buildObject();
        nameIdentifier.setValue("val");
        subject.getPrincipals().add(new NameIdentifierPrincipal(nameIdentifier));
        context.setSubject(subject);
        context.setRequesterId("S1Requester");
        context.setResponderId("S1Responder");

        Assert.assertEquals(testCanon.doCanonicalize(nameIdentifier, context), NameIdentifier.UNSPECIFIED
                + nameIdentifier.getValue() + context.getRequesterId() + context.getResponderId());

        subject.getPrincipals().clear();
        nameIdentifier = nameIdentifierBuilder.buildObject();
        nameIdentifier.setValue("val2");
        nameIdentifier.setFormat(NameIdentifier.UNSPECIFIED);
        subject.getPrincipals().add(new NameIdentifierPrincipal(nameIdentifier));
        Assert.assertEquals(testCanon.doCanonicalize(nameIdentifier, context), NameIdentifier.UNSPECIFIED
                + nameIdentifier.getValue() + context.getRequesterId() + context.getResponderId());

        subject.getPrincipals().clear();
        nameIdentifier = nameIdentifierBuilder.buildObject();
        nameIdentifier.setValue("val3");
        nameIdentifier.setFormat(NameIdentifier.WIN_DOMAIN_QUALIFIED);
        subject.getPrincipals().add(new NameIdentifierPrincipal(nameIdentifier));
        Assert.assertNull(testCanon.doCanonicalize(nameIdentifier, context));

        subject.getPrincipals().clear();
        nameIdentifier = nameIdentifierBuilder.buildObject();
        nameIdentifier.setValue("val4");
        nameIdentifier.setFormat(NameID.KERBEROS);
        subject.getPrincipals().add(new NameIdentifierPrincipal(nameIdentifier));
        Assert.assertEquals(
                testCanon.doCanonicalize(nameIdentifier, context),
                NameID.KERBEROS + nameIdentifier.getValue() + context.getRequesterId()
                        + context.getResponderId());
    }

    @Test public void testSAML2() throws ResolutionException {
        final Subject subject = new Subject();
        final SubjectCanonicalizationContext context = new SubjectCanonicalizationContext();
        NameID nameID = nameIDBuilder.buildObject();
        nameID.setValue("NID1");
        subject.getPrincipals().add(new NameIDPrincipal(nameID));
        context.setSubject(subject);
        context.setRequesterId("SAML2Requester");
        context.setResponderId("SAML2Responder");

        Assert.assertEquals(testCanon.doCanonicalize(nameID, context), nameID.getValue() + context.getRequesterId()
                + context.getResponderId() + NameID.UNSPECIFIED);

        subject.getPrincipals().clear();
        nameID = nameIDBuilder.buildObject();
        nameID.setValue("NID2");
        nameID.setFormat(NameID.UNSPECIFIED);
        subject.getPrincipals().add(new NameIDPrincipal(nameID));
        Assert.assertEquals(testCanon.doCanonicalize(nameID, context), nameID.getValue() + context.getRequesterId()
                + context.getResponderId() + NameID.UNSPECIFIED);

        subject.getPrincipals().clear();
        nameID = nameIDBuilder.buildObject();
        nameID.setValue("NID3");
        nameID.setFormat(NameID.WIN_DOMAIN_QUALIFIED);
        subject.getPrincipals().add(new NameIDPrincipal(nameID));
        Assert.assertNull(testCanon.doCanonicalize(nameID, context));

        subject.getPrincipals().clear();
        nameID = nameIDBuilder.buildObject();
        nameID.setValue("NID4");
        nameID.setFormat(NameID.KERBEROS);
        subject.getPrincipals().add(new NameIDPrincipal(nameID));
        Assert.assertEquals(testCanon.doCanonicalize(nameID, context), nameID.getValue() + context.getRequesterId()
                + context.getResponderId() + NameID.KERBEROS);
    }

    public static class TestCanonicalizer extends PrinicpalConnectorCanonicalizer {

        /**
         * Constructor.
         * 
         * @param connectors
         */
        public TestCanonicalizer(Collection<PrincipalConnector> connectors) {
            super(connectors);
        }

        @Override protected String canonicalize(NameIdentifier nameIdentifier,
                SubjectCanonicalizationContext c14nContext) throws ResolutionException {
            return "nameIdentifier";
        }

        /** Make visible for testing . */
        public String doCanonicalize(NameIdentifier nameIdentifier, SubjectCanonicalizationContext c14nContext)
                throws ResolutionException {
            return super.canonicalize(nameIdentifier, c14nContext);
        }

        @Override protected String canonicalize(NameID nameID, SubjectCanonicalizationContext c14nContext)
                throws ResolutionException {
            return "nameID";
        }

        /** Make visible for testing . */
        public String doCanonicalize(NameID nameID, SubjectCanonicalizationContext c14nContext)
                throws ResolutionException {
            return super.canonicalize(nameID, c14nContext);
        }
    }

    public static class MyDecoder implements NameIdentifierDecoder, NameIDDecoder {

        private final String prefix;

        public MyDecoder(String thePrefix) {
            prefix = thePrefix;
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull public String decode(@Nonnull final SubjectCanonicalizationContext scc, @Nonnull NameIdentifier nameIdentifier)
                throws NameDecoderException {
            return prefix + nameIdentifier.getValue() + scc.getRequesterId() + scc.getResponderId();
        }

        /** {@inheritDoc} */
        @Override @Nonnull public String decode(@Nonnull final SubjectCanonicalizationContext scc, @Nonnull NameID nameID)
                throws NameDecoderException {
            return nameID.getValue() + scc.getRequesterId() + scc.getResponderId() + prefix;
        }

    }
    
}