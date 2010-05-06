package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data.model.StopAndTimeBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopTimeInstanceBean;
import org.onebusaway.transit_data.model.TripBean;
import org.onebusaway.transit_data.model.TripDetailsBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStatusBeanService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;

import edu.washington.cs.rse.text.DateLibrary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

@Component
class TripStatusBeanServiceImpl implements TripStatusBeanService {

  @Autowired
  private CalendarService _calendarService;

  @Autowired
  private TripPlannerGraph _tripPlannerGraph;

  @Autowired
  private TripBeanService _tripBeanService;

  @Autowired
  private StopBeanService _stopBeanService;

  @Transactional
  public TripDetailsBean getTripStatus(AgencyAndId tripId) {

    TripBean trip = _tripBeanService.getTripForId(tripId);
    if (trip == null)
      return null;

    TripDetailsBean bean = new TripDetailsBean();
    bean.setTrip(trip);

    bean.setStatus("default");

    TripEntry tripEntry = _tripPlannerGraph.getTripEntryForId(tripId);

    AgencyAndId serviceId = tripEntry.getServiceId();
    Set<Date> dates = _calendarService.getServiceDatesForServiceId(serviceId);

    if (!dates.isEmpty()) {
      
      Date today = DateLibrary.getTimeAsDay(new Date());
      Date serviceDate = dates.contains(today) ? today : dates.iterator().next();
      

      for (StopTimeEntry stopTime : tripEntry.getStopTimes()) {

        StopTimeInstanceProxy sti = new StopTimeInstanceProxy(stopTime, serviceDate);

        StopTimeInstanceBean stBean = new StopTimeInstanceBean();
        stBean.setArrivalTime(stopTime.getArrivalTime());
        stBean.setArrivalDate(new Date(sti.getArrivalTime()));
        stBean.setDepartureTime(stopTime.getDepartureTime());
        stBean.setDepartureDate(new Date(sti.getDepartureTime()));
        stBean.setTripId(AgencyAndIdLibrary.convertToString(tripId));
        stBean.setServiceId(AgencyAndIdLibrary.convertToString(serviceId));

        StopEntry stopEntry = stopTime.getStop();
        StopBean stopBean = _stopBeanService.getStopForId(stopEntry.getId());

        StopAndTimeBean satBean = new StopAndTimeBean();
        satBean.setStopTime(stBean);
        satBean.setStop(stopBean);
        bean.addStopAndTimeBean(satBean);
      }
    }

    if (tripEntry.getPrevTrip() != null) {
      TripBean previousTrip = _tripBeanService.getTripForId(tripEntry.getPrevTrip().getId());
      bean.setPreviousTrip(previousTrip);
    }

    if (tripEntry.getNextTrip() != null) {
      TripBean nextTrip = _tripBeanService.getTripForId(tripEntry.getNextTrip().getId());
      bean.setNextTrip(nextTrip);
    }

    return bean;
  }

}
