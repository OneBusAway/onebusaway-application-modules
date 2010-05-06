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

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "transit_street_names")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@AccessType("field")
public class StreetName extends IdentityBean {

  private static final long serialVersionUID = 1L;

  /**
   * We make the id a "property" access element such that a call to
   * {@link #getId()} when the object is proxied does not result in proxy
   * insantiation
   */
  @Id
  @AccessType("property")
  private int id;

  @Column(length = 1)
  private String status;

  private Date dbModDate;

  @Column(length = 2)
  private String prefix;

  @Column(length = 30)
  private String name;

  @Column(length = 8)
  private String type;

  @Column(length = 2)
  private String suffix;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Date getDbModDate() {
    return dbModDate;
  }

  @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
  public void setDbModDate(Date dbModDate) {
    this.dbModDate = dbModDate;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public String getCombinedName() {
    String combined = this.name;
    if (type != null && type.length() > 0)
      combined = combined + " " + type;
    if (prefix != null && prefix.length() > 0)
      combined = prefix + " " + combined;
    if (suffix != null && suffix.length() > 0)
      combined = combined + " " + suffix;
    return combined;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StreetName))
      return false;
    StreetName sn = (StreetName) obj;
    return id == sn.getId();
  }

  @Override
  public int hashCode() {
    return id;
  }
}
