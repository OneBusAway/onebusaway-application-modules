package org.onebusaway.gtfs_diff.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_diff.model.EntityMatch;
import org.onebusaway.gtfs_diff.model.EntityMismatch;
import org.onebusaway.gtfs_diff.model.MatchCollection;
import org.onebusaway.gtfs_diff.model.ServiceId;
import org.onebusaway.gtfs_diff.model.ServiceIdDateMismatch;
import org.onebusaway.gtfs_diff.services.GtfsDifferenceService;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServiceIdDifferenceServiceImpl extends
    AbstractDifferenceServiceImpl implements GtfsDifferenceService {

  private CalendarService _calendarService;

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  public void computeDifferences() {

    Set<AgencyAndId> serviceIds = _calendarService.getServiceIds();

    Map<AgencyAndId, AgencyAndId> ids = new HashMap<AgencyAndId, AgencyAndId>();
    for (AgencyAndId serviceId : serviceIds)
      ids.put(serviceId, serviceId);

    Map<AgencyAndId, AgencyAndId> serviceIdsA = translateIds(ids,
        _results.getModelIdA());
    Map<AgencyAndId, AgencyAndId> serviceIdsB = translateIds(ids,
        _results.getModelIdB());

    Set<AgencyAndId> commonIds = getCommonElements(serviceIdsA.keySet(),
        serviceIdsB.keySet());

    for (AgencyAndId id : commonIds) {
      ServiceId serviceIdA = new ServiceId(serviceIdsA.remove(id));
      ServiceId serviceIdB = new ServiceId(serviceIdsB.remove(id));
      EntityMatch<ServiceId> match = _results.addMatch(new EntityMatch<ServiceId>(
          serviceIdA, serviceIdB));
      compareServiceIds(serviceIdA, serviceIdB, match);
    }

    for (AgencyAndId serviceIdA : serviceIdsA.values())
      _results.addMismatch(new EntityMismatch(new ServiceId(serviceIdA), null));

    for (AgencyAndId serviceIdB : serviceIdsB.values())
      _results.addMismatch(new EntityMismatch(null, new ServiceId(serviceIdB)));

  }

  private void compareServiceIds(ServiceId serviceIdA, ServiceId serviceIdB,
      MatchCollection results) {

    Set<Date> datesA = new HashSet<Date>(
        _calendarService.getServiceDatesForServiceId(serviceIdA.getServiceId()));
    Set<Date> datesB = new HashSet<Date>(
        _calendarService.getServiceDatesForServiceId(serviceIdB.getServiceId()));
    Set<Date> commonDates = getCommonElements(datesA, datesB);

    datesA.removeAll(commonDates);
    datesB.removeAll(commonDates);

    for (Date date : datesA)
      results.addMismatch(new ServiceIdDateMismatch(serviceIdA, date,
          serviceIdB, null));

    for (Date date : datesB)
      results.addMismatch(new ServiceIdDateMismatch(serviceIdA, null,
          serviceIdB, date));

  }
}
