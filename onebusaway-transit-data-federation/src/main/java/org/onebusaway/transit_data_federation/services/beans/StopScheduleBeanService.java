package org.onebusaway.transit_data_federation.services.beans;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.StopCalendarDaysBean;
import org.onebusaway.transit_data.model.StopRouteScheduleBean;

public interface StopScheduleBeanService {

  public List<StopRouteScheduleBean> getScheduledArrivalsForStopAndDate(AgencyAndId stopId, ServiceDate date);

  public StopCalendarDaysBean getCalendarForStop(AgencyAndId stopId);
}
