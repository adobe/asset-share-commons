package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.async.download.impl.AsyncDownloadImpl;
import com.adobe.aem.commons.assetshare.util.DownloadHelper;
import com.adobe.aem.commons.assetshare.util.MimeTypeHelper;
import com.day.cq.dam.api.Rendition;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component(service = DownloadHelper.class)
public class DownloadHelperImpl implements DownloadHelper {

	private static final Logger log = LoggerFactory.getLogger(MimeTypeHelper.class);

	private static final String REQ_KEY_ASSET_PATHS = "path";
	private static final String PN_ALLOWED_RENDITION_NAMES = "allowedRenditionNames";

	@Reference
	private ModelFactory modelFactory;

	public List<AssetModel> getAssets(final SlingHttpServletRequest request) {
		final RequestParameter[] requestParameters = request.getRequestParameters(REQ_KEY_ASSET_PATHS);

		if (requestParameters == null) {
			return EMPTY_LIST;
		}

		return Arrays.stream(requestParameters).map(RequestParameter::getString)
				.map(path -> request.getResourceResolver().getResource(path)).filter(Objects::nonNull)
				.map(resource -> modelFactory.getModelFromWrappedRequest(request, resource, AssetModel.class))
				.filter(Objects::nonNull).collect(Collectors.toList());
	}

	public List<String> getRenditionNames(final SlingHttpServletRequest request, String rendition) {
		final String[] allowedRenditionNames = request.getResource().getValueMap().get(PN_ALLOWED_RENDITION_NAMES,
				new String[] {});

		if (allowedRenditionNames == null) {
			return EMPTY_LIST;
		}

		final RequestParameter[] requestParameters = request.getRequestParameters(rendition);
		if (requestParameters != null) {
			return Arrays.stream(requestParameters).map(RequestParameter::getString)
					.filter(renditionName -> allowedRenditionNames.length == 0
					|| ArrayUtils.contains(allowedRenditionNames, renditionName))
					.distinct().collect(Collectors.toList());
		} else {
			return emptyList();
		}
	}
}
