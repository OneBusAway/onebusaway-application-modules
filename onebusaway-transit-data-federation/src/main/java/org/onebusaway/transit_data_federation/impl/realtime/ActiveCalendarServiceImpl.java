package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.services.realtime.ActiveCalendarService;
import org.onebusaway.transit_data_federation.services.realtime.BlockInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ActiveCalendarServiceImpl implements ActiveCalendarService {

  private CalendarService _calendarService;

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Override
  public List<BlockInstance> getActiveBlocksInTimeRange(
      List<BlockEntry> blocks, Date timeFrom, Date timeTo) {

    List<BlockInstance> blockInstances = new ArrayList<BlockInstance>();

    for (BlockEntry block : blocks) {

      ServiceIdIntervals intervals = new ServiceIdIntervals();

      Set<LocalizedServiceId> blockServiceIds = new HashSet<LocalizedServiceId>();

      for (TripEntry trip : block.getTrips()) {

        AgencyAndId serviceId = trip.getServiceId();
        LocalizedServiceId lsid = _calendarService.getLocalizedServiceIdForAgencyAndServiceId(
            trip.getId().getAgencyId(), serviceId);

        blockServiceIds.add(lsid);

        List<StopTimeEntry> stopTimes = trip.getStopTimes();
        StopTimeEntry first = stopTimes.get(0);
        StopTimeEntry last = stopTimes.get(stopTimes.size() - 1);
        intervals.addStopTime(lsid, first.getArrivalTime(),
            first.getDepartureTime());
        intervals.addStopTime(lsid, last.getArrivalTime(),
            last.getDepartureTime());
      }

      Map<LocalizedServiceId, List<Date>> serviceDatesWithinRange = _calendarService.getServiceDatesWithinRange(
          intervals, timeFrom, timeTo);

      Map<Date, Set<LocalizedServiceId>> serviceDatesByDate = getServiceIdsByDate(serviceDatesWithinRange);

      for (Map.Entry<Date, Set<LocalizedServiceId>> entry : serviceDatesByDate.entrySet()) {
        Date serviceDate = entry.getKey();
        Set<LocalizedServiceId> serviceIds = entry.getValue();

        if (serviceIds.size() < blockServiceIds.size()) {
          Set<LocalizedServiceId> extendedServiceIds = null;
          for (LocalizedServiceId lsid : blockServiceIds) {
            if (!serviceIds.contains(lsid)
                && _calendarService.isLocalizedServiceIdActiveOnDate(lsid,
                    serviceDate)) {
              if (extendedServiceIds == null)
                extendedServiceIds = new HashSet<LocalizedServiceId>(serviceIds);
              extendedServiceIds.add(lsid);
            }
          }
          if (extendedServiceIds != null)
            serviceIds = extendedServiceIds;
        }

        BlockInstance instance = new BlockInstance(block,
            serviceDate.getTime(), serviceIds);
        blockInstances.add(instance);
      }
    }

    return blockInstances;
  }

  private Map<Date, Set<LocalizedServiceId>> getServiceIdsByDate(
      Map<LocalizedServiceId, List<Date>> serviceDatesWithinRange) {
    Map<Date, Set<LocalizedServiceId>> serviceDatesByDate = new FactoryMap<Date, Set<LocalizedServiceId>>(
        new HashSet<LocalizedServiceId>());

    for (Map.Entry<LocalizedServiceId, List<Date>> entry : serviceDatesWithinRange.entrySet()) {
      LocalizedServiceId lsid = entry.getKey();
      for (Date date : entry.getValue())
        serviceDatesByDate.get(date).add(lsid);
    }
    return serviceDatesByDate;
  }
}
