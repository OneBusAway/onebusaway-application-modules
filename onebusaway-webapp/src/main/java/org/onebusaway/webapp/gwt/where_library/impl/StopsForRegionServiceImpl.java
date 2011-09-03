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
package org.onebusaway.webapp.gwt.where_library.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;
import org.onebusaway.webapp.gwt.where_library.services.StopsForRegionService;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class StopsForRegionServiceImpl implements StopsForRegionService {

  private static final int MAX_STOP_COUNT_PER_REGION = 200;

  private LinkedList<RegionOp> _pendingOps = new LinkedList<RegionOp>();

  private AsyncCallback<List<StopBean>> _callback;

  private Map<CoordinateBounds, RegionCache> _cache = new HashMap<CoordinateBounds, RegionCache>();

  private Set<CoordinateBounds> _sentToClient = new HashSet<CoordinateBounds>();

  private double _latStep = Double.NaN;

  private double _lonStep = Double.NaN;

  private WebappServiceAsync _webappService = WebappServiceAsync.SERVICE;

  public void setLatStep(double latStep) {
    _latStep = latStep;
  }

  public void setLonStep(double lonStep) {
    _lonStep = lonStep;
  }

  public void setWebappService(WebappServiceAsync webappService) {
    _webappService = webappService;
  }

  public WebappServiceAsync getWebappService() {
    return _webappService;
  }

  public void getStopsForRegion(final CoordinateBounds bounds,
      final AsyncCallback<List<StopBean>> callback) {

    _pendingOps.clear();
    _sentToClient.clear();
    _callback = callback;

    checkSteps(bounds);
    /**
     * We attempt to load the visible region directly at first
     */
    exploreVisibleRegion(bounds);

    /**
     * We follow up by pre-fetching the buffered region
     */
    exploreBufferedRegion(bounds);

    /**
     * Events queued up, we fire off any pending events
     */
    checkPending();
  }

  /*****
   * Private Methods
   ****/

  private void checkSteps(CoordinateBounds bounds) {
    if (!(Double.isNaN(_latStep) || Double.isNaN(_lonStep)))
      return;

    CoordinateBounds steps = SphericalGeometryLibrary.bounds(
        bounds.getMinLat(), bounds.getMinLon(), 200);

    _latStep = snap(steps.getMaxLat() - steps.getMinLat(), 1e3);
    _lonStep = snap(steps.getMaxLon() - steps.getMinLon(), 1e3);
  }

  private void exploreVisibleRegion(CoordinateBounds bounds) {

    double minLat = floor(bounds.getMinLat(), _latStep);
    double maxLat = floor(bounds.getMaxLat(), _latStep);
    double minLon = floor(bounds.getMinLon(), _lonStep);
    double maxLon = floor(bounds.getMaxLon(), _lonStep);

    CoordinateBounds dirtyRegion = new CoordinateBounds();
    List<CoordinateBounds> dirtyRegions = new ArrayList<CoordinateBounds>();

    for (double lat = minLat; lat <= maxLat; lat += _latStep) {
      for (double lon = minLon; lon <= maxLon; lon += _lonStep) {
        CoordinateBounds region = snapBounds(lat, lon, lat + _latStep, lon
            + _lonStep);
        RegionCache cache = _cache.get(region);
        if (cache == null) {
          dirtyRegion.addBounds(region);
          dirtyRegions.add(region);
        } else {
          handleRegion(region, cache);
        }
      }
    }

    if (!dirtyRegions.isEmpty()) {
      MultiRegionOp op = new MultiRegionOp(dirtyRegion, dirtyRegions);
      addOp(op);

    }
  }

  private void exploreBufferedRegion(CoordinateBounds bounds) {

    double minLat = floor(bounds.getMinLat() - _latStep, _latStep);
    double maxLat = floor(bounds.getMaxLat() + _latStep, _latStep);
    double minLon = floor(bounds.getMinLon() - _lonStep, _lonStep);
    double maxLon = floor(bounds.getMaxLon() + _lonStep, _lonStep);

    for (double lat = minLat; lat <= maxLat; lat += _latStep) {
      for (double lon = minLon; lon <= maxLon; lon += _lonStep) {
        CoordinateBounds region = snapBounds(lat, lon, lat + _latStep, lon
            + _lonStep);
        checkRegion(region);
      }
    }
  }

  private void checkRegion(CoordinateBounds region) {
    RegionCache cache = _cache.get(region);
    if (cache == null) {
      addOp(new RegionOp(region));
    } else {
      handleRegion(region, cache);
    }
  }

  private void addOp(RegionOp op) {
    _pendingOps.addLast(op);
  }

  private void handleRegion(CoordinateBounds region, RegionCache cache) {

    // Only send stops to client if we haven't already
    if (_sentToClient.contains(region))
      return;

    // There were too many stops in the region, so jump to sub-regions
    if (cache.hasOverflow()) {
      for (CoordinateBounds subRegion : splitRegion(region))
        checkRegion(subRegion);
      return;
    }

    List<StopBean> stopsInView = new ArrayList<StopBean>();

    for (StopBean stop : cache.getStops()) {
      // if (_bounds.contains(stop.getLat(), stop.getLon()))
      stopsInView.add(stop);
    }

    _sentToClient.add(region);

    // And only if we have stops to show
    if (!stopsInView.isEmpty())
      _callback.onSuccess(stopsInView);
  }

  private void checkPending() {

    if (_pendingOps.isEmpty())
      return;

    RegionOp regionOp = _pendingOps.removeFirst();
    CoordinateBounds region = regionOp.getRequestRegion();

    RegionCache cache = _cache.get(region);
    if (cache != null) {
      handleRegion(region, cache);
      return;
    }

    SearchQueryBean query = new SearchQueryBean();

    query.setBounds(region);
    query.setMaxCount(MAX_STOP_COUNT_PER_REGION);
    query.setType(EQueryType.BOUNDS);
    _webappService.getStops(query, new StopHandler(regionOp));
  }

  /****
   * Private Static Methods
   ****/

  private static List<CoordinateBounds> splitRegion(CoordinateBounds region) {

    List<CoordinateBounds> splits = new ArrayList<CoordinateBounds>();

    double minLat = region.getMinLat();
    double maxLat = region.getMaxLat();
    double minLon = region.getMinLon();
    double maxLon = region.getMaxLon();
    double centerLat = (minLat + maxLat) / 2;
    double centerLon = (minLon + maxLon) / 2;

    splits.add(snapBounds(minLat, minLon, centerLat, centerLon));
    splits.add(snapBounds(minLat, centerLon, centerLat, maxLon));
    splits.add(snapBounds(centerLat, minLon, maxLat, centerLon));
    splits.add(snapBounds(centerLat, centerLon, maxLat, maxLon));

    return splits;
  }

  private static double floor(double value, double step) {
    return Math.floor(value / step) * step;
  }

  private static CoordinateBounds snapBounds(double latMin, double lonMin,
      double latMax, double lonMax) {
    return new CoordinateBounds(snap(latMin), snap(lonMin), snap(latMax),
        snap(lonMax));
  }

  private static double snap(double latOrLon) {
    return snap(latOrLon, 1e5);
  }

  private static double snap(double latOrLon, double factor) {
    return Math.round(latOrLon * factor) / factor;
  }

  private class StopHandler implements AsyncCallback<StopsBean> {

    private RegionOp _regionOp;

    public StopHandler(RegionOp regionOp) {
      _regionOp = regionOp;
    }

    public void onSuccess(StopsBean result) {

      if (result.isLimitExceeded()) {

        _cache.put(_regionOp.getRequestRegion(), new RegionCache(true));
        for (CoordinateBounds region : _regionOp.getSplitRegions())
          checkRegion(region);

      } else {

        Map<CoordinateBounds, List<StopBean>> stopsByRegion = getStopsByActualRegion(result);

        for (Map.Entry<CoordinateBounds, List<StopBean>> entry : stopsByRegion.entrySet()) {
          CoordinateBounds bounds = entry.getKey();
          List<StopBean> stops = entry.getValue();
          RegionCache cache = new RegionCache(stops, false);
          _cache.put(bounds, cache);
          handleRegion(bounds, cache);
        }
      }

      checkPending();
    }

    public void onFailure(Throwable caught) {
      checkPending();
    }

    private Map<CoordinateBounds, List<StopBean>> getStopsByActualRegion(
        StopsBean result) {

      Map<CoordinateBounds, List<StopBean>> stopsByBounds = new HashMap<CoordinateBounds, List<StopBean>>();

      List<CoordinateBounds> actualRegions = _regionOp.getActualRegions();

      // Make sure each region has a stop list, even if it ultimately has no
      // stops (want to cache that fact too)
      for (CoordinateBounds region : actualRegions)
        stopsByBounds.put(region, new ArrayList<StopBean>());

      for (StopBean stop : result.getStops()) {
        for (CoordinateBounds bounds : actualRegions) {
          if (bounds.contains(stop.getLat(), stop.getLon())) {
            List<StopBean> stops = stopsByBounds.get(bounds);
            stops.add(stop);
            continue;
          }
        }
      }
      return stopsByBounds;
    }
  }

  private static class RegionOp {

    protected final CoordinateBounds _region;

    public RegionOp(CoordinateBounds region) {
      _region = region;
    }

    public CoordinateBounds getRequestRegion() {
      return _region;
    }

    public List<CoordinateBounds> getActualRegions() {
      return Arrays.asList(_region);
    }

    public List<CoordinateBounds> getSplitRegions() {
      return splitRegion(_region);
    }

    @Override
    public String toString() {
      return "RegionOp(region=" + _region + ")";
    }
  }

  private static class MultiRegionOp extends RegionOp {

    private List<CoordinateBounds> _actualRegions;

    public MultiRegionOp(CoordinateBounds region,
        List<CoordinateBounds> actualRegions) {
      super(region);
      _actualRegions = actualRegions;
    }

    @Override
    public List<CoordinateBounds> getActualRegions() {
      return _actualRegions;
    }

    @Override
    public List<CoordinateBounds> getSplitRegions() {
      return _actualRegions;
    }

    @Override
    public String toString() {
      return "MultiRegionOp(region=" + _region + " + actualRegions="
          + _actualRegions + ")";
    }
  }

  private static class RegionCache {

    private List<StopBean> _stops;

    private boolean _overflow = false;

    public RegionCache(List<StopBean> stops, boolean limitExceeded) {
      _stops = stops;
      _overflow = limitExceeded;
    }

    @SuppressWarnings("unchecked")
    public RegionCache(boolean limitExceeded) {
      this(Collections.EMPTY_LIST, limitExceeded);
    }

    public List<StopBean> getStops() {
      return _stops;
    }

    public boolean hasOverflow() {
      return _overflow;
    }
  }
}
