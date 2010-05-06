package org.onebusaway.gtfs.model;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.onebusaway.common.model.IdentityBean;
import org.onebusaway.csv.CsvField;
import org.onebusaway.csv.CsvFields;
import org.onebusaway.gtfs.serialization.RouteAgencyFieldMappingFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "gtfs_routes")
@AccessType("field")
@org.hibernate.annotations.Entity(mutable = false)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@CsvFields(filename = "routes.txt", prefix = "route_")
public class Route extends IdentityBean<String> {

  private static final long serialVersionUID = 1L;

  @Id
  @AccessType("property")
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @CsvField(name = "agency_id", mapping = RouteAgencyFieldMappingFactory.class, optional = true)
  private Agency agency;

  private String shortName;

  @CsvField(optional = true)
  private String longName;

  // "desc" is a mysql reserved keyword
  @Column(name = "description")
  @CsvField(optional = true)
  private String desc;

  private int type;

  @CsvField(optional = true)
  private String url;

  @CsvField(optional = true)
  private String color;

  @CsvField(optional = true)
  private String textColor;

  public String getId() {
    return id;
  }

  public void setId(String id) {
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
    return this.shortName;
  }
}
