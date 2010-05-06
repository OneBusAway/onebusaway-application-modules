/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.model.Indexed;
import org.onebusaway.kcmetro2gtfs.model.MetroKCPatternTimepoint;
import org.onebusaway.kcmetro2gtfs.model.MetroKCStop;
import org.onebusaway.kcmetro2gtfs.model.ServicePatternKey;
import org.onebusaway.kcmetro2gtfs.model.StopTimepointInterpolation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class StopTimeInterpolationHandler implements Runnable {

  private TranslationContext _context;

  private ServicePatternHandler _spHandler;

  private Map<ServicePatternKey, SortedMap<Indexed<MetroKCStop>, StopTimepointInterpolation>> _results = new HashMap<ServicePatternKey, SortedMap<Indexed<MetroKCStop>, StopTimepointInterpolation>>();

  private Map<ServicePatternKey, Map<MetroKCPatternTimepoint, Double>> _timepointDistanceOffsets = new HashMap<ServicePatternKey, Map<MetroKCPatternTimepoint, Double>>();

  public StopTimeInterpolationHandler(TranslationContext context) {
    _context = context;
    _spHandler = context.getHandler(ServicePatternHandler.class);
  }

  public SortedMap<Indexed<MetroKCStop>, StopTimepointInterpolation> getStisByServicePattern(
      ServicePatternKey servicePatternId) {
    return _results.get(servicePatternId);
  }

  public Map<MetroKCPatternTimepoint, Double> getTimepointDistanceOffsetsByServicePattern(
      ServicePatternKey servicePatternId) {
    return _timepointDistanceOffsets.get(servicePatternId);
  }

  public void run() {

    List<ServicePatternKey> patterns = getServicePatterns();
    int index = 0;

    for (ServicePatternKey servicePattern : patterns) {

      if (true) {
        Route route = _spHandler.getRouteByServicePatternKey(servicePattern);
        System.out.println(servicePattern.getId() + " route="
            + route.getShortName() + " index=" + (index++) + "/"
            + patterns.size());
      }

      ServicePatternStopInterpolation interp = new ServicePatternStopInterpolation(
          _context, servicePattern);

      SortedMap<Indexed<MetroKCStop>, StopTimepointInterpolation> stis = interp.run();
      _results.put(servicePattern, stis);

      Map<MetroKCPatternTimepoint, Double> timepointDistanceOffsets = interp.getPatternTimepointDistanceOffsets();
      _timepointDistanceOffsets.put(servicePattern, timepointDistanceOffsets);
    }
  }

  private List<ServicePatternKey> getServicePatterns() {
    List<ServicePatternKey> patterns = new ArrayList<ServicePatternKey>(
        _spHandler.getKeys());
    Collections.sort(patterns, new ServicePatternComparator());
    return patterns;
  }

  private class ServicePatternComparator implements
      Comparator<ServicePatternKey> {

    public int compare(ServicePatternKey o1, ServicePatternKey o2) {
      Route r1 = _spHandler.getRouteByServicePatternKey(o1);
      Route r2 = _spHandler.getRouteByServicePatternKey(o2);

      int rc = r1.getShortName().compareTo(r2.getShortName());

      return rc == 0 ? o1.compareTo(o2) : rc;
    }

  }
}
