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
