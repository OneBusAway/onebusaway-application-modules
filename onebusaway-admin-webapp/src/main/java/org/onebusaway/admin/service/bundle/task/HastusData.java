/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.task;

public class HastusData {

  private String agencyId;
  private String gisDataDirectory;
  private String scheduleDataDirectory;
  
  public String getAgencyId() {
    return agencyId;
  }
  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }
  public String getGisDataDirectory() {
    return gisDataDirectory;
  }
  public void setGisDataDirectory(String gisDataDirectory) {
    this.gisDataDirectory = gisDataDirectory;
  }
  public String getScheduleDataDirectory() {
    return scheduleDataDirectory;
  }
  public void setScheduleDataDirectory(String scheduleDataDirectory) {
    this.scheduleDataDirectory = scheduleDataDirectory;
  }
  
  public String toString() {
    return "HastusData{[" + agencyId + "] gis=" + gisDataDirectory 
      + ", schedule=" + scheduleDataDirectory;
  }
  public boolean isValid() {
    return (gisDataDirectory != null && scheduleDataDirectory != null);
  }
}
