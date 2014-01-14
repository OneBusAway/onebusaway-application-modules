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
package org.onebusaway.geospatial.grid;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.EncodedPolygonBean;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.utility.filter.IFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimedGridFactory {

  private GridFactoryImpl _grid;
  private double _velocity;
  private double _sourceLat;
  private double _sourceLon;
  private long _time;
  private long _timeRemaining;

  public TimedGridFactory(double gridLatStep, double gridLonStep, double velocity) {
    _grid = new GridFactoryImpl(gridLatStep,gridLonStep);
    _velocity = velocity;
  }

  public void addPoint(double lat, double lon, long time, long timeRemaining) {
    _sourceLat = lat;
    _sourceLon = lon;
    _time = time;
    _timeRemaining = timeRemaining;

    double distanceRemaining = timeRemaining * _velocity;
    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(lat, lon,
        distanceRemaining);
    _grid.addBounds(bounds);

    _sourceLat = Double.NaN;
    _sourceLon = Double.NaN;
    _time = 0;
    _timeRemaining = 0;
  }

  public Map<Integer, List<EncodedPolygonBean>> getPolygonsByTime(
      int segmentSizeInMinutes) {
    return _grid.getPolygonsByTime(segmentSizeInMinutes);
  }

  private class GridFactoryImpl extends GridFactory {

    public GridFactoryImpl(double gridLatStep, double gridLonStep) {
      super(gridLatStep,gridLonStep);
    }

    public Map<Integer, List<EncodedPolygonBean>> getPolygonsByTime(
        int segmentSizeInMinutes) {

      Map<Integer, List<Grid.Entry<Object>>> indicesByTime = new HashMap<Integer, List<Grid.Entry<Object>>>();

      for (Grid.Entry<Object> entry : _grid.getEntries()) {
        Long value = (Long) entry.getValue();
        int t = (int) (value / (1000 * 60 * segmentSizeInMinutes));
        get(indicesByTime,t).add(entry);
      }

      Map<Integer, List<EncodedPolygonBean>> results = new HashMap<Integer, List<EncodedPolygonBean>>();

      for (Map.Entry<Integer, List<Grid.Entry<Object>>> entry : indicesByTime.entrySet()) {
        BoundaryFactory factory = new BoundaryFactory();
        factory.setPruneAllButCorners(true);
        int offset = entry.getKey();
        long tFrom = offset * segmentSizeInMinutes * 60 * 1000;
        long tTo = (offset + 1) * segmentSizeInMinutes * 60 * 1000;
        TimeRangeFilter filter = new TimeRangeFilter(tFrom, tTo);
        FilteredGrid<Object> filtered = new FilteredGrid<Object>(_grid, filter);
        List<Boundary> boundaries = factory.getBoundaries(filtered,
            entry.getValue());
        List<EncodedPolygonBean> beans = getBoundariesAsBeans(boundaries);
        results.put(offset, beans);
      }

      return results;
    }

    @Override
    protected void addCell(GridIndex index, Object value) {

      if (Double.isNaN(_sourceLat) || Double.isNaN(_sourceLon))
        throw new IllegalStateException();

      CoordinateBounds bounds = getIndexAsBounds(index);

      double lat2 = (bounds.getMinLat() + bounds.getMaxLat()) / 2;
      double lon2 = (bounds.getMinLon() + bounds.getMaxLon()) / 2;

      double d = SphericalGeometryLibrary.distance(_sourceLat, _sourceLon,
          lat2, lon2);
      double t = d == 0 ? 0 : d / _velocity;

      if (t > _timeRemaining)
        return;

      long time = (long) (t + _time);
      Long currentTime = (Long) _grid.get(index);
      if (currentTime == null || currentTime > time)
        super.addCell(index, new Long(time));
    }

    private List<Grid.Entry<Object>> get(
        Map<Integer, List<Grid.Entry<Object>>> map, Integer key) {
      List<Grid.Entry<Object>> list = map.get(key);
      if (list == null) {
        list = new ArrayList<Grid.Entry<Object>>();
        map.put(key, list);
      }
      return list;
    }
  }

  private static class TimeRangeFilter implements IFilter<Object> {

    private long _from;
    private long _to;

    public TimeRangeFilter(long from, long to) {
      _from = from;
      _to = to;
    }

    public boolean isEnabled(Object object) {
      Long time = (Long) object;
      return _from <= time && time < _to;
    }
  }
}
