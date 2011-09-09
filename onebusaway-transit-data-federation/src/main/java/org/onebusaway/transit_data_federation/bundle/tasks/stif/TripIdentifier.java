/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.transit_data_federation.bundle.tasks.stif;

public class TripIdentifier {
  public int startTime;
  public String routeName;
  public String startStop;

  public TripIdentifier(String routeName, int startTime, String startStop) {
    this.routeName = routeName;
    this.startTime = startTime;
    this.startStop = startStop;
  }

  @Override
  public String toString() {
    return "TripIdentifier(" + routeName + "," + startTime + "," + startStop
        + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((routeName == null) ? 0 : routeName.hashCode());
    result = prime * result + ((startStop == null) ? 0 : startStop.hashCode());
    result = prime * result + startTime;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TripIdentifier other = (TripIdentifier) obj;
    if (routeName == null) {
      if (other.routeName != null)
        return false;
    } else if (!routeName.equals(other.routeName))
      return false;
    if (startStop == null) {
      if (other.startStop != null)
        return false;
    } else if (!startStop.equals(other.startStop))
      return false;
    if (startTime != other.startTime)
      return false;
    return true;
  }
}