package com.adobe.aem.commons.assetshare.components.predicates.impl.options;

public class SortOptionItem {

  private boolean selected;

  private String text;

  private String value;

  private boolean caseSensitive;

  public SortOptionItem(String text, String value, boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
    this.text = text;
    this.value = value;
  }

  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public String getValue() {
    return value;
  }

  public String getText() {
    return text;
  }
}
