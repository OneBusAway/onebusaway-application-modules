package org.onebusaway.webapp.gwt.viewkit;

public class NavigationItem {

  private String id;

  private String title;

  private BarButtonItem leftBarButtonItem;

  private BarButtonItem rightBarButtonItem;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public BarButtonItem getLeftBarButtonItem() {
    return leftBarButtonItem;
  }

  public void setLeftBarButtonItem(BarButtonItem leftBarButtonItem) {
    this.leftBarButtonItem = leftBarButtonItem;
  }

  public BarButtonItem getRightBarButtonItem() {
    return rightBarButtonItem;
  }

  public void setRightBarButtonItem(BarButtonItem rightBarButtonItem) {
    this.rightBarButtonItem = rightBarButtonItem;
  }
}
