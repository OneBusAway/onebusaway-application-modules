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

public class ArchivedAgency implements Serializable {

  private static final long serialVersionUID = 2L;

  private String id;

  private String name;

  private String url;

  private String timezone;

  private String lang;

  private String phone;

  private String fareUrl;
  
  private String email;

  private Integer gtfsBundleInfoId;

  public ArchivedAgency() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getFareUrl() {
    return fareUrl;
  }

  public void setFareUrl(String fareUrl) {
    this.fareUrl = fareUrl;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Integer getGtfsBundleInfoId() {
    return gtfsBundleInfoId;
  }

  public void setGtfsBundleInfoId(Integer gtfsBundleInfoId) {
    this.gtfsBundleInfoId = gtfsBundleInfoId;
  }
}
