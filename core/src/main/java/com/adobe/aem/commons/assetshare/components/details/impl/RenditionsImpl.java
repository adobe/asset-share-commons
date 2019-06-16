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

package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.aem.commons.assetshare.components.details.Renditions;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.Rendition;
import com.adobe.aem.commons.assetshare.content.properties.impl.LicenseImpl;
import com.adobe.aem.commons.assetshare.util.UrlUtil;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.day.cq.dam.commons.util.UIHelper;
import com.day.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Renditions.class},
        resourceType = {RenditionsImpl.RESOURCE_TYPE}
)
public class RenditionsImpl extends AbstractEmptyTextComponent implements Renditions {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/renditions";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Self
    @Required
    private AssetModel asset;

    @Self
    @Required
    private Options coreOptions;

    @ValueMapValue
    @Default(booleanValues = false)
    private boolean allowLinks;

    @ValueMapValue
    @Default(booleanValues = false)
    private boolean showMissingRenditions;

    @OSGiService
    @Required
    private MimeTypeService mimeTypeService;

    private Collection<Rendition> renditions = null;

    @PostConstruct
    protected void init() {
        coreOptions = request.adaptTo(Options.class);
    }

    @Override
    public Collection<Rendition> getRenditions() {
        return getRenditions(isShowMissingRenditions());
    }

    @Override
    public boolean isAllowLinks() {
        return allowLinks;
    }

    @Override
    public boolean isShowMissingRenditions() {
        return showMissingRenditions;
    }

    private Collection<Rendition> getRenditions(boolean includeAll) {
        if (renditions == null) {
            final List<Rendition> collectedRenditions = new ArrayList<>();
            final List<com.day.cq.dam.api.Rendition> assetRenditions = asset.getRenditions();

            for (final OptionItem item : coreOptions.getItems()) {
                final Pattern pattern = Pattern.compile(item.getValue());
                boolean found = false;

                for (final com.day.cq.dam.api.Rendition assetRendition : assetRenditions) {
                    final Matcher matcher = pattern.matcher(assetRendition.getName());

                    if (matcher.matches()) {
                        collectedRenditions.add(new RenditionImpl(item.getText(),
                                assetRendition,
                                StringUtils.isNotBlank(asset.getProperties().get(LicenseImpl.NAME, String.class)),
                                true));
                        found = true;
                    }
                }

                if (!found && includeAll) {
                    collectedRenditions.add(new MissingRenditionImpl(item.getText()));
                }
            }

            renditions = collectedRenditions;
        }

        return renditions;
    }

    @Override
    public boolean isEmpty() {
        return getRenditions().size() == 0;
    }

    @Override
    public boolean isReady() {
        return !isEmpty();
    }

    /**
     * Implementation of the Rendition interface.
     */
    private class RenditionImpl implements Rendition {
        private final String label;
        private final String size;
        private final String mimeType;
        private final String path;
        private final String name;
        private final boolean exists;
        private final boolean licensed;

        public RenditionImpl(String label, com.day.cq.dam.api.Rendition assetRendition, boolean licensed, boolean exists) {
            this.label = label;
            this.size = UIHelper.getSizeLabel(assetRendition.getSize());
            this.mimeType = assetRendition.getMimeType();
            this.path = UrlUtil.escape(assetRendition.getPath());
            this.name = assetRendition.getName();
            this.exists = exists;
            this.licensed = licensed;
        }

        public String getDownloadFileName() {
            String downloadFileName = StringUtils.removeEnd(label, ".");

            String extension = mimeTypeService.getExtension(mimeType);
            if (StringUtils.isBlank(extension)) {
                extension = StringUtils.substringAfterLast(name, ".");
                if (StringUtils.equals(extension, name)) {
                    extension = null;
                }
            }

            if (extension != null) {
                downloadFileName += "." + extension;
            }

            return downloadFileName;
        }

        public boolean isExists() {
            return exists;
        }

        public String getLabel() {
            return label;
        }

        public String getSize() {
            return size;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        public boolean isLicensed() {
            return licensed;
        }
    }

    /**
     * Private class that models a Missing rendition
     */
    private class MissingRenditionImpl implements Rendition {
        private final String label;

        public MissingRenditionImpl(String text) {
            this.label = text;
        }

        @Override
        public boolean isExists() {
            return false;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public String getSize() {
            return null;
        }

        @Override
        public String getMimeType() {
            return null;
        }

        @Override
        public String getPath() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getDownloadFileName() {
            return getLabel();
        }

        @Override
        public boolean isLicensed() {
            return false;
        }
    }

}
