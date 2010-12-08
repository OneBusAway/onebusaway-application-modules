package org.onebusaway.transit_data_federation.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExtendedCalendarServiceImpl implements ExtendedCalendarService {

  private CalendarService _calendarService;

  private double _serviceDateRangeCacheInterval = 4 * 60 * 60;

  private Cache _serviceDateRangeCache;

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  public void setServiceDateRangeCacheInterval(int hours) {
    _serviceDateRangeCacheInterval = hours * 60 * 60;
  }

  public void setServiceDateRangeCache(Cache serviceDateRangeCache) {
    _serviceDateRangeCache = serviceDateRangeCache;
  }

  @Cacheable
  @Override
  public Set<ServiceDate> getServiceDatesForServiceIds(
      ServiceIdActivation serviceIds) {

    Set<ServiceDate> serviceDates = null;

    List<LocalizedServiceId> activeServiceIds = serviceIds.getActiveServiceIds();
    List<LocalizedServiceId> inactiveServiceIds = serviceIds.getInactiveServiceIds();

    for (LocalizedServiceId activeServiceId : activeServiceIds) {

      Set<ServiceDate> dates = _calendarService.getServiceDatesForServiceId(activeServiceId.getId());
      if (dates.isEmpty())
        return Collections.emptySet();
      if (serviceDates == null)
        serviceDates = new HashSet<ServiceDate>(dates);
      else
        serviceDates.retainAll(dates);
      if (serviceDates.isEmpty())
        return Collections.emptySet();
    }

    for (LocalizedServiceId inactiveServiceId : inactiveServiceIds) {
      Set<ServiceDate> dates = _calendarService.getServiceDatesForServiceId(inactiveServiceId.getId());
      serviceDates.removeAll(dates);
    }

    return serviceDates;
  }

  @Cacheable
  @Override
  public Set<Date> getDatesForServiceIds(ServiceIdActivation serviceIds) {

    Set<Date> serviceDates = null;

    List<LocalizedServiceId> activeServiceIds = serviceIds.getActiveServiceIds();
    List<LocalizedServiceId> inactiveServiceIds = serviceIds.getInactiveServiceIds();

    for (LocalizedServiceId activeServiceId : activeServiceIds) {

      List<Date> dates = _calendarService.getDatesForLocalizedServiceId(activeServiceId);
      if (dates.isEmpty())
        return Collections.emptySet();
      if (serviceDates == null)
        serviceDates = new HashSet<Date>(dates);
      else
        serviceDates.retainAll(dates);
      if (serviceDates.isEmpty())
        return Collections.emptySet();
    }

    for (LocalizedServiceId inactiveServiceId : inactiveServiceIds) {
      List<Date> dates = _calendarService.getDatesForLocalizedServiceId(inactiveServiceId);
      serviceDates.removeAll(dates);
    }

    return serviceDates;
  }

  @Cacheable
  public List<Date> getDatesForServiceIdsAsOrderedList(
      ServiceIdActivation serviceIds) {
    Set<Date> dates = getDatesForServiceIds(serviceIds);
    List<Date> list = new ArrayList<Date>(dates);
    Collections.sort(list);
    return list;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Date> getServiceDatesWithinRange(
      ServiceIdActivation serviceIds, ServiceInterval interval, Date from,
      Date to) {

    if (_serviceDateRangeCache == null)
      return getServiceDatesWithinRangeExact(serviceIds, interval, from, to);

    ServiceDateRangeKey key = getCacheKey(serviceIds, interval, from, to);
    Element element = _serviceDateRangeCache.get(key);

    if (element == null) {

      serviceIds = key.getServiceIds();
      interval = key.getInterval();
      from = key.getFromTime();
      to = key.getToTime();

      Collection<Date> values = getServiceDatesWithinRangeExact(serviceIds,
          interval, from, to);

      element = new Element(key, values);
      _serviceDateRangeCache.put(element);
    }

    return (Collection<Date>) element.getValue();
  }

  /****
   * Private Methods
   ****/

  private ServiceDateRangeKey getCacheKey(ServiceIdActivation serviceIds,
      ServiceInterval interval, Date from, Date to) {

    Serializable serviceIdsKey = getServiceIdsKey(serviceIds);
    int fromStopTime = (int) (Math.floor(interval.getMinArrival()
        / _serviceDateRangeCacheInterval) * _serviceDateRangeCacheInterval);
    int toStopTime = (int) (Math.ceil(interval.getMaxDeparture()
        / _serviceDateRangeCacheInterval) * _serviceDateRangeCacheInterval);
    double m = _serviceDateRangeCacheInterval * 1000;
    long fromTime = (long) (Math.floor(from.getTime() / m) * m);
    long toTime = (long) (Math.ceil(from.getTime() / m) * m);
    return new ServiceDateRangeKey(serviceIdsKey, fromStopTime, toStopTime,
        fromTime, toTime);
  }

  private Serializable getServiceIdsKey(ServiceIdActivation serviceIds) {

    List<LocalizedServiceId> activeServiceIds = serviceIds.getActiveServiceIds();
    List<LocalizedServiceId> inactiveServiceIds = serviceIds.getInactiveServiceIds();

    if (activeServiceIds.size() == 1 && inactiveServiceIds.isEmpty())
      return activeServiceIds.get(0);

    return serviceIds;
  }

  private Collection<Date> getServiceDatesWithinRangeExact(
      ServiceIdActivation serviceIds, ServiceInterval interval, Date from,
      Date to) {
    Set<Date> serviceDates = null;

    List<LocalizedServiceId> activeServiceIds = serviceIds.getActiveServiceIds();
    List<LocalizedServiceId> inactiveServiceIds = serviceIds.getInactiveServiceIds();

    // System.out.println(serviceIds + " " + interval + " " + from + " " + to);

    // 95% of configs look like this
    if (activeServiceIds.size() == 1 && inactiveServiceIds.isEmpty())
      return _calendarService.getServiceDatesWithinRange(
          activeServiceIds.get(0), interval, from, to);

    for (LocalizedServiceId serviceId : activeServiceIds) {
      List<Date> dates = _calendarService.getServiceDatesWithinRange(serviceId,
          interval, from, to);

      // If the dates are ever empty here, we can short circuit to no dates
      if (dates.isEmpty())
        return Collections.emptyList();

      if (serviceDates == null)
        serviceDates = new HashSet<Date>(dates);
      else
        serviceDates.retainAll(serviceDates);

      // If the dates are empty here after the intersection operation, we can
      // short circuit to no dates
      if (serviceDates.isEmpty())
        return Collections.emptyList();
    }

    if (!inactiveServiceIds.isEmpty()) {
      for (LocalizedServiceId serviceId : inactiveServiceIds) {
        List<Date> dates = _calendarService.getServiceDatesWithinRange(
            serviceId, interval, from, to);
        serviceDates.removeAll(dates);
      }
    }

    return serviceDates;
  }

  private class ServiceDateRangeKey {
    private final Serializable _serviceIds;
    private final int _fromStopTime;
    private final int _toStopTime;
    private final long _fromTime;
    private final long _toTime;

    public ServiceDateRangeKey(Serializable serviceIds, int fromStopTime,
        int toStopTime, long fromTime, long toTime) {
      if (serviceIds == null)
        throw new IllegalStateException("serviceIds cannot be null");
      _serviceIds = serviceIds;
      _fromStopTime = fromStopTime;
      _toStopTime = toStopTime;
      _fromTime = fromTime;
      _toTime = toTime;
    }

    public ServiceIdActivation getServiceIds() {
      if (_serviceIds instanceof ServiceIdActivation) {
        return (ServiceIdActivation) _serviceIds;
      } else if (_serviceIds instanceof LocalizedServiceId) {
        return new ServiceIdActivation((LocalizedServiceId) _serviceIds);
      } else {
        throw new IllegalStateException("unknown service id type: "
            + _serviceIds);
      }
    }

    public ServiceInterval getInterval() {
      return new ServiceInterval(_fromStopTime, _toStopTime);
    }

    public Date getFromTime() {
      return new Date(_fromTime);
    }

    public Date getToTime() {
      return new Date(_toTime);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _fromStopTime;
      result = prime * result + (int) (_fromTime ^ (_fromTime >>> 32));
      result = prime * result + _serviceIds.hashCode();
      result = prime * result + _toStopTime;
      result = prime * result + (int) (_toTime ^ (_toTime >>> 32));
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ServiceDateRangeKey other = (ServiceDateRangeKey) obj;
      if (_fromStopTime != other._fromStopTime)
        return false;
      if (_fromTime != other._fromTime)
        return false;
      if (!_serviceIds.equals(other._serviceIds))
        return false;
      if (_toStopTime != other._toStopTime)
        return false;
      if (_toTime != other._toTime)
        return false;
      return true;
    }
  }
}
