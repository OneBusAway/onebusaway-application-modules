/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.webapp.gwt.viewkit;

import com.google.gwt.user.client.ui.Widget;

public class ListViewRow {

  public static enum ListViewRowStyle {
    DEFAULT, DETAIL
  }

  private ListViewRowStyle style = ListViewRowStyle.DEFAULT;

  private String text;

  private String detailText;

  private Widget customView;

  public ListViewRowStyle getStyle() {
    return style;
  }

  public void setStyle(ListViewRowStyle style) {
    this.style = style;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getDetailText() {
    return detailText;
  }

  public void setDetailText(String detailText) {
    this.detailText = detailText;
  }

  public Widget getCustomView() {
    return customView;
  }

  public void setCustomView(Widget customView) {
    this.customView = customView;
  }

}
