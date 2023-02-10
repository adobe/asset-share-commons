/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.adobe.aem.commons.assetshare.util.assetkit;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ConsumerType;

import javax.jcr.RepositoryException;

@ConsumerType
/**
 * A component updater is used to update the components on an asset kit page based on the asset kit's payload.
 */
public interface ComponentUpdater {
    /**
     * The name of the component updater. Displays in the Asset kit creator workflow dialog's dropdown.
     * @return the name of the component updater. Ideally is unique across all component updaters, so you can tell which is which.
     */
    String getName();

    /**
     * The id of the component updater. Used to identify the component updater in the workflow dialog.
     * Must be unique across all component updaters.
     * Defaults to the component's full class name. No need to change the default implementation.
     * @return
     */
    default String getId() { return this.getClass().getName(); }

    /**
     * Entry point for updating the @{code assetKitPage} based on the @{code assetKit} payload resource.
     * 
     * This can do anything, it can find and updated existing components created by the template's initial content, or it can create new resources under the page (or anywhere it has write access).
     * 
     * @param assetKitPage the asset kit page being updated.
     * @param assetKit the resource (folder, collection) that represents the asset kit's contents.
     * @throws PersistenceException
     * @throws RepositoryException
     */
    void updateComponent(Page assetKitPage, Resource assetKit) throws PersistenceException, RepositoryException;
}
