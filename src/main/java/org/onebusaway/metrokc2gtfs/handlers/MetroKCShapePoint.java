/**
 * 
 */
package org.onebusaway.metrokc2gtfs.handlers;

import org.onebusaway.csv.CsvField;
import org.onebusaway.csv.CsvFields;

@CsvFields(filename = "trans_link_shape_point.csv")
public class MetroKCShapePoint implements Comparable<MetroKCShapePoint> {

  private int transLinkId;

  private double x;

  private double y;

  private int sequence;

  @CsvField(ignore = true)
  private double lat;

  @CsvField(ignore = true)
  private double lon;

  public int getTransLinkId() {
    return transLinkId;
  }

  public void setTransLinkId(int transLinkId) {
    this.transLinkId = transLinkId;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
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

  public int compareTo(MetroKCShapePoint o) {
    return this.sequence == o.sequence ? 0 : (this.sequence < o.sequence ? -1
        : 1);
  }
}