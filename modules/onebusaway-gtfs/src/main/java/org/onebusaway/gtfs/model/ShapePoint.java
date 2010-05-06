package org.onebusaway.gtfs.model;

public final class ShapePoint extends IdentityBean<Integer> implements Comparable<ShapePoint> {

  private static final long serialVersionUID = 1L;

  private int id;

  private AgencyAndId shapeId;

  private int sequence;

  private double distTraveled;

  private double lat;

  private double lon;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public AgencyAndId getShapeId() {
    return shapeId;
  }

  public void setShapeId(AgencyAndId shapeId) {
    this.shapeId = shapeId;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
  }

  public double getDistTraveled() {
    return distTraveled;
  }

  public void setDistTraveled(double distTraveled) {
    this.distTraveled = distTraveled;
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
 
  @Override
  public String toString() {
     return "<ShapePoint "+this.shapeId+" #"+sequence+" ("+this.getLat()+","+this.getLon()+")>";
  }

  @Override
  public int compareTo(ShapePoint o) {
    return this.sequence - o.sequence;
  }
}
