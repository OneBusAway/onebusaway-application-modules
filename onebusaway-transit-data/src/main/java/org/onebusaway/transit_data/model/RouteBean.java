/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data.model;

import java.io.Serializable;

public final class RouteBean implements Serializable {

  private static final long serialVersionUID = 2L;

  private String id;

  private String shortName;

  private String longName;

  private String description;

  private int type;

  private String url;

  private String color;

  private String textColor;

  private AgencyBean agency;

  public static Builder builder() {
    return new Builder();
  }
  
  public static Builder builder(RouteBean bean) {
    return new Builder(bean);
  }
  
  private RouteBean() {
    this(builder());
  }

  private RouteBean(Builder builder) {
    this.agency = builder.agency;
    this.color = builder.color;
    this.description = builder.description;
    this.id = builder.id;
    this.longName = builder.longName;
    this.shortName = builder.shortName;
    this.textColor = builder.textColor;
    this.type = builder.type;
    this.url = builder.url;
  }

  public String getId() {
    return id;
  }

  public String getShortName() {
    return shortName;
  }

  public String getLongName() {
    return longName;
  }

  public String getDescription() {
    return description;
  }

  public int getType() {
    return type;
  }

  public String getUrl() {
    return url;
  }

  public String getColor() {
    return color;
  }

  public String getTextColor() {
    return textColor;
  }

  public AgencyBean getAgency() {
    return agency;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RouteBean))
      return false;
    RouteBean other = (RouteBean) obj;
    return this.id.equals(other.id);
  }

  public static class Builder {

    private String id;

    private String shortName;

    private String longName;

    private String description;

    private int type;

    private String url;

    private String color;

    private String textColor;

    private AgencyBean agency;

    public Builder() {
      
    }
    
    public Builder(RouteBean bean) {
      this.agency = bean.agency;
      this.color = bean.color;
      this.description = bean.description;
      this.id = bean.id;
      this.longName = bean.longName;
      this.shortName = bean.shortName;
      this.textColor = bean.textColor;
      this.type = bean.type;
      this.url = bean.url;
    }

    public RouteBean create() {
      return new RouteBean(this);
    }

    public void setId(String id) {
      this.id = id;
    }

    public void setShortName(String shortName) {
      this.shortName = shortName;
    }

    public void setLongName(String longName) {
      this.longName = longName;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public void setType(int type) {
      this.type = type;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public void setColor(String color) {
      this.color = color;
    }

    public void setTextColor(String textColor) {
      this.textColor = textColor;
    }

    public void setAgency(AgencyBean agency) {
      this.agency = agency;
    }
  }
}
