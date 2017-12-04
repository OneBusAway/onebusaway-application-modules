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
 * Holds the stop count summary for a particular mode and route.
 * The summary data is contained in the DataValidaionHeadsignCts
 * elements.
 *
 * @author jpearson
 *
 */
public class DataValidationRouteCounts implements Comparable<DataValidationRouteCounts> {
  private String routeNum;
  private String routeName;
  SortedSet<DataValidationHeadsignCts> headsignCounts;
  private String srcCode;  // Used in diff files to indicate the source.

  public DataValidationRouteCounts() {
    super();
  }
  public DataValidationRouteCounts(String routeName, String headsign, String direction) {
    super();
    String routeNum = "";
    //Check for routeNum as prefix to routeName
    int idx = routeName.substring(0,5).indexOf("-");
    if (idx > 0) {
      routeNum = routeName.substring(0,idx).trim();
      routeName = routeName.substring(idx+1);
    }
    this.routeNum = routeNum;
    this.routeName = routeName;
    this.headsignCounts = new TreeSet<DataValidationHeadsignCts>();
    this.headsignCounts.add(new DataValidationHeadsignCts(headsign, direction));
  }
  public DataValidationRouteCounts(String routeNum, String routeName, String headsign, String direction) {
    super();
    this.routeNum = routeNum;
    this.routeName = routeName;
    this.headsignCounts = new TreeSet<DataValidationHeadsignCts>();
    this.headsignCounts.add(new DataValidationHeadsignCts(headsign, direction));
  }

  public String getRouteNum() {
    return routeNum;
  }
  public void setRouteNum(String routeNum) {
    this.routeNum = routeNum;
  }
  public String getRouteName() {
    return routeName;
  }
  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }
  public SortedSet<DataValidationHeadsignCts> getHeadsignCounts() {
    return headsignCounts;
  }
  public void setHeadsignCounts(SortedSet<DataValidationHeadsignCts> headsignCounts) {
    this.headsignCounts = headsignCounts;
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
        + ((headsignCounts == null) ? 0 : headsignCounts.hashCode());
    result = prime * result + ((routeName == null) ? 0 : routeName.hashCode());
    result = prime * result + ((routeNum == null) ? 0 : routeNum.hashCode());
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
    if (!(obj instanceof DataValidationRouteCounts)) {
      return false;
    }
    DataValidationRouteCounts other = (DataValidationRouteCounts) obj;
    if (headsignCounts == null) {
      if (other.headsignCounts != null) {
        return false;
      }
    } else if (!headsignCounts.equals(other.headsignCounts)) {
      return false;
    }
    if (routeNum == null) {
      if (other.routeNum != null) {
        return false;
      }
    } else if (!routeNum.equals(other.routeNum)) {
      return false;
    }
    if (routeName == null) {
      if (other.routeName != null) {
        return false;
      }
    } else if (!routeName.equals(other.routeName)) {
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
  public int compareTo(DataValidationRouteCounts other) {
    if (routeNum == null) {
      if (other.routeNum != null) {
        return -1;
      }
    } else {
      if (other.routeNum == null) {
        return 1;
      } else {
        if (!routeNum.equals(other.routeNum)) {
          if (isNumeric(routeNum)) {
            if (isNumeric(other.routeNum)) {
              return Integer.parseInt(routeNum) - Integer.parseInt(other.routeNum);
            } else {
              return -1;    // Numeric vs. alpha.
            }
          } else {
            if (isNumeric(other.routeNum)) {
              return 1;
            } else {
              return routeNum.compareTo(other.routeNum);
            }
          }
        }
      }
    }

    if (routeName == null) {
      if (other.routeName != null) {
        return -1;
      }
    } else if (!routeName.equals(other.routeName)) {
      return routeName.compareTo(other.routeName);
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

    //Compare headsign sets
    if (!headsignCounts.equals(other.headsignCounts)) {
      if (headsignCounts.size() != other.headsignCounts.size()) {
        return headsignCounts.size() - other.headsignCounts.size();
      }
      Iterator<DataValidationHeadsignCts> it_1 = headsignCounts.iterator();
      Iterator<DataValidationHeadsignCts> it_2 = other.headsignCounts.iterator();
      while (it_1.hasNext()) {
        if (!it_1.next().equals(it_2.next())) {
          return it_1.next().compareTo(it_2.next());
        }
      }
    }

    return 0;
  }

  public static boolean isNumeric(String str) {
    if (str.isEmpty()) {
      return false;
    }
    for (char c : str.toCharArray()) {
      if (!Character.isDigit(c)) {
        return false;
      }
    }
    return true;
  }
}
