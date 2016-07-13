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

import java.util.List;

/**
 * Holds the details for a transit mode in the Fixed Route Data Validation
 * report.  For each mode a number of routes is reported on, based on the input
 * data.  Each route has a summary of trips per stop count for that route.
 *
 * @author jpearson
 *
 */
public class DataValidationMode {
  private String modeName;
  List<DataValidationRouteCounts> routes;
  private String srcCode;  // Used in diff files to indicate the source.

  public String getModeName() {
    return modeName;
  }
  public void setModeName(String modeName) {
    this.modeName = modeName;
  }
  public List<DataValidationRouteCounts> getRoutes() {
    return routes;
  }
  public void setRoutes(List<DataValidationRouteCounts> routes) {
    this.routes = routes;
  }
  public String getSrcCode() {
    return srcCode;
  }
  public void setSrcCode(String srcCode) {
    this.srcCode = srcCode;
  }
}
