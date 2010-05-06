package org.onebusaway.gtfs.model;

public final class Stop extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = 1L;

  private AgencyAndId id;

  private String code;

  private String name;

  private String desc;

  private double lat;

  private double lon;

  private String zoneId;

  private String url;

  private int locationType;

  private String parentStation;

  private Double direction;

  public AgencyAndId getId() {
    return id;
  }

  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public String getZoneId() {
    return zoneId;
  }

  public void setZoneId(String zoneId) {
    this.zoneId = zoneId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getLocationType() {
    return locationType;
  }

  public void setLocationType(int locationType) {
    this.locationType = locationType;
  }

  public String getParentStation() {
    return parentStation;
  }

  public void setParentStation(String parentStation) {
    this.parentStation = parentStation;
  }

  public Double getDirection() {
    return direction;
  }

  public void setDirection(Double direction) {
    this.direction = direction;
  }

  @Override
  public String toString() {
    return "<Stop " + this.id + ">";
  }
}
