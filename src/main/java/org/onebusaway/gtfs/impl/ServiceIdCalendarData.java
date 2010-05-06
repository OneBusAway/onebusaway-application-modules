/**
 * 
 */
package org.onebusaway.gtfs.impl;

import edu.emory.mathcs.backport.java.util.Collections;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

class ServiceIdCalendarData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final List<Date> _serviceDates;
  private final int _firstArrival;
  private final int _lastArrival;
  private final int _firstDeparture;
  private final int _lastDeparture;

  public ServiceIdCalendarData(List<Date> serviceDates, int firstArrival, int lastArrival, int firstDeparture,
      int lastDeparture) {
    _serviceDates = serviceDates;
    Collections.sort(_serviceDates);
    _firstArrival = firstArrival;
    _lastArrival = lastArrival;
    _firstDeparture = firstDeparture;
    _lastDeparture = lastDeparture;
  }

  public List<Date> getServiceDates() {
    return _serviceDates;
  }

  public int getFirstArrival() {
    return _firstArrival;
  }

  public int getLastArrival() {
    return _lastArrival;
  }

  public int getFirstDeparture() {
    return _firstDeparture;
  }

  public int getLastDeparture() {
    return _lastDeparture;
  }
}