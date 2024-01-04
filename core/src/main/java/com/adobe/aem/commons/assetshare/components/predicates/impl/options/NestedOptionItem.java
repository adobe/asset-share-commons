package com.adobe.aem.commons.assetshare.components.predicates.impl.options;

import com.adobe.cq.wcm.core.components.models.form.OptionItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class NestedOptionItem implements OptionItem {
    protected boolean selected;
    protected String text;
    protected String value;
    protected String name;
    protected String path;
    protected int index;

    protected List<NestedOptionItem> children;

    public NestedOptionItem(String path, int index, String text, String value, boolean selected) {
        this.path = path;
        this.index = index;
        this.name = getPathName(path);
        this.text = StringUtils.defaultIfBlank(text, this.name);
        this.value = value;
        this.selected = selected;
    }
    public String getName() {
        return name;
    }
    public String getPath() {
        return path;
    }
    public int getIndex() {
        return index;
    }
    public List<NestedOptionItem> getChildren() {
        return children;
    }
    public void setChildren(List<NestedOptionItem> children) {
        this.children = children;
    }
    public void addChild(NestedOptionItem child) {
        if (children == null) setChildren(new ArrayList<>());
        children.add(child);
    }    
    public void removeChild(NestedOptionItem child) {
        if (CollectionUtils.isEmpty(children)) return;

        children.remove(child);
        if (children.isEmpty()) setChildren(null);
    }
    public String getPathParent() {
        return getPathParent(path);
    }
    public String getPathName() {
        return getPathName(path);
    }
    public boolean isPathParent(NestedOptionItem other) {
        return isPathParent(this.path, other.path);
    }
    
    public void sortChildren() {
        sortItems(children);
    }

    public boolean hasChildren() {
        return CollectionUtils.isNotEmpty(children);
    }
    
    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean isDisabled() {
        return false;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getText() {
        return text;
    }
    public static String getPathName(String path) {
        if (StringUtils.isBlank(path)) return null;

        return path.substring(path.lastIndexOf("/") + 1);
    }
    public static String getPathParent(String path) {
        if (StringUtils.isBlank(path)) return null;

        return path.substring(0, path.lastIndexOf("/"));
    }
    public static boolean isPathParent(String path, String other) {
        if (StringUtils.isAnyBlank(path, other)) return false;

        return other.startsWith(path) && other.lastIndexOf("/") == path.length();
    }
    public static void sortItems(List<NestedOptionItem> items) {
        if (CollectionUtils.isEmpty(items)) return;

        items.sort(Comparator.comparing(NestedOptionItem::getText, String.CASE_INSENSITIVE_ORDER));
    }
}