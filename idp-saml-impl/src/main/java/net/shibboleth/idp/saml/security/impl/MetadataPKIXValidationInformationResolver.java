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

package net.shibboleth.idp.saml.security.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.idp.saml.security.KeyAuthoritySupport;
import net.shibboleth.idp.saml.xmlobject.KeyAuthority;
import net.shibboleth.utilities.java.support.collection.LockableClassToInstanceMultiMap;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.metadata.resolver.RoleDescriptorResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.x509.PKIXValidationInformation;
import org.opensaml.security.x509.PKIXValidationInformationResolver;
import org.opensaml.security.x509.TrustedNamesCriterion;
import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link PKIXValidationInformationResolver} which resolves {@link PKIXValidationInformation} based
 * on information stored in SAML 2 metadata. Validation information is retrieved from Shibboleth-specific metadata
 * extensions to {@link EntityDescriptor} represented by instances of {@link KeyAuthority}, as well as instances of
 * {@link PKIXValidationInformation} which have been previously populated within the data set available from
 * {@link EntityDescriptor#getObjectMetadata()}.
 * 
 * Resolution of trusted names for an entity is also supported, based on {@link org.opensaml.xmlsec.signature.KeyName}
 * information contained within the {@link KeyInfo} of a role descriptor's {@link KeyDescriptor} element.
 */
public class MetadataPKIXValidationInformationResolver extends AbstractInitializableComponent
        implements PKIXValidationInformationResolver {

    /** Default value for Shibboleth KeyAuthority verify depth. */
    public static final int KEY_AUTHORITY_VERIFY_DEPTH_DEFAULT = 1;

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(MetadataPKIXValidationInformationResolver.class);

    /** Metadata RoleDescriptor resolver used to resolve metadata information. */
    @Nonnull private RoleDescriptorResolver roleDescriptorResolver;

    /**
     * Constructor.
     * 
     * @param resolver role descriptor resolver
     */
    public MetadataPKIXValidationInformationResolver(@Nonnull final RoleDescriptorResolver resolver) {
        roleDescriptorResolver = Constraint.isNotNull(resolver, "RoleDescriptor resolver cannot be null");
    }

    /**
     * Get the metadata RoleDescriptor resolver instance used by this resolver.
     * 
     * @return the resolver's RoleDescriptor metadata resolver instance
     */
    @Nonnull public RoleDescriptorResolver getRoleDescriptorResolver() {
        return roleDescriptorResolver;
    }

    /** {@inheritDoc} */
    @Override public PKIXValidationInformation resolveSingle(CriteriaSet criteriaSet) throws ResolverException {
        final Iterator<PKIXValidationInformation> pkixInfoIter = resolve(criteriaSet).iterator();
        if (pkixInfoIter.hasNext()) {
            return pkixInfoIter.next();
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override public Iterable<PKIXValidationInformation> resolve(CriteriaSet criteriaSet) throws ResolverException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        checkCriteriaRequirements(criteriaSet);

        final String entityID = criteriaSet.get(EntityIdCriterion.class).getEntityId();

        final EntityRoleCriterion roleCriteria = criteriaSet.get(EntityRoleCriterion.class);
        final QName role = roleCriteria.getRole();

        String protocol = null;
        final ProtocolCriterion protocolCriteria = criteriaSet.get(ProtocolCriterion.class);
        if (protocolCriteria != null) {
            protocol = protocolCriteria.getProtocol();
        }

        return retrievePKIXInfoFromMetadata(criteriaSet, entityID, role, protocol);
    }

    /** {@inheritDoc} */
    @Override @Nonnull public Set<String> resolveTrustedNames(CriteriaSet criteriaSet) throws ResolverException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        checkCriteriaRequirements(criteriaSet);

        final String entityID = criteriaSet.get(EntityIdCriterion.class).getEntityId();

        final EntityRoleCriterion roleCriteria = criteriaSet.get(EntityRoleCriterion.class);
        final QName role = roleCriteria.getRole();

        String protocol = null;
        final ProtocolCriterion protocolCriteria = criteriaSet.get(ProtocolCriterion.class);
        if (protocolCriteria != null) {
            protocol = protocolCriteria.getProtocol();
        }

        final UsageCriterion usageCriteria = criteriaSet.get(UsageCriterion.class);
        UsageType usage = null;
        if (usageCriteria != null) {
            usage = usageCriteria.getUsage();
        } else {
            usage = UsageType.UNSPECIFIED;
        }

        Set<String> trustedNames = new HashSet<>();
        trustedNames.addAll(retrieveTrustedNamesFromMetadata(criteriaSet, entityID, role, protocol, usage));
        trustedNames.add(entityID);
        TrustedNamesCriterion trustedNamesCriterion = criteriaSet.get(TrustedNamesCriterion.class);
        if (trustedNamesCriterion != null) {
            trustedNames.addAll(trustedNamesCriterion.getTrustedNames());
        }
        return trustedNames;
    }

    /** {@inheritDoc} */
    @Override public boolean supportsTrustedNameResolution() {
        return true;
    }

    /**
     * Check that all necessary criteria are available.
     * 
     * @param criteriaSet the criteria set to evaluate
     */
    protected void checkCriteriaRequirements(CriteriaSet criteriaSet) {
        final EntityIdCriterion entityCriteria =
                Constraint.isNotNull(criteriaSet.get(EntityIdCriterion.class), "EntityIdCriterion must be supplied");
        Constraint.isNotNull(StringSupport.trimOrNull(entityCriteria.getEntityId()),
                "Credential owner entity ID criteria value must be supplied");

        final EntityRoleCriterion roleCriteria =
                Constraint
                        .isNotNull(criteriaSet.get(EntityRoleCriterion.class), "EntityRoleCriterion must be supplied");
        Constraint.isNotNull(roleCriteria.getRole(), "Credential entity role criteria value must be supplied");
    }

    /**
     * Retrieves validation information from the provided resolver.
     * 
     * @param criteriaSet the criteria set being processed
     * @param entityID entity ID for which to resolve validation information
     * @param role role in which the entity is operating
     * @param protocol protocol over which the entity is operating (may be null)
     * 
     * @return collection of resolved validation information, possibly empty
     * 
     * @throws ResolverException thrown if the key, certificate, or CRL information is represented in an unsupported
     *             format
     */
    protected Collection<PKIXValidationInformation> retrievePKIXInfoFromMetadata(CriteriaSet criteriaSet,
            String entityID, QName role, String protocol) throws ResolverException {

        log.debug("Attempting to retrieve PKIX validation info from resolver for entity: {}", entityID);
        // Use LinkedHashSet so we don't worry about duplicates, but keep predictable ordering (insertion order).
        final Collection<PKIXValidationInformation> accumulator = new LinkedHashSet<>();

        final Iterable<RoleDescriptor> roleDescriptors = getRoleDescriptors(criteriaSet, entityID, role, protocol);
        if (roleDescriptors == null) {
            return accumulator;
        }

        for (final RoleDescriptor roleDescriptor : roleDescriptors) {
            resolvePKIXInfo(accumulator, roleDescriptor);
        }

        return accumulator;
    }

    /**
     * Retrieves validation information from the provided role descriptor.
     * 
     * @param roleDescriptor the role descriptor from which to resolve information.
     * @param accumulator accumulator of PKIX validation information to return
     * @throws ResolverException thrown if the key, certificate, or CRL information is represented in an unsupported
     *             format
     * 
     */
    protected void resolvePKIXInfo(Collection<PKIXValidationInformation> accumulator, RoleDescriptor roleDescriptor)
            throws ResolverException {

        if (roleDescriptor.getParent() instanceof EntityDescriptor) {
            final EntityDescriptor entityDescriptor = (EntityDescriptor) roleDescriptor.getParent();

            resolvePKIXInfo(accumulator, entityDescriptor.getExtensions());

            // These would have been cached on the EntityDescriptor by another mechanism,
            // for example via pre-processing by the MetadataResolver.
            final LockableClassToInstanceMultiMap<Object> entityDescriptorObjectMetadata =
                    entityDescriptor.getObjectMetadata();
            final ReadWriteLock rwlock = entityDescriptorObjectMetadata.getReadWriteLock();
            try {
                rwlock.readLock().lock();
                accumulator.addAll(entityDescriptorObjectMetadata.get(PKIXValidationInformation.class));
            } finally {
                rwlock.readLock().unlock();
            }
        }

    }

    /**
     * Retrieves validation information from the resolver extension element.
     * 
     * @param extensions the extension element from which to resolve information
     * @param accumulator accumulator of PKIX validation information to return
     * @throws ResolverException thrown if the key, certificate, or CRL information is represented in an unsupported
     *             format
     */
    protected void resolvePKIXInfo(Collection<PKIXValidationInformation> accumulator, Extensions extensions)
            throws ResolverException {
        if (extensions == null) {
            return;
        }

        final List<XMLObject> authorities = extensions.getUnknownXMLObjects(KeyAuthority.DEFAULT_ELEMENT_NAME);
        if (authorities == null || authorities.isEmpty()) {
            return;
        }

        for (final XMLObject xmlObj : authorities) {
            extractPKIXInfo(accumulator, (KeyAuthority) xmlObj);
        }
    }

    /**
     * Retrieves validation information from the Shibboleth KeyAuthority resolver extension element.
     * 
     * @param keyAuthority the Shibboleth KeyAuthority element from which to resolve information
     * @param accumulator accumulator of PKIX validation information to return
     * @throws ResolverException thrown if the key, certificate, or CRL information is represented in an unsupported
     *             format
     */
    protected void extractPKIXInfo(@Nonnull final Collection<PKIXValidationInformation> accumulator,
            @Nonnull final KeyAuthority keyAuthority) throws ResolverException {

        final LockableClassToInstanceMultiMap<Object> keyAuthorityObjectMetadata = keyAuthority.getObjectMetadata();
        final ReadWriteLock rwlock = keyAuthorityObjectMetadata.getReadWriteLock();

        try {
            rwlock.readLock().lock();
            final List<PKIXValidationInformation> cachedPKIXInfo =
                    keyAuthorityObjectMetadata.get(PKIXValidationInformation.class);
            if (!cachedPKIXInfo.isEmpty()) {
                log.debug("Resolved cached PKIXValidationInformation from KeyAuthority object metadata");
                accumulator.addAll(cachedPKIXInfo);
                return;
            } else {
                log.debug("Found no cached PKIXValidationInformation in KeyAuthority object metadata, resolving XML");
            }
        } finally {
            // Note: with the standard Java ReentrantReadWriteLock impl, you can not upgrade a read lock
            // to a write lock! So have to release here and then acquire the write lock below.
            rwlock.readLock().unlock();
        }

        try {
            rwlock.writeLock().lock();

            // Need to check again in case another waiting writer beat us in acquiring the write lock
            final List<PKIXValidationInformation> cachedPKIXInfo =
                    keyAuthorityObjectMetadata.get(PKIXValidationInformation.class);
            if (!cachedPKIXInfo.isEmpty()) {
                log.debug("PKIXValidationInformation was resolved and cached by another thread "
                        + "while this thread was waiting on the write lock");
                accumulator.addAll(cachedPKIXInfo);
                return;
            }

            final PKIXValidationInformation pkixInfo = KeyAuthoritySupport.extractPKIXValidationInfo(keyAuthority);
            if (pkixInfo != null) {

                keyAuthorityObjectMetadata.put(pkixInfo);

                accumulator.add(pkixInfo);
            }

        } catch (final org.opensaml.security.SecurityException e) {
            throw new ResolverException("Error resolving PKIXValidationInformation for shibmd:KeyAuthority", e);
        } finally {
            rwlock.writeLock().unlock();
        }

    }

    /**
     * Retrieves trusted name information from the provided resolver.
     * 
     * @param criteriaSet the criteria set being processed
     * @param entityID entity ID for which to resolve trusted names
     * @param role role in which the entity is operating
     * @param protocol protocol over which the entity is operating (may be null)
     * @param usage usage specifier for role descriptor key descriptors to evaluate
     * 
     * @return collection of resolved trusted name information, possibly empty
     * 
     * @throws SecurityException thrown if there is an error extracting trusted name information
     * @throws ResolverException if we have an error getting the role descriptors
     */
    protected Set<String> retrieveTrustedNamesFromMetadata(CriteriaSet criteriaSet, String entityID, QName role,
            String protocol, UsageType usage) throws ResolverException {

        log.debug("Attempting to retrieve trusted names for PKIX validation from resolver for entity: {}", entityID);
        Set<String> trustedNames = new HashSet<>();

        Iterable<RoleDescriptor> roleDescriptors = getRoleDescriptors(criteriaSet, entityID, role, protocol);
        if (roleDescriptors == null) {
            return trustedNames;
        }

        for (final RoleDescriptor roleDescriptor : roleDescriptors) {
            final List<KeyDescriptor> keyDescriptors = roleDescriptor.getKeyDescriptors();
            for (final KeyDescriptor keyDescriptor : keyDescriptors) {
                UsageType mdUsage = keyDescriptor.getUse();
                if (mdUsage == null) {
                    mdUsage = UsageType.UNSPECIFIED;
                }
                if (matchUsage(mdUsage, usage)) {
                    if (keyDescriptor.getKeyInfo() != null) {
                        getTrustedNames(trustedNames, keyDescriptor.getKeyInfo());
                    }
                }
            }

        }

        return trustedNames;
    }

    /**
     * Extract trusted names from a KeyInfo element.
     * 
     * @param keyInfo the KeyInfo instance from which to extract trusted names
     * @param accumulator set of trusted names to return
     */
    protected void getTrustedNames(Set<String> accumulator, KeyInfo keyInfo) {
        // TODO return anything if there are things other than names in the KeyInfo ?
        accumulator.addAll(KeyInfoSupport.getKeyNames(keyInfo));
    }

    /**
     * Match usage enum type values from resolver KeyDescriptor and from specified resolution criteria.
     * 
     * @param metadataUsage the value from the 'use' attribute of a resolver KeyDescriptor element
     * @param criteriaUsage the value from specified criteria
     * @return true if the two usage specifiers match for purposes of resolving validation information, false otherwise
     */
    protected boolean matchUsage(UsageType metadataUsage, UsageType criteriaUsage) {
        if (metadataUsage == UsageType.UNSPECIFIED || criteriaUsage == UsageType.UNSPECIFIED) {
            return true;
        }
        return metadataUsage == criteriaUsage;
    }

    /**
     * Get the list of resolver role descriptors which match the given entityID, role and protocol.
     * 
     * @param criteriaSet the criteria set being processed
     * @param entityID entity ID of the resolver entity descriptor to resolve
     * @param role role in which the entity is operating
     * @param protocol protocol over which the entity is operating (may be null)
     * @return a list of role descriptors matching the given parameters, or null
     * @throws ResolverException thrown if there is an error retrieving role descriptors from the resolver provider
     */
    protected Iterable<RoleDescriptor> getRoleDescriptors(CriteriaSet criteriaSet, String entityID, QName role,
            String protocol) throws ResolverException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving role descriptor metadata for entity '{}' in role '{}' for protocol '{}'",
                        new Object[] {entityID, role, protocol});
            }

            return getRoleDescriptorResolver().resolve(criteriaSet);

        } catch (ResolverException e) {
            log.error("Unable to resolve information from metadata", e);
            throw new ResolverException("Unable to resolve unformation from metadata", e);
        }

    }

}
