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
