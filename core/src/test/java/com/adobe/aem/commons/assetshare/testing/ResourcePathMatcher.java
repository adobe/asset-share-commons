/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
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

package com.adobe.aem.commons.assetshare.testing;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.mockito.ArgumentMatcher;

public class ResourcePathMatcher implements ArgumentMatcher<Resource> {
    private final String path;

    public ResourcePathMatcher(final String path) {
        this.path = path;
    }

    public boolean matches(Resource resource) {
        return resource != null && StringUtils.equals(path, resource.getPath());
    }
    public String toString() {
        //printed in verification errors
        return "[Resource path of " + this.path + "]";
    }
}
