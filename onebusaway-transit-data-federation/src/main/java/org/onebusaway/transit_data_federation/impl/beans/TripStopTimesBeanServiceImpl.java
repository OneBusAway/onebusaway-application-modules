package org.onebusaway.transit_data_federation.impl.beans;

import java.util.TimeZone;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.TripBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.TripStopTimesBean;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripStopTimesBeanServiceImpl implements TripStopTimesBeanService {

  private TransitGraphDao _graph;

  private TripBeanService _tripBeanService;

  private StopBeanService _stopBeanService;

  private AgencyService _agencyService;

  @Autowired
  public void setTransitGraph(TransitGraphDao graph) {
    _graph = graph;
  }
  
  @Autowired
  public void setAgencyService(AgencyService agencyService) {
    _agencyService = agencyService;
  }

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService) {
    _tripBeanService = tripBeanService;
  }

  @Autowired
  public void setStopBeanService(StopBeanService stopBeanService) {
    _stopBeanService = stopBeanService;
  }

  @Override
  @Cacheable
  public TripStopTimesBean getStopTimesForTrip(AgencyAndId tripId) {

    TripEntry trip = _graph.getTripEntryForId(tripId);

    if (trip == null)
      return null;

    TripStopTimesBean bean = new TripStopTimesBean();
    
    TimeZone tz = _agencyService.getTimeZoneForAgencyId(tripId.getAgencyId());
    bean.setTimeZone(tz.getID());

    for (StopTimeEntry stopTime : trip.getStopTimes()) {

      TripStopTimeBean stBean = new TripStopTimeBean();

      stBean.setArrivalTime(stopTime.getArrivalTime());
      stBean.setDepartureTime(stopTime.getDepartureTime());

      StopEntry stopEntry = stopTime.getStop();
      StopBean stopBean = _stopBeanService.getStopForId(stopEntry.getId());
      stBean.setStop(stopBean);

      bean.addStopTime(stBean);
    }

    if (trip.getPrevTrip() != null) {
      TripBean previousTrip = _tripBeanService.getTripForId(trip.getPrevTrip().getId());
      bean.setPreviousTrip(previousTrip);
    }

    if (trip.getNextTrip() != null) {
      TripBean nextTrip = _tripBeanService.getTripForId(trip.getNextTrip().getId());
      bean.setNextTrip(nextTrip);
    }

    return bean;
  }
}
