package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopCalendarDayBean;
import org.onebusaway.transit_data.model.StopRouteScheduleBean;

import java.util.Date;
import java.util.List;

public interface StopScheduleBeanService {

  public List<StopRouteScheduleBean> getScheduledArrivalsForStopAndDate(AgencyAndId stopId, Date date);

  public List<StopCalendarDayBean> getCalendarForStop(AgencyAndId stopId);
}
