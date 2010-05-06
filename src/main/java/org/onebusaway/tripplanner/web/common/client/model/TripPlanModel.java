package org.onebusaway.tripplanner.web.common.client.model;

import org.onebusaway.common.web.common.client.model.AbstractModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class TripPlanModel extends AbstractModel<TripPlanModel> {

  private static TripBeanComparator _comparator = new TripBeanComparator();

  private List<TripBean> _trips;

  public void setTripPlans(List<TripBean> trips) {
    _trips = trips;
    Collections.sort(_trips, _comparator);
    fireModelChange(this);
  }

  public List<TripBean> getTrips() {
    return _trips;
  }

  private static class TripBeanComparator implements Comparator<TripBean> {

    public int compare(TripBean o1, TripBean o2) {
      
      List<TripSegmentBean> segments1 = o1.getSegments();
      List<TripSegmentBean> segments2 = o2.getSegments();

      if (segments1.isEmpty() && segments2.isEmpty())
        return 0;
      else if (segments1.isEmpty())
        return -1;
      else if (segments2.isEmpty())
        return 1;

      TripSegmentBean segment1 = segments1.get(segments1.size()-1);
      TripSegmentBean segment2 = segments2.get(segments2.size()-1);

      Date d1 = segment1.getTime();
      Date d2 = segment2.getTime();

      return d1.compareTo(d2);
    }
  }
}
