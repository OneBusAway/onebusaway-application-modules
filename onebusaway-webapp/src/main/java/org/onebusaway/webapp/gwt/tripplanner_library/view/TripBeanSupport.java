/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.webapp.gwt.tripplanner_library.view;

import org.onebusaway.transit_data.model.tripplanning.ItineraryBean;

public class TripBeanSupport {

  public static String getDurationLabel(ItineraryBean bean) {
    return getDurationLabel(bean.getEndTime() - bean.getStartTime());
  }

  public static String getDurationLabel(long duration) {

    int minutes = (int) (duration / (1000 * 60));
    int hours = minutes / 60;
    minutes = minutes % 60;

    if (hours == 0 && minutes == 0)
      return "0 mins";

    String label = "";
    if (hours > 0)
      label = Integer.toString(hours) + (hours == 1 ? " hour" : " hours");

    if (minutes == 0 && hours > 0)
      return label;

    if (label.length() > 0)
      label += " ";

    label += Integer.toString(minutes) + (minutes == 1 ? " min" : " mins");

    return label;
  }

}
