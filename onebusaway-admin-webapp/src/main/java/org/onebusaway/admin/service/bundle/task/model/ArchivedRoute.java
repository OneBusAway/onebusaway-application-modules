/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.admin.service.bundle.task.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;

//@Entity
//@Table(name="gtfs_routes")
//@org.hibernate.annotations.Entity(mutable = false)
public class ArchivedRoute implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final int MISSING_VALUE = -999;

  //private AgencyAndId id;
  private String agencyId;
  
  private String id;

  private String agency;

  private String shortName;

  private String longName;

  private int type;

  private String desc;

  private String url;

  private String color;

  private String textColor;
  
  @Deprecated
  private int routeBikesAllowed = 0;

  /**
   * 0 = unknown / unspecified, 1 = bikes allowed, 2 = bikes NOT allowed
   */
  private int bikesAllowed = 0;

  private int sortOrder = MISSING_VALUE;

  private Integer gtfsBundleInfoId;

  public ArchivedRoute() {

  }

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAgency() {
    return agency;
  }

  public void setAgency(String agency) {
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

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
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

  public int getRouteBikesAllowed() {
    return routeBikesAllowed;
  }

  public void setRouteBikesAllowed(int routeBikesAllowed) {
    this.routeBikesAllowed = routeBikesAllowed;
  }

  public int getBikesAllowed() {
    return bikesAllowed;
  }

  public void setBikesAllowed(int bikesAllowed) {
    this.bikesAllowed = bikesAllowed;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

  public Integer getGtfsBundleInfoId() {
    return gtfsBundleInfoId;
  }

  public void setGtfsBundleInfoId(Integer gtfsBundleInfoId) {
    this.gtfsBundleInfoId = gtfsBundleInfoId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + agencyId.hashCode();
    result = prime * result + id.hashCode();
    result = prime * result + agency.hashCode();
    result = prime * result + shortName.hashCode();
    result = prime * result + longName.hashCode();
    result = prime * result + type;
    result = prime * result + desc.hashCode();
    result = prime * result + url.hashCode();
    result = prime * result + color.hashCode();
    result = prime * result + textColor.hashCode();
    result = prime * result + gtfsBundleInfoId;                
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ArchivedRoute)) {
      return false;
    }
    ArchivedRoute other = (ArchivedRoute) obj;
    if (gtfsBundleInfoId != other.gtfsBundleInfoId) {
      return false;
    } else if (!agencyId.equals(other.agencyId)) {
      return false;
    } else if (!id.equals(other.id)) {
      return false;
    } else if (!agency.equals(other.agency)) {
      return false;
    } else if (!shortName.equals(other.shortName)) {
      return false;
    } else if (!longName.equals(other.longName)) {
      return false;
    } else if (type != other.type) {
      return false;
    } else if (!desc.equals(other.desc)) {
      return false;
    } else if (!url.equals(other.url)) {
      return false;
    } else if (!color.equals(other.color)) {
      return false;
    } else if (!textColor.equals(other.textColor)) {
      return false;
    }
    
    return true;
  }
}
