package org.onebusaway.webapp.gwt.where_library.impl;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;
import org.onebusaway.webapp.gwt.where_library.services.StopsForRegionService;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopsForRegionServiceImpl implements StopsForRegionService {

  private static final int MAX_STOP_COUNT_PER_REGION = 200;

  private double _latStep;

  private double _lonStep;

  private List<CoordinateBounds> _pendingOps = new ArrayList<CoordinateBounds>();

  private AsyncCallback<List<StopBean>> _callback;

  private Map<CoordinateBounds, RegionCache> _cache = new HashMap<CoordinateBounds, RegionCache>();

  private CoordinateBounds _bounds;

  public void setLatStep(double latStep) {
    _latStep = latStep;
  }

  public void setLonStep(double lonStep) {
    _lonStep = lonStep;
  }

  public void getStopsForRegion(CoordinateBounds bounds,
      AsyncCallback<List<StopBean>> callback) {

    _pendingOps.clear();
    _callback = callback;
    _bounds = bounds;

    double minLat = floor(bounds.getMinLat(), _latStep);
    double maxLat = floor(bounds.getMaxLat(), _latStep);
    double minLon = floor(bounds.getMinLon(), _lonStep);
    double maxLon = floor(bounds.getMaxLon(), _lonStep);

    for (double lat = minLat; lat <= maxLat; lat += _latStep) {
      for (double lon = minLon; lon <= maxLon; lon += _lonStep) {
        CoordinateBounds region = new CoordinateBounds(lat, lon,
            lat + _latStep, lon + _lonStep);
        checkRegion(region);
      }
    }

    checkPending();
  }

  private void checkRegion(CoordinateBounds region) {
    RegionCache cache = _cache.get(region);
    if (cache == null) {
      _pendingOps.add(region);
    } else {
      handleRegion(region, cache);
    }
  }

  private void handleRegion(CoordinateBounds region, RegionCache cache) {

    List<StopBean> stops = new ArrayList<StopBean>();

    for (StopBean stop : cache.getStops()) {
      if (_bounds.contains(stop.getLat(), stop.getLon()))
        stops.add(stop);
    }

    if (!stops.isEmpty())
      _callback.onSuccess(stops);

    if (cache.hasOverflow()) {
      for (CoordinateBounds subRegion : splitRegion(region))
        checkRegion(subRegion);
    }
  }

  private void checkPending() {

    if (_pendingOps.isEmpty())
      return;

    CoordinateBounds region = _pendingOps.remove(_pendingOps.size() - 1);

    WebappServiceAsync service = WebappServiceAsync.SERVICE;
    service.getStopsByBounds(region, MAX_STOP_COUNT_PER_REGION,
        new StopHandler(region));
  }

  private List<CoordinateBounds> splitRegion(CoordinateBounds region) {

    List<CoordinateBounds> splits = new ArrayList<CoordinateBounds>();

    double minLat = region.getMinLat();
    double maxLat = region.getMaxLat();
    double minLon = region.getMinLon();
    double maxLon = region.getMaxLon();
    double centerLat = (minLat + maxLat) / 2;
    double centerLon = (minLon + maxLon) / 2;

    splits.add(new CoordinateBounds(minLat, minLon, centerLat, centerLon));
    splits.add(new CoordinateBounds(minLat, centerLon, centerLat, maxLon));
    splits.add(new CoordinateBounds(centerLat, minLon, maxLat, centerLon));
    splits.add(new CoordinateBounds(centerLat, centerLon, maxLat, maxLon));

    return splits;
  }

  private double floor(double value, double step) {
    return Math.floor(value / step) * step;
  }

  private class StopHandler implements AsyncCallback<StopsBean> {

    private CoordinateBounds _region;

    public StopHandler(CoordinateBounds region) {
      _region = region;
    }

    public void onSuccess(StopsBean result) {
      RegionCache cache = new RegionCache(result.getStops(),
          result.isLimitExceeded());
      _cache.put(_region, cache);
      handleRegion(_region, cache);
      checkPending();
    }

    public void onFailure(Throwable caught) {
      checkPending();
    }
  }

  private static class RegionCache {

    private List<StopBean> _stops;

    private boolean _overflow = false;

    public RegionCache(List<StopBean> stops, boolean limitExceeded) {
      _stops = stops;
      _overflow = limitExceeded;
    }

    public List<StopBean> getStops() {
      return _stops;
    }

    public boolean hasOverflow() {
      return _overflow;
    }
  }
}
