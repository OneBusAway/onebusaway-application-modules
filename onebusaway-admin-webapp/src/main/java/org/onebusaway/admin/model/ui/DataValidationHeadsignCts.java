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
package org.onebusaway.admin.model.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the stop count details for a particular mode, route, and headsign.
 * Summary trip count details are collected in the DataValidationDirectionCts
 * entries.
 *
 * @author jpearson
 *
 */
public class DataValidationHeadsignCts {
  private String headsign;
  List<DataValidationDirectionCts> dirCounts;
  private String srcCode;  // Used in diff files to indicate the source.

  public DataValidationHeadsignCts() {
    super();
    this.dirCounts = new ArrayList<DataValidationDirectionCts>();
  }
  public DataValidationHeadsignCts(String headsign, String direction) {
    super();
    this.headsign = headsign;
    this.dirCounts = new ArrayList<DataValidationDirectionCts>();
    this.dirCounts.add(new DataValidationDirectionCts(direction));
  }

  public String getHeadsign() {
    return headsign;
  }
  public void setHeadsign(String headsign) {
    this.headsign = headsign;
  }
  public List<DataValidationDirectionCts> getDirCounts() {
    return dirCounts;
  }
  public void setDirCounts(List<DataValidationDirectionCts> dirCounts) {
    this.dirCounts = dirCounts;
  }
  public String getSrcCode() {
    return srcCode;
  }
  public void setSrcCode(String srcCode) {
    this.srcCode = srcCode;
  }
}
