package org.onebusaway.gtfs.impl.calendar;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarServiceData;
import org.onebusaway.gtfs.services.calendar.ServiceIdCalendarServiceData;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CalendarServiceDataImpl implements Serializable, CalendarServiceData {

  private static final long serialVersionUID = 1L;

  private static final DateFormat _dayFormat = new SimpleDateFormat("yyyyMMdd");

  private Map<AgencyAndId, ServiceIdCalendarServiceData> _data = new HashMap<AgencyAndId, ServiceIdCalendarServiceData>();

  private Map<String, Set<AgencyAndId>> _serviceIdsByDate = new HashMap<String, Set<AgencyAndId>>();

  public ServiceIdCalendarServiceData getDataForServiceId(AgencyAndId serviceId) {
    return _data.get(serviceId);
  }
  
  public Set<AgencyAndId> getServiceIds() {
    return Collections.unmodifiableSet(_data.keySet());
  }

  public Set<AgencyAndId> getServiceIdsForDate(Date date) {
    String d = _dayFormat.format(date);
    Set<AgencyAndId> serviceIds = _serviceIdsByDate.get(d);
    if (serviceIds == null)
      serviceIds = new HashSet<AgencyAndId>();
    return serviceIds;
  }

  public void putDataForServiceId(AgencyAndId serviceId, ServiceIdCalendarServiceData data) {
    _data.put(serviceId, data);
    for (Date serviceDate : data.getServiceDates()) {
      String d = _dayFormat.format(serviceDate);
      Set<AgencyAndId> serviceIds = _serviceIdsByDate.get(d);
      if (serviceIds == null) {
        serviceIds = new HashSet<AgencyAndId>();
        _serviceIdsByDate.put(d, serviceIds);
      }
      serviceIds.add(serviceId);
    }
  }
}
