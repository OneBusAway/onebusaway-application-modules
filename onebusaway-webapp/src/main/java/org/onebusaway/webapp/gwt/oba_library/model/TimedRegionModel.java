package org.onebusaway.webapp.gwt.oba_library.model;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.webapp.gwt.common.model.AbstractModel;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class TimedRegionModel extends AbstractModel<TimedRegionModel> {

  private LatLngBounds _bounds;

  private List<TimedRegion> _regions = new ArrayList<TimedRegion>();

  private int _maxTime;

  public LatLngBounds getBounds() {
    return _bounds;
  }

  public List<TimedRegion> getRegions() {
    return _regions;
  }

  public int getMaxTime() {
    return _maxTime;
  }

  public void setData(List<CoordinateBounds> timeGrid, List<Integer> times) {

    _bounds = LatLngBounds.newInstance();
    _regions.clear();
    _maxTime = 0;

    for (int i = 0; i < timeGrid.size(); i++) {

      CoordinateBounds bounds = timeGrid.get(i);

      int time = times.get(i);
      _maxTime = Math.max(time, _maxTime);

      LatLng p1 = LatLng.newInstance(bounds.getMinLat(), bounds.getMinLon());
      LatLng p2 = LatLng.newInstance(bounds.getMaxLon(), bounds.getMaxLon());
      LatLngBounds b = LatLngBounds.newInstance();
      b.extend(p1);
      b.extend(p2);

      TimedRegion region = new TimedRegion(b, time);
      _regions.add(region);

      _bounds.extend(p1);
      _bounds.extend(p2);
    }

    refresh();
  }

  private void refresh() {
    fireModelChange(this);
  }
}
