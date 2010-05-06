/**
 * 
 */
package org.onebusaway.metrokc2gtfs.calendar;

import org.onebusaway.metrokc2gtfs.model.ServiceIdModification;

import java.util.Date;
import java.util.Set;

class ServiceIdModificationImpl implements ServiceIdModification {

  private Set<Date> _dates;

  private int _offset = 0;

  private String _suffix = null;

  public void setDates(Set<Date> dates) {
    _dates = dates;
  }

  public void setOffset(int offset) {
    _offset = offset;
  }

  public void setSuffix(String suffix) {
    _suffix = suffix;
  }

  public String getServiceId(String serviceId) {
    if (_suffix == null)
      return serviceId;
    return serviceId + _suffix;
  }

  public Set<Date> getDates() {
    return _dates;
  }

  public int applyPassingTimeTransformation(int passingTime) {
    return passingTime + _offset;
  }

  public String getGtfsTripId(String tripId) {
    if (_suffix == null)
      return tripId;
    return tripId + _suffix;
  }

  public String getGtfsBlockId(String blockId) {
    if (_suffix == null)
      return blockId;
    return blockId + _suffix;
  }

}