package org.onebusaway.gtfs.model;

public final class Route extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = 1L;

  private AgencyAndId id;

  private Agency agency;

  private String shortName;

  private String longName;

  private String desc;

  private int type;

  private String url;

  private String color;

  private String textColor;

  public AgencyAndId getId() {
    return id;
  }

  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public Agency getAgency() {
    return agency;
  }

  public void setAgency(Agency agency) {
    this.agency = agency;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getLongName() {
    return longName;
  }

  public void setLongName(String longName) {
    this.longName = longName;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getTextColor() {
    return textColor;
  }

  public void setTextColor(String textColor) {
    this.textColor = textColor;
  }

  @Override
  public String toString() {
    return "<Route " + id + " " + shortName + ">";
  }
}
