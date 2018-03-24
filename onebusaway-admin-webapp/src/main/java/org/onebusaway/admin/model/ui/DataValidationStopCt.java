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

import java.util.Arrays;

/**
 * Holds the number of trips for a specific stop count for a given route and 
 * mode in the Fixed Route Data Validation report.  The trip numbers will be
 * the number of weekday trips, Saturday trips, and Sunday trips making this 
 * number of stops for the particular route and mode.
 *
 * @author jpearson
 *
 */
public class DataValidationStopCt implements Comparable {

  private int stopCt;
  private int[] tripCts;
  private String srcCode;  // Used in diff files to indicate the source.

  public int getStopCt() {
    return stopCt;
  }
  public void setStopCt(int stopCt) {
    this.stopCt = stopCt;
  }
  public int[] getTripCts() {
    return tripCts;
  }
  public void setTripCts(int[] tripCts) {
    this.tripCts = tripCts;
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
    result = prime * result + ((srcCode == null) ? 0 : srcCode.hashCode());
    result = prime * result + stopCt;
    result = prime * result + Arrays.hashCode(tripCts);
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
    if (!(obj instanceof DataValidationStopCt)) {
      return false;
    }
    DataValidationStopCt other = (DataValidationStopCt) obj;
    if (srcCode == null) {
      if (other.srcCode != null) {
        return false;
      }
    } else if (!srcCode.equals(other.srcCode)) {
      return false;
    }
    if (stopCt != other.stopCt) {
      return false;
    }
    if (!Arrays.equals(tripCts, other.tripCts)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(Object obj) {
    DataValidationStopCt other = (DataValidationStopCt)obj;
    if (stopCt != other.stopCt) {
      return stopCt - other.stopCt;
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
    for (int i=0; i<3; i++) {
      if (tripCts[i] != other.tripCts[i]) {
        return tripCts[i] - other.tripCts[i];
      }
    }
    return 0;
  }
}
