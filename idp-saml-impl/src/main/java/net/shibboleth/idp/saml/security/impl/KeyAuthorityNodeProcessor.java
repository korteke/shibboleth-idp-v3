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

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.idp.saml.security.KeyAuthoritySupport;
import net.shibboleth.idp.saml.xmlobject.KeyAuthority;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.metadata.resolver.filter.FilterException;
import org.opensaml.saml.metadata.resolver.filter.MetadataNodeProcessor;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.security.SecurityException;
import org.opensaml.security.x509.PKIXValidationInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link MetadataNodeProcessor} which supports processing the 
 * Shibboleth {@link KeyAuthority} information within a metadata document.
 */
public class KeyAuthorityNodeProcessor implements MetadataNodeProcessor {
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(KeyAuthority.class);

    /** {@inheritDoc} */
    @Override
    public void process(XMLObject metadataNode) throws FilterException {
        if (metadataNode instanceof EntitiesDescriptor) {
            handleEntitiesDescriptor((EntitiesDescriptor) metadataNode);
        } else if (metadataNode instanceof EntityDescriptor) {
            handleEntityDescriptor((EntityDescriptor) metadataNode);
        }
    }

    /**
     * Handle an {@link EntitiesDescriptor}.
     * 
     * @param entitiesDescriptor the entities descriptor being processed
     * 
     * @throws FilterException if there is a fatal error during processing
     */
    protected void handleEntitiesDescriptor(EntitiesDescriptor entitiesDescriptor) throws FilterException {
        log.debug("Processing EntitiesDescriptor with id '{}', name '{}'", 
                entitiesDescriptor.getID(), entitiesDescriptor.getName());
        
        List<XMLObject> keyAuthorities = getKeyAuthorities(entitiesDescriptor);
        if (keyAuthorities.isEmpty()) {
            return;
        }
        
        log.debug("Saw at least one KeyAuthority for EntitiesDescriptor with id '{}', name '{}'", 
                entitiesDescriptor.getID(), entitiesDescriptor.getName());
        
        for (XMLObject keyAuthority : keyAuthorities) {
            try {
                PKIXValidationInformation pkixInfo = KeyAuthoritySupport.
                        extractPKIXValidationInfo((KeyAuthority) keyAuthority);
                if (pkixInfo != null) {
                    keyAuthority.getObjectMetadata().put(pkixInfo);
                }
            } catch (SecurityException e) {
                //TODO should throw here or just log error and continue?  
                throw new FilterException("Error extracting PKIX validation info from KeyAuthority", e);
            }
        }
        
    }

    /**
     * Handle an {@link EntityDescriptor}.
     * 
     * @param entityDescriptor the entity descriptor being processed
     * 
     * @throws FilterException if there is a fatal error during processing
     */
    protected void handleEntityDescriptor(EntityDescriptor entityDescriptor) throws FilterException {
        XMLObject currentParent = entityDescriptor.getParent();
        while (currentParent != null) {
            if (currentParent instanceof EntitiesDescriptor) {
                for (XMLObject keyAuthority : getKeyAuthorities((EntitiesDescriptor) currentParent)) {
                    entityDescriptor.getObjectMetadata().putAll(keyAuthority.getObjectMetadata()
                            .get(PKIXValidationInformation.class));
                }
            }
            currentParent = currentParent.getParent();
        }
    }
    
    /**
     * Get the list of KeyAuthority's from an EntitiesDescriptor's Extensions.
     * 
     * @param entitiesDescriptor the entities descriptor to process.
     * @return list of XMLObjects
     */
    @Nonnull protected List<XMLObject> getKeyAuthorities(@Nonnull EntitiesDescriptor entitiesDescriptor) {
        Extensions extensions = entitiesDescriptor.getExtensions();
        if (extensions == null) {
            return Collections.emptyList();
        }
        
        List<XMLObject> keyAuthorities = extensions.getUnknownXMLObjects(KeyAuthority.DEFAULT_ELEMENT_NAME);
        if (keyAuthorities == null) {
            return Collections.emptyList();
        } else {
            return keyAuthorities;
        }
    }
    
}
