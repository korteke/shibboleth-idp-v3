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

package net.shibboleth.idp.saml.nameid;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.annotation.Nullable;

/** Object representing a persistent identifier entry in storage. */
public class PersistentIdEntry implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = -8711779466442306767L;

    /** ID of the entity that issued that identifier. */
    @Nullable private String issuerEntityId;

    /** ID of the entity to which the identifier was issued. */
    @Nullable private String recipientEntityId;

    /** Name of the principal represented by the identifier. */
    @Nullable private String principalName;

    /** Underlying source ID of the entry. */
    @Nullable private String sourceId;

    /** The persistent identifier. */
    @Nullable private String persistentId;

    /** ID, associated with the persistent identifier, provided by the peer. */
    @Nullable private String peerProvidedId;

    /** Time the identifier was created. */
    @Nullable private Timestamp creationTime;

    /** Time the identifier was deactivated. */
    @Nullable private Timestamp deactivationTime;

    /**
     * Get the ID of the entity that issued the identifier.
     * 
     * @return ID of the entity that issued the identifier
     */
    @Nullable public String getIssuerEntityId() {
        return issuerEntityId;
    }

    /**
     * Set the ID of the entity that issued the identifier.
     * 
     * @param id ID of the entity that issued the identifier
     */
    public void setIssuerEntityId(@Nullable final String id) {
        issuerEntityId = id;
    }

    /**
     * Get the ID of the entity to which the identifier was issued.
     * 
     * @return ID of the entity to which the identifier was issued
     */
    @Nullable public String getRecipientEntityId() {
        return recipientEntityId;
    }

    /**
     * Set the ID of the entity to which the identifier was issued.
     * 
     * @param id ID of the entity to which the identifier was issued
     */
    public void setRecipientEntityId(@Nullable final String id) {
        recipientEntityId = id;
    }

    /**
     * Get the name of the principal the identifier represents.
     * 
     * @return name of the principal the identifier represents
     */
    @Nullable public String getPrincipalName() {
        return principalName;
    }

    /**
     * Set the name of the principal the identifier represents.
     * 
     * @param name name of the principal the identifier represents
     */
    public void setPrincipalName(@Nullable final String name) {
        principalName = name;
    }

    /**
     * Get the source ID underlying the persistent identifier.
     * 
     * @return source ID underlying the persistent identifier
     */
    @Nullable public String getSourceId() {
        return sourceId;
    }

    /**
     * Set the source ID underlying the persistent identifier.
     * 
     * @param id source ID underlying the persistent identifier
     */
    public void setSourceId(@Nullable final String id) {
        sourceId = id;
    }

    /**
     * Get the persistent identifier.
     * 
     * @return the persistent identifier
     */
    @Nullable public String getPersistentId() {
        return persistentId;
    }

    /**
     * Set the persistent identifier.
     * 
     * @param id the persistent identifier
     */
    public void setPersistentId(@Nullable final String id) {
        persistentId = id;
    }

    /**
     * Get the alias, provided by the recipient, attached to this ID.
     * 
     * @return alias, provided by the recipient, associated with this ID
     */
    @Nullable public String getPeerProvidedId() {
        return peerProvidedId;
    }

    /**
     * Set the alias, provided by the recipient, attached to this ID.
     * 
     * @param id alias, provided by the recipient, attached to this ID
     */
    public void setPeerProvidedId(@Nullable final String id) {
        peerProvidedId = id;
    }

    /**
     * Get the time the identifier was created.
     * 
     * @return time the identifier was created
     */
    @Nullable public Timestamp getCreationTime() {
        return creationTime;
    }

    /**
     * Set the time the identifier was created.
     * 
     * @param time time the identifier was created
     */
    public void setCreationTime(@Nullable final Timestamp time) {
        creationTime = time;
    }

    /**
     * Get the time the identifier was deactivated.
     * 
     * @return time the identifier was deactivated
     */
    @Nullable public Timestamp getDeactivationTime() {
        return deactivationTime;
    }

    /**
     * Set the time the identifier was deactivated.
     * 
     * @param time the time the identifier was deactivated
     */
    public void setDeactivationTime(@Nullable final Timestamp time) {
        deactivationTime = time;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder stringForm = new StringBuilder("PersistentIdEntry{");
        stringForm.append("persistentId:").append(persistentId).append(", ");
        stringForm.append("localEntityId:").append(issuerEntityId).append(", ");
        stringForm.append("recipientEntityId:").append(recipientEntityId).append(", ");
        stringForm.append("sourceId:").append(sourceId).append(", ");
        stringForm.append("principalName:").append(principalName).append(", ");
        stringForm.append("peerProvidedId:").append(peerProvidedId).append(", ");
        stringForm.append("creationTime:").append(creationTime).append(", ");
        stringForm.append("deactivationTime:").append(deactivationTime).append(", ");
        stringForm.append("}");
        return stringForm.toString();
    }
    
}