package org.onebusaway.gtfs.impl;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServiceCalendarData implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final DateFormat _dayFormat = new SimpleDateFormat("yyyyMMdd");

  private Map<String, ServiceIdCalendarData> _data = new HashMap<String, ServiceIdCalendarData>();

  private Map<String, Set<String>> _serviceIdsByDate = new HashMap<String, Set<String>>();

  public ServiceIdCalendarData getDataForServiceId(String serviceId) {
    return _data.get(serviceId);
  }

  public Set<String> getServiceIdsForDate(Date date) {
    String d = _dayFormat.format(date);
    Set<String> serviceIds = _serviceIdsByDate.get(d);
    if (serviceIds == null)
      serviceIds = new HashSet<String>();
    return serviceIds;
  }

  public void putDataForServiceId(String serviceId, ServiceIdCalendarData data) {
    _data.put(serviceId, data);
    for (Date serviceDate : data.getServiceDates()) {
      String d = _dayFormat.format(serviceDate);
      Set<String> serviceIds = _serviceIdsByDate.get(d);
      if (serviceIds == null) {
        serviceIds = new HashSet<String>();
        _serviceIdsByDate.put(d, serviceIds);
      }
      serviceIds.add(serviceId);
    }
  }
}
