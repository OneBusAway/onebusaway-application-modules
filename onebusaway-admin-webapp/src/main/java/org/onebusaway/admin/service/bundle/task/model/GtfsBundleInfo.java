package org.onebusaway.admin.service.bundle.task.model;

import java.util.Date;

/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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

public final class GtfsBundleInfo  {

  private static final long serialVersionUID = 1L;

  private String bundleId;

  private Integer id;

  private String name;
  
  private String directory;
  
  private Date startDate;
  
  private Date endDate;

  private Date timestamp;

  public GtfsBundleInfo() {

  }

  public String getBundleId() {
    return bundleId;
  }

  public void setBundleId(String raw) {
    this.bundleId = raw;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDirectory() {
    return directory;
  }
  
  public void setDirectory(String bundleDirectoryName) {
    this.directory = bundleDirectoryName;
  }

  public Date getStartDate() {
    return startDate;
  }
  
  public void setStartDate(Date bundleStartDate) {
    this.startDate = bundleStartDate;
  }
  
  public Date getEndDate() {
    return endDate;
  }
  
  public void setEndDate(Date bundleEndDate) {
     this.endDate = bundleEndDate;
  }

  public Date getTimestamp() {
    return timestamp;
  }
  
  public void setTimestamp(Date date) {
    this.timestamp = date;
  }
}
