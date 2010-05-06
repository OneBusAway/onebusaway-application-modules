package org.onebusaway.oba.impl;

import edu.washington.cs.rse.collections.tuple.T2;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.oba.web.common.client.model.LocationBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimedGridFactory extends GridFactory {

  private double _velocity;
  private Point _source;
  private long _time;
  private long _timeRemaining;

  private Map<IntPoint, Long> _cellTimes = new HashMap<IntPoint, Long>();

  public TimedGridFactory(ProjectionService projection, double gridSize, double velocity) {
    super(projection, gridSize);
    _velocity = velocity;
  }

  public void addPoint(Point point, double radius, long time, long timeRemaining) {
    _source = point;
    _time = time;
    _timeRemaining = timeRemaining;
    addPoint(point, radius);
    _source = null;
    _time = 0;
    _timeRemaining = 0;
  }

  public List<T2<LocationBounds, Long>> getGridAndTimes() {
    List<T2<LocationBounds, Long>> results = new ArrayList<T2<LocationBounds, Long>>();
    for (Map.Entry<IntPoint, Long> entry : _cellTimes.entrySet()) {
      IntPoint cell = entry.getKey();
      Long time = entry.getValue();
      LocationBounds bounds = getCellAsLocationBounds(cell);
      results.add(T2.create(bounds, time));
    }
    return results;
  }

  @Override
  protected void addCell(IntPoint index) {

    if (_source == null)
      throw new IllegalStateException();

    DoublePoint min = new DoublePoint();
    DoublePoint max = new DoublePoint();
    getCellAsPoints(index, min, max);

    // This is a subjective sort of thing
    // TODO

    double dx = _source.getX() - (min.x + max.x) / 2;
    double dy = _source.getY() - (min.y + max.y) / 2;
    double d = Math.sqrt(dx * dx + dy * dy);
    double t = d == 0 ? 0 : d / _velocity;

    if (t > _timeRemaining)
      return;

    super.addCell(index);

    long time = (long) (t + _time);
    Long currentTime = _cellTimes.get(index);
    if (currentTime == null || currentTime > time)
      _cellTimes.put(index, time);
  }

}
