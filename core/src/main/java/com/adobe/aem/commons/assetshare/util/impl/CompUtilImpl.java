/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
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

package com.adobe.aem.commons.assetshare.util.impl;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.CompUtil;
import com.adobe.granite.asset.api.AssetException;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {CompUtil.class}
)
public class CompUtilImpl implements CompUtil {
    @Self
    @Required
    private SlingHttpServletRequest request;

    @Inject
    @Required
    private AssetModel asset;

   @Override
	public String getEncodedPath() {		
		try {
			if(null == asset) {
				throw new AssetException("Asset is null", asset);	
			}
			return URLEncoder.encode(asset.getPath(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssetException("Could not Encode the path", asset.getPath());
		}
	}
}