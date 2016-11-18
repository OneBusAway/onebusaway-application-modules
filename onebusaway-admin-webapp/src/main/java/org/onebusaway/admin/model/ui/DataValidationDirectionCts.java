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
 * Holds the stop count details for a particular mode, route, headsign, and
 * direction. All the trips meeting the above criteria are checked for the
 * number of stops they make. This data is summarized by totaling the number
 * of trips with a particular number of stops.
 *
 * @author jpearson
 *
 */
public class DataValidationDirectionCts implements Comparable<DataValidationDirectionCts> {
  private String direction;
  SortedSet<DataValidationStopCt> stopCounts;
  private String srcCode;  // Used in diff files to indicate the source.

  public DataValidationDirectionCts() {
    super();
    this.stopCounts = new TreeSet<DataValidationStopCt>();
  }
  public DataValidationDirectionCts(String direction) {
    super();
    this.direction = direction;
    this.stopCounts = new TreeSet<DataValidationStopCt>();
  }

  public String getDirection() {
    return direction;
  }
  public void setDirection(String direction) {
    this.direction = direction;
  }
  public SortedSet<DataValidationStopCt> getStopCounts() {
    return stopCounts;
  }
  public void setStopCounts(SortedSet<DataValidationStopCt> stopCounts) {
    this.stopCounts = stopCounts;
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
    result = prime * result + ((direction == null) ? 0 : direction.hashCode());
    result = prime * result + ((srcCode == null) ? 0 : srcCode.hashCode());
    result = prime * result
        + ((stopCounts == null) ? 0 : stopCounts.hashCode());
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
    if (!(obj instanceof DataValidationDirectionCts)) {
      return false;
    }
    DataValidationDirectionCts other = (DataValidationDirectionCts) obj;
    if (direction == null) {
      if (other.direction != null) {
        return false;
      }
    } else if (!direction.equals(other.direction)) {
      return false;
    }
    if (srcCode == null) {
      if (other.srcCode != null) {
        return false;
      }
    } else if (!srcCode.equals(other.srcCode)) {
      return false;
    }

    if (stopCounts == null) {
      if (other.stopCounts != null) {
        return false;
      }
    } else if (!stopCounts.equals(other.stopCounts)) {
      return false;
    }

    return true;
  }

  @Override
  public int compareTo(DataValidationDirectionCts other) {
    if (this == other) {
      return 0;
    }
    if (direction == null) {
      if (other.direction != null) {
        return -1;
      }
    } else if (other.direction == null) {
      return 1;
    }
    if (!direction.equals(other.direction)) {
      return direction.compareTo(other.direction);
    }

    if (srcCode == null) {
      if (other.srcCode != null) {
        return -1;
      }
    } else if (other.srcCode == null) {
      return 1;
    }
    if (!srcCode.equals(other.srcCode)) {
      return srcCode == "1" ? -1 : 1;
    }

    if (stopCounts == null) {
      if (other.stopCounts != null) {
        return -1;
      }
    } else if (other.stopCounts == null) {
      return 1;
    }
    //Compare stopCounts lists
    if (!stopCounts.equals(other.stopCounts)) {
      if (stopCounts.size() != other.stopCounts.size()) {
        return stopCounts.size() - other.stopCounts.size();
      }
      Iterator<DataValidationStopCt> it_1 = stopCounts.iterator();
      Iterator<DataValidationStopCt> it_2 = other.stopCounts.iterator();
      while (it_1.hasNext()) {
        if (!it_1.next().equals(it_2.next())) {
          return it_1.next().compareTo(it_2.next());
        }
      }
    }

    return 0;
  }
}
