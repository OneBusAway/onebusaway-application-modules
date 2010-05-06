package org.onebusaway.webapp.gwt.tripplanner_library.view;

import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripSegmentBean;

import java.util.Date;
import java.util.List;

public class TripBeanSupport {

  public static Date getStartTime(TripPlanBean trip) {
    TripSegmentBean segment = trip.getSegments().get(0);
    return new Date(segment.getTime());
  }

  public static Date getEndTime(TripPlanBean trip) {
    List<TripSegmentBean> segments = trip.getSegments();
    TripSegmentBean segment = segments.get(segments.size() - 1);
    return new Date(segment.getTime());
  }

  public static String getDurationLabel(TripPlanBean bean) {
    Date from = getStartTime(bean);
    Date to = getEndTime(bean);
    return getDurationLabel(to.getTime() - from.getTime());
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
