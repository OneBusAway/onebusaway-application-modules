package org.onebusaway.webapp.gwt.mobile_application.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Bookmark implements IsSerializable {
  
  private int id;

  private String name;

  private String stopId;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }
}
