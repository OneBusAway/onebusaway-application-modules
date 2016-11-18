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
 * Holds the stop count details for a particular mode, route, and headsign.
 * Summary trip count details are collected in the DataValidationDirectionCts
 * entries.
 *
 * @author jpearson
 *
 */
public class DataValidationHeadsignCts implements Comparable<DataValidationHeadsignCts> {
  private String headsign;
  SortedSet<DataValidationDirectionCts> dirCounts;
  private String srcCode;  // Used in diff files to indicate the source.

  public DataValidationHeadsignCts() {
    super();
    this.dirCounts = new TreeSet<DataValidationDirectionCts>();
  }
  public DataValidationHeadsignCts(String headsign, String direction) {
    super();
    this.headsign = headsign;
    this.dirCounts = new TreeSet<DataValidationDirectionCts>();
    this.dirCounts.add(new DataValidationDirectionCts(direction));
  }

  public String getHeadsign() {
    return headsign;
  }
  public void setHeadsign(String headsign) {
    this.headsign = headsign;
  }
  public SortedSet<DataValidationDirectionCts> getDirCounts() {
    return dirCounts;
  }
  public void setDirCounts(SortedSet<DataValidationDirectionCts> dirCounts) {
    this.dirCounts = dirCounts;
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
    result = prime * result + ((dirCounts == null) ? 0 : dirCounts.hashCode());
    result = prime * result + ((headsign == null) ? 0 : headsign.hashCode());
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
    if (!(obj instanceof DataValidationHeadsignCts)) {
      return false;
    }
    DataValidationHeadsignCts other = (DataValidationHeadsignCts) obj;
    if (dirCounts == null) {
      if (other.dirCounts != null) {
        return false;
      }
    } else if (!dirCounts.equals(other.dirCounts)) {
      return false;
    }

    if (headsign == null) {
      if (other.headsign != null) {
        return false;
      }
    } else if (!headsign.equals(other.headsign)) {
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
  public int compareTo(DataValidationHeadsignCts other) {
    if (headsign == null) {
      if (other.headsign != null) {
        return -1;
      }
    } else if (!headsign.equals(other.headsign)) {
      return headsign.compareTo(other.headsign);
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

    //Compare dirCounts sets
    if (!dirCounts.equals(other.dirCounts)) {
      if (dirCounts.size() != other.dirCounts.size()) {
        return dirCounts.size() - other.dirCounts.size();
      }
      Iterator<DataValidationDirectionCts> it_1 = dirCounts.iterator();
      Iterator<DataValidationDirectionCts> it_2 = other.dirCounts.iterator();
      while (it_1.hasNext()) {
        if (!it_1.next().equals(it_2.next())) {
          return it_1.next().compareTo(it_2.next());
        }
      }
    }

    return 0;
  }
}
