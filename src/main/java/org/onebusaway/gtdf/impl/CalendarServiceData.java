package org.onebusaway.gtdf.impl;

import org.onebusaway.where.model.ServiceDate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CalendarServiceData implements Serializable {

  private static final long serialVersionUID = 1L;

  private Map<String, Set<Date>> _activeDatesByServiceId = new HashMap<String, Set<Date>>();

  private Map<Date, Set<String>> _serviceIdsByDate = new HashMap<Date, Set<String>>();

  private Map<Date, Set<ServiceDate>> _serviceDatesByDate = new HashMap<Date, Set<ServiceDate>>();

  public void setActiveDatesByServiceId(String serviceId, Set<Date> activeDates) {
    _activeDatesByServiceId.put(serviceId, activeDates);
  }

  public void addServiceDateForDate(Date date, ServiceDate serviceDate) {
    Set<ServiceDate> byDate = _serviceDatesByDate.get(date);
    if (byDate == null) {
      byDate = new HashSet<ServiceDate>();
      _serviceDatesByDate.put(date, byDate);
    }
    byDate.add(serviceDate);
  }

  public void addServiceIdForDate(Date date, String serviceId) {
    Set<String> ids = _serviceIdsByDate.get(date);
    if (ids == null) {
      ids = new HashSet<String>();
      _serviceIdsByDate.put(date, ids);
    }
    ids.add(serviceId);
  }

  public Set<String> getServiceIdsForDate(Date date) {
    return _serviceIdsByDate.get(date);
  }

  public Set<Date> getDatesForServiceId(String serviceId) {
    return _activeDatesByServiceId.get(serviceId);
  }

  public Set<ServiceDate> getServiceDatesForDate(Date date) {
    return _serviceDatesByDate.get(date);
  }
}
