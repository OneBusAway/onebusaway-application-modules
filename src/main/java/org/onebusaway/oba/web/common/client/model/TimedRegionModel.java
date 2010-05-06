package org.onebusaway.oba.web.common.client.model;

import org.onebusaway.common.web.common.client.model.AbstractModel;

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
  
  public void setData(List<LocationBounds> timeGrid, List<Integer> times) {

    _bounds = LatLngBounds.newInstance();
    _regions.clear();
    _maxTime = 0;

    for (int i = 0; i < timeGrid.size(); i++) {

      LocationBounds bounds = timeGrid.get(i);

      int time = times.get(i);
      _maxTime = Math.max(time,_maxTime);

      LatLng p1 = LatLng.newInstance(bounds.getLatMin(), bounds.getLonMin());
      LatLng p2 = LatLng.newInstance(bounds.getLatMax(), bounds.getLonMax());
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
