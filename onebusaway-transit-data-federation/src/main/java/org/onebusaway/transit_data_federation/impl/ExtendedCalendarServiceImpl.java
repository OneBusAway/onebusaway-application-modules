package org.onebusaway.transit_data_federation.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExtendedCalendarServiceImpl implements ExtendedCalendarService {

  private CalendarService _calendarService;

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
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
  @Override
  public Collection<Date> getServiceDatesWithinRange(
      ServiceIdActivation serviceIds, ServiceInterval interval, Date from,
      Date to) {

    Set<Date> serviceDates = null;

    List<LocalizedServiceId> activeServiceIds = serviceIds.getActiveServiceIds();
    List<LocalizedServiceId> inactiveServiceIds = serviceIds.getInactiveServiceIds();

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
}
