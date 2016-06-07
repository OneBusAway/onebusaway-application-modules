/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif;

import java.io.Serializable;
import java.util.Formatter;

public class TripIdentifier implements Serializable {
  private static final long serialVersionUID = 1L;
  
  public int startTime;
  public String routeName;
  public String startStop;
  public String run;
  public String block;
  public int endTime;

  public TripIdentifier(String routeName, int startTime, int endTime, String startStop, String run, String block) {
    this.routeName = routeName;
    this.startTime = startTime;
    this.startStop = startStop;
    this.endTime = endTime;
    this.run = run;
    this.block = block;
  }

  private String gtfsAndStifTimeFormat(int time) {
    StringBuilder sb = new StringBuilder();
    Formatter formatter = new Formatter(sb);
    int hours = time / 3600;
    int minutes = (time - hours * 3600) / 60;
    int seconds = time % 60;
    int centiminutes = (time * 100) / 60;
    formatter.format("%02d:%02d:%02d or %d", hours, minutes, seconds, centiminutes);
    return sb.toString();
  }

  @Override
  public String toString() {
    return "TripIdentifier(" + routeName + "," + gtfsAndStifTimeFormat(startTime) + "," + gtfsAndStifTimeFormat(endTime) + "," + startStop
        + "," + run + " from " + startStop + " block " + block + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((routeName == null) ? 0 : routeName.hashCode());
    result = prime * result + ((startStop == null) ? 0 : startStop.hashCode());
    result = prime * result + startTime;
    result = prime * result + endTime;
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
    else if (run != null && other.run != null && (!run.equals(other.run)))
      return false;
    else if (block != null && other.block != null && (!block.equals(other.block)))
      return false;
    if (startStop == null) {
      if (other.startStop != null)
        return false;
    } else if (!startStop.equals(other.startStop))
      return false;
    if (startTime != other.startTime || endTime != other.endTime)
      return false;
    return true;
  }
}