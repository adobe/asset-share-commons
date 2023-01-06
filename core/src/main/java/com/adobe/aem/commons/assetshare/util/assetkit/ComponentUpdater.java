package com.adobe.aem.commons.assetshare.util.assetkit;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;

import javax.jcr.RepositoryException;

public interface ComponentUpdater {

    String getName();

    default String getId() { return this.getClass().getName(); }

    void updateComponent(Page assetKitPage, Resource assetKit) throws PersistenceException, RepositoryException;
}
