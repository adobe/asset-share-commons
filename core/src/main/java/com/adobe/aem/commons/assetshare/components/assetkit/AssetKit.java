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
package com.adobe.aem.commons.assetshare.components.assetkit;

import com.adobe.aem.commons.assetshare.components.Component;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.osgi.annotation.versioning.ConsumerType;
import org.osgi.annotation.versioning.ProviderType;

import java.util.Collection;
@ProviderType
public interface AssetKit extends Component {
    /**
     * Returns the assets that are part of this Asset Kit.
     * The assets are returned as as AssetModels, so ComputedProperties can used to display relevant data.
     * @return a Collection of AssetModels
     */    
    Collection<? extends AssetModel> getAssets();

    /**
     * Returns true is the component is ready to display. This is used to determine if the component's Page Editor edit box should display or not.
     */
    boolean isReady();

    @ConsumerType
    /**
     * A Filter is used to filter the assets that are part of the Asset Kit.
     * 
     * This acts as an OSGi service interface that can be implemented with a service.ranking > 10000 to override default filtering behavior.
     * The Asset Share Commons provided filter removed the "banner image asset" from the asset kit listing (AssetKitFilterImpl.java).
     * If the logic for defining the banner image changes a custom Filter would need to be developed.
     */
    interface Filter {
        Collection<? extends AssetModel> filter(Collection<? extends AssetModel> assets);
    }
}
