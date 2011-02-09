package org.onebusaway.webapp.gwt.tripplanner_library.model;

import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripSegmentBean;
import org.onebusaway.webapp.gwt.common.model.AbstractModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TripPlanModel extends AbstractModel<TripPlanModel> {

  private static TripBeanComparator _comparator = new TripBeanComparator();

  private List<TripPlanBean> _trips;

  private int _selectedIndex = 0;

  public void setTripPlans(List<TripPlanBean> trips) {
    _trips = trips;
    Collections.sort(_trips, _comparator);
    _selectedIndex = _trips.isEmpty() ? -1 : 0;
    fireModelChange(this);
  }

  public List<TripPlanBean> getTrips() {
    return _trips;
  }

  public int getSelectedIndex() {
    return _selectedIndex;
  }

  public void setSelectedIndex(int selectedIndex) {
    _selectedIndex = selectedIndex;
    fireModelChange(this);
  }

  private static class TripBeanComparator implements Comparator<TripPlanBean> {

    public int compare(TripPlanBean o1, TripPlanBean o2) {

      List<TripSegmentBean> segments1 = o1.getSegments();
      List<TripSegmentBean> segments2 = o2.getSegments();

      if (segments1.isEmpty() && segments2.isEmpty())
        return 0;
      else if (segments1.isEmpty())
        return -1;
      else if (segments2.isEmpty())
        return 1;

      TripSegmentBean segment1 = segments1.get(segments1.size() - 1);
      TripSegmentBean segment2 = segments2.get(segments2.size() - 1);

      long d1 = segment1.getTime();
      long d2 = segment2.getTime();

      return d1 == d2 ? 0 : (d1 < d2 ? -1 : 1);
    }
  }
}
