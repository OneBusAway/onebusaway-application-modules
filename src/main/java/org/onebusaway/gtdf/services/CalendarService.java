package org.onebusaway.gtdf.services;

import org.onebusaway.where.model.ServiceDate;

import java.util.Date;
import java.util.Set;

public interface CalendarService {
  public Set<ServiceDate> getServiceDatesWithinRange(Date from, Date to);

  public Set<String> getServiceIdsOnDate(Date date);

  public Set<Date> getDatesForServiceId(String serviceId);
}
