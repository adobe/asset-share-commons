/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2020 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/* Copied from: https://github.com/Adobe-Consulting-Services/acs-aem-commons/blob/master/bundle/src/main/java/com/adobe/acs/commons/util/impl/RequireAemImpl.java */
package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.cq.dam.download.api.DownloadService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This OSGi Service/Component will only exist on AEM as a Cloud Service/SDK since this is where the Download Service exists.
 */
@Component(
        service = { RequireAemCanary.class }
)
public class RequireAemCanaryImpl implements RequireAemCanary {
    @Reference
    DownloadService downloadService;
}
