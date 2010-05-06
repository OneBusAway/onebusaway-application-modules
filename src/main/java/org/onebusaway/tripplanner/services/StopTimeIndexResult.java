/**
 * 
 */
package org.onebusaway.tripplanner.services;

import java.util.List;

public class StopTimeIndexResult {

  private final List<StopTimeInstanceProxy> _stopTimeInstances;

  private final Object _hint;

  public StopTimeIndexResult(List<StopTimeInstanceProxy> stopTimeInstances, Object hint) {
    _stopTimeInstances = stopTimeInstances;
    _hint = null;
  }

  public List<StopTimeInstanceProxy> getStopTimeInstances() {
    return _stopTimeInstances;
  }

  public Object getHint() {
    return _hint;
  }
}