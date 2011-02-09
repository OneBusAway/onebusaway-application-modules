/**
 * 
 */
package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.List;

public class StopTimeIndexResult {

  private final List<StopTimeInstance> _stopTimeInstances;

  private final Object _hint;

  public StopTimeIndexResult(List<StopTimeInstance> stopTimeInstances, Object hint) {
    _stopTimeInstances = stopTimeInstances;
    _hint = null;
  }

  public List<StopTimeInstance> getStopTimeInstances() {
    return _stopTimeInstances;
  }

  public Object getHint() {
    return _hint;
  }
}