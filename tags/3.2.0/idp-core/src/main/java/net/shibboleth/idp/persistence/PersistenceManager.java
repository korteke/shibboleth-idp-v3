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

package net.shibboleth.idp.persistence;

import net.shibboleth.utilities.java.support.component.IdentifiedComponent;

/**
 * Interface describing behavior for IdP components that persist and manage a particular type of stored data.
 * 
 * @param <ItemType> type of item stored and managed
 * 
 * @deprecated
 */
public interface PersistenceManager<ItemType> extends IdentifiedComponent {

    /**
     * Determines if an item with the given ID is exists within the persistence store.
     * 
     * @param id ID of the item, may be null or empty
     * 
     * @return true if an item with the given ID exists within the persistence store, false if the ID is null or empty
     *         or an item with the given ID does not exists within the persistence store
     */
    public boolean contains(String id);

    /**
     * Determines if an item exists within the persistence store.
     * 
     * @param item the item to check to see if it exists within the persistence store
     * 
     * @return true if the item exists within the persistence store, false if the item is null or does not exists within
     *         the persistence store
     */
    public boolean contains(ItemType item);

    /**
     * Gets the item associated with the given ID.
     * 
     * @param id ID of the item, may be null or empty
     * 
     * @return the item associated with the ID or null if the given ID was null or empty or no item is associated
     *         with that id
     */
    public ItemType get(String id);

    /**
     * Persists an item to the persistence store. If the item does not currently exist in the persistence store then a
     * new record will be created, otherwise the existing record will be updated. If the item to be persisted is an
     * update to an existing item then the argument to this method MUST be an updated version of the item as returned by
     * {@link #get(String)} or {@link #persist(String, Object)}.
     * 
     * Note, the item returned from this method is not necessarily the same as the one provided as an argument. A given
     * implementation of this interface may return a different object or update the provided object in some way during
     * this method call.
     * 
     * @param id the ID of the item to be persisted
     *
     * @param item item to be persisted, maybe null
     * 
     * @return the persisted item or null if the given item was null
     */
    public ItemType persist(String id, ItemType item);

    /**
     * Removes an item with the given ID from the persistence store.
     * 
     * @param id ID of the item, may be null or empty
     * 
     * @return the item that was removed or null if the given ID was null or empty or no item was associated with the
     *         given ID
     */
    public ItemType remove(String id);

    /**
     * Removes an item from the persistence store.
     * 
     * @param item the item to remove, may be null
     * 
     * @return the item that was removed or null if the given item was null or no record existed for the given item
     */
    public ItemType remove(ItemType item);

}