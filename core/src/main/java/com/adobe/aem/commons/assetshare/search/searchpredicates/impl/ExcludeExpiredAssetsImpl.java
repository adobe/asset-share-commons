package com.adobe.aem.commons.assetshare.search.searchpredicates.impl;

import com.adobe.aem.commons.assetshare.search.searchpredicates.SearchPredicate;
import com.day.cq.search.PredicateConverter;
import com.day.cq.search.PredicateGroup;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.HashMap;
import java.util.Map;

@Component(service = SearchPredicate.class)
@Designate(ocd = ExcludeExpiredAssetsImpl.Cfg.class)
public class ExcludeExpiredAssetsImpl implements SearchPredicate {
    private static final String PN_EXPIRATION_DATE = "jcr:content/metadata/prism:expirationDate";

    public static final String LABEL = "Exclude expired assets";
    public static final String NAME = "exclude-expired-assets";

    private Cfg cfg;

    @Override
    public String getLabel() {
        return cfg.label();
    }

    @Override
    public String getName() {
        return NAME;
    }

    public PredicateGroup getPredicateGroup(SlingHttpServletRequest request) {
        final Map<String, String> params = new HashMap<>();

        params.put("group.property", PN_EXPIRATION_DATE);
        params.put("group.property.operation", "not");
        params.put("group.p.or", "true");
        params.put("group.relativedaterange.property", PN_EXPIRATION_DATE);
        params.put("group.relativedaterange.lowerBound", "0d");

        return PredicateConverter.createPredicates(params);
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Global Predicate - " + NAME)
    public @interface Cfg {
        @AttributeDefinition(
                name = "Label",
                description = "Human readable label."
        )
        String label() default LABEL;
    }
}
