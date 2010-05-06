package org.onebusaway.gtfs.services.calendar;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.Date;
import java.util.Set;

public interface CalendarServiceData {

  public ServiceIdCalendarServiceData getDataForServiceId(AgencyAndId serviceId);

  public Set<AgencyAndId> getServiceIds();

  public Set<AgencyAndId> getServiceIdsForDate(Date date);

}