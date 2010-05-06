/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common.model;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.vividsolutions.jts.geom.Point;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "transit_stop_locations")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@AccessType("field")
public class StopLocation extends IdentityBean implements IHasLocation {

  private static final long serialVersionUID = 1L;

  /**
   * We make the id a "property" access element such that a call to
   * {@link #getId()} when the object is proxied does not result in proxy
   * insantiation
   */
  @Id
  @AccessType("property")
  private int id;

  @Column(length = 2)
  private String bay;

  @ManyToOne(fetch = FetchType.EAGER)
  private StreetName mainStreetName;

  @ManyToOne(fetch = FetchType.EAGER)
  private StreetName crossStreetName;

  private double displacement;

  private Date effectiveBeginDate;

  private Date effectiveEndDate;

  private int fromCrossCurb;

  private int fromIntersectionCenter;

  private String gisJurisdictionCode;

  @Column(length = 3)
  private int gisZipCode;

  private double mappedLinkLen;

  private double mappedPercentFrom;

  private double mappedTransNodeFrom;

  @Column(length = 1)
  private String rideFreeArea;

  private int side;

  @Column(length = 1)
  private String sideCross;

  @Column(length = 1)
  private String sideOn;

  @Column(length = 40)
  private String streetAddressComment;

  @ManyToOne(fetch = FetchType.LAZY)
  private TransLink transLink;

  @Type(type = "org.hibernatespatial.GeometryUserType")
  @Column(columnDefinition = "GEOMETRY")
  @Index(name = "location")
  private Point location;

  @Type(type = "org.hibernatespatial.GeometryUserType")
  @Column(columnDefinition = "GEOMETRY")
  @Index(name = "offsetLocation")
  private Point offsetLocation;

  @AccessType("property")
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getBay() {
    return bay;
  }

  public void setBay(String bay) {
    this.bay = bay;
  }

  public StreetName getMainStreetName() {
    return mainStreetName;
  }

  public void setMainStreetName(StreetName mainStreetName) {
    this.mainStreetName = mainStreetName;
  }

  public StreetName getCrossStreetName() {
    return crossStreetName;
  }

  public void setCrossStreetName(StreetName crossStreetName) {
    this.crossStreetName = crossStreetName;
  }

  public double getDisplacement() {
    return displacement;
  }

  public void setDisplacement(double displacement) {
    this.displacement = displacement;
  }

  public Date getEffectiveBeginDate() {
    return effectiveBeginDate;
  }

  @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
  public void setEffectiveBeginDate(Date effectiveBeginDate) {
    this.effectiveBeginDate = effectiveBeginDate;
  }

  public Date getEffectiveEndDate() {
    return effectiveEndDate;
  }

  @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
  public void setEffectiveEndDate(Date effectiveEndDate) {
    this.effectiveEndDate = effectiveEndDate;
  }

  public int getFromCrossCurb() {
    return fromCrossCurb;
  }

  public void setFromCrossCurb(int fromCrossCurb) {
    this.fromCrossCurb = fromCrossCurb;
  }

  public int getFromIntersectionCenter() {
    return fromIntersectionCenter;
  }

  public void setFromIntersectionCenter(int fromIntersectionCenter) {
    this.fromIntersectionCenter = fromIntersectionCenter;
  }

  public String getGisJurisdictionCode() {
    return this.gisJurisdictionCode;
  }

  public void setGisJurisdictionCode(String gisJurisdictionCode) {
    this.gisJurisdictionCode = gisJurisdictionCode;
  }

  public int getGisZipCode() {
    return gisZipCode;
  }

  public void setGisZipCode(int gisZipCode) {
    this.gisZipCode = gisZipCode;
  }

  public double getMappedLinkLen() {
    return mappedLinkLen;
  }

  public void setMappedLinkLen(double mappedLinkLen) {
    this.mappedLinkLen = mappedLinkLen;
  }

  public double getMappedPercentFrom() {
    return mappedPercentFrom;
  }

  public void setMappedPercentFrom(double mappedPercentFrom) {
    this.mappedPercentFrom = mappedPercentFrom;
  }

  public double getMappedTransNodeFrom() {
    return mappedTransNodeFrom;
  }

  public void setMappedTransNodeFrom(double mappedTransNodeFrom) {
    this.mappedTransNodeFrom = mappedTransNodeFrom;
  }

  public String getRideFreeArea() {
    return rideFreeArea;
  }

  public void setRideFreeArea(String rideFreeArea) {
    this.rideFreeArea = rideFreeArea;
  }

  public int getSide() {
    return side;
  }

  public void setSide(int side) {
    this.side = side;
  }

  public String getSideCross() {
    return sideCross;
  }

  public void setSideCross(String sideCross) {
    this.sideCross = sideCross;
  }

  public String getSideOn() {
    return sideOn;
  }

  public void setSideOn(String sideOn) {
    this.sideOn = sideOn;
  }

  public String getStreetAddressComment() {
    return streetAddressComment;
  }

  public void setStreetAddressComment(String streetAddressComment) {
    this.streetAddressComment = streetAddressComment;
  }

  public TransLink getTransLink() {
    return transLink;
  }

  public void setTransLink(TransLink transLink) {
    this.transLink = transLink;
  }

  public Point getLocation() {
    return location;
  }

  public void setLocation(Point location) {
    this.location = location;
  }

  public Point getOffsetLocation() {
    return offsetLocation;
  }

  public void setOffsetLocation(Point offsetLocation) {
    this.offsetLocation = offsetLocation;
  }

  /***************************************************************************
   * {@link IHasLocation} Interface
   **************************************************************************/

  public String getDirection() {

    double xOffset = offsetLocation.getX();
    double yOffset = offsetLocation.getY();

    double x = location.getX();
    double y = location.getY();

    double theta = Math.atan2(yOffset - y, xOffset - x);

    double t = Math.PI / 4;
    int r = (int) Math.floor((theta + t / 2) / t);

    switch (r) {
      case 0:
        return "N";
      case 1:
        return "NW";
      case 2:
        return "W";
      case 3:
        return "SW";
      case 4:
        return "S";
      case -1:
        return "NE";
      case -2:
        return "E";
      case -3:
        return "SE";
      case -4:
        return "S";
      default:
        return "?";
    }
  }

  public Intersection getIntersection() {
    return new Intersection(getMainStreetName(), getCrossStreetName());
  }

  @Override
  public String toString() {
    return "Stop(id=" + getId() + ")";
  }
}
