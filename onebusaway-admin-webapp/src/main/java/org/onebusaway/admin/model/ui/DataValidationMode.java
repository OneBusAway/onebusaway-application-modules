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

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Holds the details for a transit mode in the Fixed Route Data Validation
 * report.  For each mode a number of routes is reported on, based on the input
 * data.  Each route has a summary of trips per stop count for that route.
 *
 * @author jpearson
 *
 */
public class DataValidationMode implements Comparable<DataValidationMode> {
  private String modeName;
  SortedSet<DataValidationRouteCounts> routes;
  private String srcCode;  // Used in diff files to indicate the source.

  public DataValidationMode() {
    super();
  }
  public DataValidationMode(String modeName,
      String routeName, String headsign, String direction) {
    super();
    this.modeName = modeName;
    this.routes = new TreeSet<DataValidationRouteCounts>();
    String routeNum = "";
    int idx = routeName.substring(0,5).indexOf("-");
    if (idx > 0) {
      routeNum = routeName.substring(0,idx).trim();
      routeName = routeName.substring(idx+1);
    }
    routes.add(new DataValidationRouteCounts(routeNum, routeName, headsign, direction));
  }
  public DataValidationMode(String modeName, String routeNum,
      String routeName, String headsign, String direction) {
    super();
    this.modeName = modeName;
    this.routes = new TreeSet<DataValidationRouteCounts>();
    routes.add(new DataValidationRouteCounts(routeNum, routeName, headsign, direction));
  }

  public String getModeName() {
    return modeName;
  }
  public void setModeName(String modeName) {
    this.modeName = modeName;
  }
  public SortedSet<DataValidationRouteCounts> getRoutes() {
    return routes;
  }
  public void setRoutes(SortedSet<DataValidationRouteCounts> routes) {
    this.routes = routes;
  }
  public String getSrcCode() {
    return srcCode;
  }
  public void setSrcCode(String srcCode) {
    this.srcCode = srcCode;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((routes == null) ? 0 : routes.hashCode());
    result = prime * result + ((modeName == null) ? 0 : modeName.hashCode());
    result = prime * result + ((srcCode == null) ? 0 : srcCode.hashCode());
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
    if (!(obj instanceof DataValidationMode)) {
      return false;
    }
    DataValidationMode other = (DataValidationMode) obj;
    if (routes == null) {
      if (other.routes != null) {
        return false;
      }
    } else if (!routes.equals(other.routes)) {
      return false;
    }

    if (modeName == null) {
      if (other.modeName != null) {
        return false;
      }
    } else if (!modeName.equals(other.modeName)) {
      return false;
    }

    if (srcCode == null) {
      if (other.srcCode != null) {
        return false;
      }
    } else if (!srcCode.equals(other.srcCode)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(DataValidationMode obj) {
    DataValidationMode other = (DataValidationMode) obj;
    if (modeName == null) {
      if (other.modeName != null) {
        return -1;
      }
    } else if (other.modeName == null) {
      return 1;
    } else if (!modeName.equals(other.modeName)) {
      return modeName.compareTo(other.modeName);
    }

    //Compare routes sets
    if (!routes.equals(other.routes)) {
      if (routes.size() != other.routes.size()) {
        return routes.size() - other.routes.size();
      }
      Iterator<DataValidationRouteCounts> it_1 = routes.iterator();
      Iterator<DataValidationRouteCounts> it_2 = other.routes.iterator();
      while (it_1.hasNext()) {
        if (!it_1.next().equals(it_2.next())) {
          return it_1.next().compareTo(it_2.next());
        }
      }
    }

    if (srcCode == null) {
      if (other.srcCode != null) {
        return -1;
      } else {
        return 0;
      }
    } else if (other.srcCode == null) {
      return 1;
    } else if (!srcCode.equals(other.srcCode)) {
      return srcCode == "1" ? -1 : 1;
    }
    return 0;
  }
}
