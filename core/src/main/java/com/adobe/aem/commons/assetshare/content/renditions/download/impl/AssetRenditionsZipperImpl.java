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

package com.adobe.aem.commons.assetshare.content.renditions.download.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.download.AssetRenditionStreamer;
import com.adobe.aem.commons.assetshare.content.renditions.download.AssetRenditionsDownloadOrchestrator;
import com.adobe.aem.commons.assetshare.content.renditions.download.AssetRenditionsException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Designate(ocd = AssetRenditionsZipperImpl.Cfg.class)
public class AssetRenditionsZipperImpl implements AssetRenditionsDownloadOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(AssetRenditionsZipperImpl.class);

    private static final String DEFAULT_FILE_ATTACHMENT_NAME = "Assets.zip";

    public static final String NAME = "asset-share-commons__download-orchestrator--zip-packer";

    private static final long BYTES_IN_MB = 1024;

    public static final String VAR_ASSET_NAME = "${asset.name}";
    public static final String VAR_ASSET_EXTENSION = "${asset.extension}";
    public static final String VAR_RENDITION_NAME = "${rendition.name}";
    public static final String VAR_RENDITION_EXTENSION = "${rendition.extension}";
    public static final String VAR_ASSET_FILE_NAME = "${asset.filename}";
    public static final String VAR_ASSET_TITLE = "${asset.title}";

    private static final String PN_NO_CONTENT_FILE_NAME = "noContentFileName";
    private static final String PN_NO_CONTENT_MESSAGE = "noContentMessage";
    private static final String PN_FILE_NAME = "fileName";
    public static final String ZIP_EXTENSION = ".zip";
    public static final String DEFAULT_NO_CONTENT_FILE_NAME = "NO DOWNLOADABLE RENDITIONS.txt";
    public static final String DEFAULT_NO_CONTENT_MESSAGE = "Sorry, we could not find any downloadable renditions for the selected assets / renditions combinations.";

    @Reference
    private AssetRenditionStreamer assetRenditionStreamer;

    @Reference
    private MimeTypeService mimeTypeService;

    private Cfg cfg;

    @Override
    public void execute(final SlingHttpServletRequest request,
                        final SlingHttpServletResponse response,
                        final List<AssetModel> assets,
                        final List<String> renditionNames) throws IOException {
        final String filename = StringUtils.defaultIfBlank(getFileName(request.getResource().getValueMap()), DEFAULT_FILE_ATTACHMENT_NAME);

        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setContentType(JcrPackage.MIME_TYPE);

        final ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());

        final boolean groupAssetRenditionsByFolder = true;

        long size = 0L;

        final Set<String> zipEntryFileNames = new HashSet<>();
        final Set<String> zipEntryFolderNames = new HashSet<>();

        for (final AssetModel asset : assets) {
            String folderName = null;

            if (groupAssetRenditionsByFolder) {
                folderName = generateUniqueZipEntry(asset.getName(), zipEntryFolderNames);
            }

            for (final String renditionName : renditionNames) {
                AssetRenditionStreamer.AssetRenditionStream stream = null;

                try {
                    stream = assetRenditionStreamer.getAssetRendition(request, response, asset, renditionName);

                    if (stream.getOutputStream().size() > 0) {
                        size += stream.getOutputStream().size();

                        checkForMaxSize(size);

                        if (folderName != null && !zipEntryFolderNames.contains(folderName)) {
                            addFolderAsZipEntry(folderName + "/", zipOutputStream);
                            zipEntryFolderNames.add(folderName);
                        }

                        final String zipEntryName = getZipEntryName(folderName, asset, renditionName, stream.getContentType(), zipEntryFileNames);

                        addAssetRenditionAsZipEntry(folderName, zipEntryName, zipOutputStream, stream.getOutputStream());
                    }
                } catch (AssetRenditionsException ex) {
                    log.error("Unable to obtain the AssetRendition as an output stream. Skipping...", ex);
                    continue;
                } catch (IOException ex) {
                    log.error("Unable to add entry to Zip that is streamed to HTTP Response. Skipping...", ex);
                    continue;
                } finally {
                    if (stream != null && stream.getOutputStream() != null) {
                        stream.getOutputStream().close();
                    }
                }
            }
        }

        if (size == 0) {
            final String fileName = request.getResource().getValueMap().get(PN_NO_CONTENT_FILE_NAME, String.class);
            String message = request.getResource().getValueMap().get(PN_NO_CONTENT_MESSAGE, String.class);
            addNoContentFile(fileName, message, zipOutputStream);
        }

        zipOutputStream.close();
    }

    @Override
    public boolean accepts(SlingHttpServletRequest request, List<AssetModel> assets, List<String> renditionNames) {
        return NAME.equals(request.getRequestParameter(REQUEST_PARAMETER_NAME).getString());
    }

    protected void checkForMaxSize(long size) throws AssetRenditionsException {
        if (cfg.max_size() >= 0 && size > cfg.max_size() * BYTES_IN_MB) {
            throw new AssetRenditionsException("Selected assets exceed maximum allows size.");
        }
    }

    protected String getFileName(final ValueMap properties) {
        String fileName = properties.get(PN_FILE_NAME, StringUtils.defaultString(cfg.file_name(), DEFAULT_FILE_ATTACHMENT_NAME));

        if (!StringUtils.endsWith(fileName, ZIP_EXTENSION)) {
            fileName += ZIP_EXTENSION;
        }
        return fileName;
    }

    protected String getZipEntryName(final String folderName, final AssetModel asset, final String renditionName,
                                     final String responseContentType, final Set<String> zipEntryFileNames) {
        final String extension = mimeTypeService.getExtension(responseContentType);

        final Map<String, String> variables = new LinkedHashMap<>();

        variables.put(VAR_ASSET_FILE_NAME, asset.getName());
        variables.put(VAR_ASSET_NAME, StringUtils.substringBeforeLast(asset.getName(), "."));
        variables.put(VAR_ASSET_TITLE, asset.getTitle());
        variables.put(VAR_ASSET_EXTENSION, StringUtils.substringAfterLast(asset.getName(), "."));
        variables.put(VAR_RENDITION_NAME, renditionName);
        variables.put(VAR_RENDITION_EXTENSION, extension);

        String zipEntryName = StringUtils.replaceEach(cfg.rendition_filename_expression(),
                variables.keySet().toArray(new String[variables.keySet().size()]),
                variables.values().toArray(new String[variables.values().size()]));

        zipEntryName = generateUniqueZipEntry(zipEntryName, zipEntryFileNames);
        if (folderName != null) {
            zipEntryFileNames.add(folderName + "/" + zipEntryName);
        } else {
            zipEntryFileNames.add(zipEntryName);
        }

        return zipEntryName;
    }

    private String generateUniqueZipEntry(final String zipEntryName, final Set<String> existingZipEntryNames ) {
        String tmpZipEntryName = zipEntryName;
        int count = 1;

        while (existingZipEntryNames.contains(tmpZipEntryName)) {
            tmpZipEntryName = count++ + "-" + zipEntryName;
        }

        return tmpZipEntryName;
    }

    private void addFolderAsZipEntry(final String folderName,
                                             final ZipOutputStream zipOutputStream) throws IOException {

        final ZipEntry zipEntry = new ZipEntry(folderName);
        zipOutputStream.putNextEntry(zipEntry);
    }

    private void addAssetRenditionAsZipEntry(final String prefix,
                                             String zipEntryName,
                                             final ZipOutputStream zipOutputStream,
                                             final ByteArrayOutputStream assetRenditionOutputStream) throws IOException {
        if (StringUtils.isNotBlank(prefix)) {
            zipEntryName = prefix + "/" +  zipEntryName;
        }

        final ZipEntry zipEntry = new ZipEntry(zipEntryName);
        zipOutputStream.putNextEntry(zipEntry);
        IOUtils.write(assetRenditionOutputStream.toByteArray(), zipOutputStream);
        zipOutputStream.closeEntry();
        assetRenditionOutputStream.close();
    }

    private void addNoContentFile(String fileName, String message, final ZipOutputStream zipOutputStream) throws IOException {
        fileName = StringUtils.defaultString(fileName, DEFAULT_NO_CONTENT_FILE_NAME);
        message = StringUtils.defaultString(message, DEFAULT_NO_CONTENT_MESSAGE);

        final ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);
        IOUtils.write(message, zipOutputStream, "UTF-8");
        zipOutputStream.closeEntry();
    }

    @Activate
    protected void activate(final Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Asset Renditions Zipper")
    public @interface Cfg {
        @AttributeDefinition
        String webconsole_configurationFactory_nameHint() default "{zip_filename_expression} with max size in MB {max.size}";

        @AttributeDefinition(
                name = "Filename of packed asset renditions",
                description = "The default value for the zip to be downloaded."
        )
        String file_name() default "Assets.zip";

        @AttributeDefinition(
                name = "Max Size (in MB)",
                description = "The max size (in MB; 1024 bytes) of source (pre-zipped) files allowed to be zipped."
        )
        long max_size() default -1L;

        @AttributeDefinition(
                name = "Rendition Filename Expression",
                description = "The expression that defines how the filename for each entry in the zip file is constructed."
        )
        String rendition_filename_expression() default "${asset.name}__${rendition.name}.${rendition.extension}";
    }
}
