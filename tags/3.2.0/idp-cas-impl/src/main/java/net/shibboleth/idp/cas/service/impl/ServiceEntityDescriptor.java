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
package net.shibboleth.idp.cas.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.cas.config.impl.AbstractProtocolConfiguration;
import net.shibboleth.idp.cas.service.Service;
import net.shibboleth.utilities.java.support.collection.LockableClassToInstanceMultiMap;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import org.joda.time.DateTime;
import org.opensaml.core.xml.Namespace;
import org.opensaml.core.xml.NamespaceManager;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.core.xml.util.AttributeMap;
import org.opensaml.core.xml.util.IDIndex;
import org.opensaml.saml.metadata.EntityGroupName;
import org.opensaml.saml.saml2.metadata.AdditionalMetadataLocation;
import org.opensaml.saml.saml2.metadata.AffiliationDescriptor;
import org.opensaml.saml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.AuthnAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.PDPDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.signature.Signature;
import org.w3c.dom.Element;

/**
 * Adapts CAS protocol service metadata onto SAML metadata.
 *
 * @author Marvin S. Addison
 */
public class ServiceEntityDescriptor implements EntityDescriptor {

    /** Underlying CAS service. */
    private final Service service;

    /** The multimap holding class-indexed instances of additional info associated with this XML object. */
    @Nonnull
    private final LockableClassToInstanceMultiMap<Object> objectMetadata;


    /**
     * Creates a new instance that wraps the given CAS service.
     *
     * @param service CAS service metadata object.
     */
    public ServiceEntityDescriptor(@Nonnull final Service service) {
        this.service = Constraint.isNotNull(service, "Service cannot be null");
        this.objectMetadata = new LockableClassToInstanceMultiMap<>(true);
        if (StringSupport.trimOrNull(service.getGroup()) != null) {
            this.objectMetadata.put(new EntityGroupName(service.getGroup()));
        }
    }

    @Override
    public String getEntityID() {
        return service.getName();
    }

    @Override
    public void setEntityID(final String id) { throw new UnsupportedOperationException(); }

    @Override
    public String getID() {
        return service.getName();
    }

    @Override
    public void setID(final String newID) { throw new UnsupportedOperationException(); }

    @Override
    public Extensions getExtensions() {
        return null;
    }

    @Override
    public void setExtensions(final Extensions extensions) { throw new UnsupportedOperationException(); }

    @Override
    public List<RoleDescriptor> getRoleDescriptors() {
        return Collections.emptyList();
    }

    @Override
    public List<RoleDescriptor> getRoleDescriptors(final QName typeOrName) {
        return Collections.emptyList();
    }

    @Override
    public List<RoleDescriptor> getRoleDescriptors(final QName typeOrName, final String supportedProtocol) {
        return Collections.emptyList();
    }

    @Override
    public IDPSSODescriptor getIDPSSODescriptor(final String supportedProtocol) {
        return null;
    }

    @Override
    public SPSSODescriptor getSPSSODescriptor(final String supportedProtocol) {
        return null;
    }

    @Override
    public AuthnAuthorityDescriptor getAuthnAuthorityDescriptor(final String supportedProtocol) {
        return null;
    }

    @Override
    public AttributeAuthorityDescriptor getAttributeAuthorityDescriptor(final String supportedProtocol) {
        return null;
    }

    @Override
    public PDPDescriptor getPDPDescriptor(final String supportedProtocol) {
        return null;
    }

    @Override
    public AffiliationDescriptor getAffiliationDescriptor() {
        return null;
    }

    @Override
    public void setAffiliationDescriptor(final AffiliationDescriptor descriptor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Organization getOrganization() {
        return null;
    }

    @Override
    public void setOrganization(Organization organization) { throw new UnsupportedOperationException(); }

    @Override
    public List<ContactPerson> getContactPersons() {
        return Collections.emptyList();
    }

    @Override
    public List<AdditionalMetadataLocation> getAdditionalMetadataLocations() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public AttributeMap getUnknownAttributes() {
        return null;
    }

    @Override
    public Long getCacheDuration() {
        return null;
    }

    @Override
    public void setCacheDuration(Long duration) { throw new UnsupportedOperationException(); }

    @Nullable
    @Override
    public String getSignatureReferenceID() {
        return null;
    }

    @Override
    public boolean isSigned() {
        return false;
    }

    @Nullable
    @Override
    public Signature getSignature() {
        return null;
    }

    @Override
    public void setSignature(@Nullable Signature newSignature) { throw new UnsupportedOperationException(); }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public DateTime getValidUntil() {
        return DateTime.now().plusDays(1);
    }

    @Override
    public void setValidUntil(final DateTime validUntil) { throw new UnsupportedOperationException(); }

    @Override
    public void detach() {}

    @Nullable
    @Override
    public Element getDOM() {
        return null;
    }

    @Nonnull
    @Override
    public QName getElementQName() {
        return new QName(AbstractProtocolConfiguration.PROTOCOL_URI, "cas");
    }

    @Nonnull
    @Override
    public IDIndex getIDIndex() {
        return null;
    }

    @Nonnull
    @Override
    public NamespaceManager getNamespaceManager() {
        return null;
    }

    @Nonnull
    @Override
    public Set<Namespace> getNamespaces() {
        return Collections.emptySet();
    }

    @Nullable
    @Override
    public String getNoNamespaceSchemaLocation() {
        return null;
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public XMLObject getParent() {
        return null;
    }

    @Nullable
    @Override
    public String getSchemaLocation() {
        return null;
    }

    @Nullable
    @Override
    public QName getSchemaType() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean hasParent() {
        return false;
    }

    @Override
    public void releaseChildrenDOM(final boolean propagateRelease) {}

    @Override
    public void releaseDOM() {}

    @Override
    public void releaseParentDOM(final boolean propagateRelease) {}

    @Nullable
    @Override
    public XMLObject resolveID(@Nonnull final String id) {
        return null;
    }

    @Nullable
    @Override
    public XMLObject resolveIDFromRoot(@Nonnull final String id) {
        return null;
    }

    @Override
    public void setDOM(@Nullable final Element dom) { throw new UnsupportedOperationException(); }

    @Override
    public void setNoNamespaceSchemaLocation(@Nullable final String location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParent(@Nullable final XMLObject parent) { throw new UnsupportedOperationException(); }

    @Override
    public void setSchemaLocation(@Nullable final String location) { throw new UnsupportedOperationException(); }

    @Nullable
    @Override
    public Boolean isNil() {
        return false;
    }

    @Nullable
    @Override
    public XSBooleanValue isNilXSBoolean() {
        return null;
    }

    @Override
    public void setNil(@Nullable final Boolean newNil) { throw new UnsupportedOperationException(); }

    @Override
    public void setNil(@Nullable final XSBooleanValue newNil) { throw new UnsupportedOperationException(); }

    @Nonnull
    @Override
    public LockableClassToInstanceMultiMap<Object> getObjectMetadata() {
        return objectMetadata;
    }
}
