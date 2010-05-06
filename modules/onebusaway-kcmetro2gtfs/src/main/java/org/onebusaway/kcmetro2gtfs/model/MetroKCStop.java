package org.onebusaway.kcmetro2gtfs.model;

import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.kcmetro2gtfs.impl.MetroKCDateFieldMappingFactory;

import java.util.Date;

@CsvFields(filename = "stop_locations.csv")
public class MetroKCStop extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private Integer id;

  private double x;

  private double y;

  private double streetX;

  private double streetY;

  @CsvField(ignore = true)
  private double lat;

  @CsvField(ignore = true)
  private double lon;

  private int transLink;

  private int crossStreetNameId;

  @CsvField(mapping = MetroKCDateFieldMappingFactory.class)
  private Date effectiveBeginDate;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
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

  public double getStreetX() {
    return streetX;
  }

  public void setStreetX(double streetX) {
    this.streetX = streetX;
  }

  public double getStreetY() {
    return streetY;
  }

  public void setStreetY(double streetY) {
    this.streetY = streetY;
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

  public int getTransLink() {
    return transLink;
  }

  public void setTransLink(int transLink) {
    this.transLink = transLink;
  }

  public int getCrossStreetNameId() {
    return crossStreetNameId;
  }

  public void setCrossStreetNameId(int crossStreetNameId) {
    this.crossStreetNameId = crossStreetNameId;
  }

  public Date getEffectiveBeginDate() {
    return effectiveBeginDate;
  }

  public void setEffectiveBeginDate(Date effectiveBeginDate) {
    this.effectiveBeginDate = effectiveBeginDate;
  }

  @Override
  public String toString() {
    return "Stop(id=" + getId() + ")";
  }
}
