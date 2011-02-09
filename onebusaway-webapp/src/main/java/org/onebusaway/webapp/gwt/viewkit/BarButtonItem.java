package org.onebusaway.webapp.gwt.viewkit;

import com.google.gwt.event.dom.client.ClickHandler;

public class BarButtonItem {

  public enum EBarButtonSystemItem {
    CROSS_HAIRS, REFRESH
  }

  private ClickHandler clickHandler;

  private EBarButtonSystemItem systemItem = null;

  private String title;

  private BarButtonItem(ClickHandler clickHandler) {
    this.clickHandler = clickHandler;
  }

  public BarButtonItem(EBarButtonSystemItem systemItem,
      ClickHandler clickHandler) {
    this(clickHandler);
    this.systemItem = systemItem;
  }

  public BarButtonItem(String title, ClickHandler clickHandler) {
    this(clickHandler);
    this.title = title;
  }

  public ClickHandler getClickHandler() {
    return clickHandler;
  }

  public void setClickHandler(ClickHandler clickHandler) {
    this.clickHandler = clickHandler;
  }

  public EBarButtonSystemItem getSystemItem() {
    return systemItem;
  }

  public void setSystemItem(EBarButtonSystemItem systemItem) {
    this.systemItem = systemItem;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
