package com.adobe.aem.commons.assetshare.components.predicates;

import com.adobe.aem.commons.assetshare.components.predicates.impl.options.NestedOptionItem;
import com.adobe.aem.commons.assetshare.util.PredicateUtil;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.commons.util.DamUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import javax.jcr.query.Query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractNestedPredicate extends AbstractPredicate implements NestedPredicate { 

    @Self
    @Required
    protected SlingHttpServletRequest request;

    @Self
    @Required
    protected Options coreOptions;

    @ValueMapValue
    protected String[] rootPaths;

    @ValueMapValue
    protected boolean merge;
    
    // @ValueMapValue
    // protected boolean propagate;

    protected String valueFromRequest;
    protected ValueMap valuesFromRequest;

    @PostConstruct
    protected void init() {
        initPredicate(request, coreOptions);
    }
  
    @Override
    public List<NestedOptionItem> getItems() {
        final List<NestedOptionItem> items = new ArrayList<>();
        final AtomicInteger counter = new AtomicInteger();

        for (String path : rootPaths) {
            NestedOptionItem item = getRootItem(path, counter);

            if (merge && item.hasChildren()) {
                items.addAll(item.getChildren());

            } else {
                items.add(item);
            }
        }
        sortItems(items);

        return items;
    }

    public NestedOptionItem getRootItem(String rootPath, AtomicInteger counter) {
        final Resource ancestor = request.getResourceResolver().getResource(rootPath);
        final Iterator<Resource> descendants = request.getResourceResolver().findResources(getDescendantsQuery(ancestor), Query.JCR_SQL2);

        return createItems(ancestor, descendants, counter);
    }

    public NestedOptionItem createItems(Resource ancestor, Iterator<Resource> descendants, AtomicInteger counter) {
        final List<NestedOptionItem> all = new ArrayList<>();
        final NestedOptionItem root = createItem(ancestor, null, counter);

        all.add(root);
        descendants.forEachRemaining(descendant -> all.add(createItem(descendant, ancestor, counter)));

        all.forEach(item -> {
            all.forEach(kin -> {
                if (item.isPathParent(kin)) item.addChild(kin);
            });
            sortItems(item.getChildren());
        });
        return root;
    }

    public NestedOptionItem createItem(Resource resource, Resource ancestor, AtomicInteger counter) {
        final int index = counter.getAndIncrement();
        final String path = getItemPath(resource, ancestor, index);
        final String text = getItemText(resource, ancestor, index);
        final String value = getItemValue(resource, ancestor, index);
        final boolean selected = PredicateUtil.isOptionInInitialValues(value, getInitialValues());

        return new NestedOptionItem(path, index, text, value, selected);
    }
    public String getDescendantsQuery(Resource ancestor) {
        return "select * from [" + getDescendantsType(ancestor) + "] where isDescendantNode([" + ancestor.getPath() + "])";
    }
    public String getDescendantsType(Resource ancestor) {
        return ancestor.getValueMap().get(JcrConstants.JCR_PRIMARYTYPE, String.class);
    }
    public String getItemText(Resource resource, Resource ancestor, int index) {
        return StringUtils.defaultIfBlank(DamUtil.getTitle(resource), resource.getName());
    }
    public String getItemPath(Resource resource, Resource ancestor, int index) {
        return resource.getPath();
    } 
    public String getItemValue(Resource resource, Resource ancestor, int index) {
        return getItemPath(resource, ancestor, index);
    }    
    public void sortItems(List<NestedOptionItem> items) {
        NestedOptionItem.sortItems(items);
    }
    
    @Override
    public String getInputName() {
        String valuesKey = getValuesKey();
        if (valuesKey != null) {
            return getGroup() + "." + getName() + ".{0}_" + valuesKey;
        }
        return getGroup() + ".{0}_" + getName();
    }
    @Override
    public String getProperty() {
        return null;
    }    
    @Override
    public String getValuesKey() {
        return null;
    }
    @Override
    public boolean hasOr() {
        return true;
    }
    // @Override
    // public boolean getPropagate() {
    //     return propagate;
    // }

    //AbstractPredicate
    @Override
    public boolean isReady() {
        return rootPaths != null && rootPaths.length > 0;
    }
    @Override
    public String getInitialValue() {
        if (valueFromRequest == null) {
            valueFromRequest = PredicateUtil.getInitialValue(request, this, getValuesKey());
        }

        return valueFromRequest;
    }
    @Override
    public ValueMap getInitialValues() {
        if (valuesFromRequest == null) 
            valuesFromRequest = PredicateUtil.getInitialValues(request, this, getValuesKey());

        return valuesFromRequest;
    } 
}