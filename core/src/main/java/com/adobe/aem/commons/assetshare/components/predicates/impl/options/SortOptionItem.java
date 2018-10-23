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
package com.adobe.aem.commons.assetshare.components.predicates.impl.options;

import com.adobe.cq.wcm.core.components.models.form.OptionItem;

public class SortOptionItem implements OptionItem {

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
