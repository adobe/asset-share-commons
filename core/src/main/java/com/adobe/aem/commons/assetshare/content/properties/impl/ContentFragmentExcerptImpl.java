package com.adobe.aem.commons.assetshare.content.properties.impl;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.adobe.aem.commons.assetshare.content.properties.AbstractComputedProperty;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.day.cq.dam.api.Asset;

@Component(service = ComputedProperty.class)
@Designate(ocd = ContentFragmentExcerptImpl.Cfg.class)
public class ContentFragmentExcerptImpl extends AbstractComputedProperty<String> {

    public static final String LABEL = "Content Fragment Excerpt";
    public static final String NAME = "contentFragmentExcerpt";

    @Reference(target = "(component.name=com.adobe.aem.commons.assetshare.content.properties.impl.AssetTypeImpl)")
    ComputedProperty<String> assetType;

    private Cfg cfg;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return cfg.label();
    }

    @Override
    public String[] getTypes() {
        return cfg.types();
    }

    @Override
    public String get(final Asset asset) {
        if ("CONTENT-FRAGMENT".equals(assetType.get(asset))) {
            return getDisplayableExcerpt(asset);
        }

        return StringUtils.EMPTY;
    }

    private String getDisplayableExcerpt(final Asset asset) {
        String excerpt;
        final Resource assetResource = asset.adaptTo(Resource.class);
        final ContentFragment contentFragment = assetResource.adaptTo(ContentFragment.class);
        final Iterator<ContentElement> elements = contentFragment.getElements();
        excerpt = getCFExcerpt(elements, 200);
        if (StringUtils.isNotEmpty(excerpt)) {
            final boolean hasMultipleElements = excerpt.contains("<hr/>");
            final String excerptClass = hasMultipleElements ? "excerpt-elements" : "excerpt";
            excerpt = "<div class=\"" + excerptClass + "\">" + excerpt;
        }
        return excerpt;
    }

    /**
     * Gets excerpt (up to specified number of characters) of a given Content Element.
     *
     * @param elements
     *            Content Element Iterator
     * @param maxChars
     *            max characters
     * @return excerpt
     */
    private String getCFExcerpt(final Iterator<ContentElement> elements, final int maxChars) {
        String excerpt = "";

        while (elements.hasNext() && excerpt.length() < maxChars) {
            final ContentElement ce = elements.next();
            if (ce != null) {
                String ceExcerpt = ce.getContent();
                ceExcerpt = ceExcerpt.replaceAll("\\<[^>]*>", "").replaceAll("(&nbsp;|\t)", " ").replaceAll(" +", " ")
                        .replaceAll("^ +$", "").replaceAll("( *\\n)+", "\n").trim();
                excerpt = excerpt.concat(ceExcerpt);

                if (maxChars < excerpt.length()) {
                    final char charAt = excerpt.charAt(maxChars);

                    excerpt = excerpt.substring(0, maxChars);

                    /* trim remaining letters if a word was cut in the middle */
                    if (charAt != ' ') {
                        final int end = excerpt.lastIndexOf(' ');

                        excerpt = end == -1 ? excerpt : excerpt.substring(0, end);
                    }
                    excerpt = excerpt.concat("...");
                } else if (!StringUtils.isEmpty(ceExcerpt) && elements.hasNext()) {
                    excerpt = excerpt.concat("\n");
                }
            }
        }
        excerpt = excerpt.replaceAll("\\n", "<br/> <hr/>");
        return excerpt;
    }

    @Activate
    protected void activate(final Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Computed Property - CF Thumbnail Rendition")
    public @interface Cfg {
        @AttributeDefinition(name = "Label", description = "Human read-able label.")
        String label() default LABEL;

        @AttributeDefinition(
                name = "Types",
                description = "Defines the type of data this exposes. This classification allows for intelligent exposure of Computed Properties in DataSources, etc.")
        String[] types() default { Types.CF_RENDITION };
    }
}
