package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.collections.Counter;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.NearbyStopsBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopWithArrivalsAndDeparturesBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class StopWithArrivalsAndDeparturesBeanServiceImpl implements
    StopWithArrivalsAndDeparturesBeanService {

  @Autowired
  private StopBeanService _stopBeanService;

  @Autowired
  private ArrivalsAndDeparturesBeanService _arrivalsAndDeparturesBeanService;

  @Autowired
  private NearbyStopsBeanService _nearbyStopsBeanService;

  @Autowired
  private AgencyService _agencyService;

  public StopWithArrivalsAndDeparturesBean getArrivalsAndDeparturesByStopId(
      AgencyAndId id, Date timeFrom, Date timeTo) {

    StopBean stop = _stopBeanService.getStopForId(id);

    if (stop == null)
      return null;

    List<ArrivalAndDepartureBean> arrivalsAndDepartures = _arrivalsAndDeparturesBeanService.getArrivalsAndDeparturesByStopId(
        id, timeFrom, timeTo);

    List<AgencyAndId> nearbyStopIds = _nearbyStopsBeanService.getNearbyStops(
        stop, 100);
    List<StopBean> nearbyStops = new ArrayList<StopBean>();
    for (AgencyAndId nearbyStopId : nearbyStopIds)
      nearbyStops.add(_stopBeanService.getStopForId(nearbyStopId));

    return new StopWithArrivalsAndDeparturesBean(stop, arrivalsAndDepartures,
        nearbyStops);
  }

  public StopsWithArrivalsAndDeparturesBean getArrivalsAndDeparturesForStopIds(
      Set<AgencyAndId> ids, Date timeFrom, Date timeTo)
      throws NoSuchStopServiceException {

    List<StopBean> stops = new ArrayList<StopBean>();
    List<ArrivalAndDepartureBean> allArrivalsAndDepartures = new ArrayList<ArrivalAndDepartureBean>();
    Set<AgencyAndId> allNearbyStopIds = new HashSet<AgencyAndId>();

    Counter<TimeZone> timeZones = new Counter<TimeZone>();

    for (AgencyAndId id : ids) {

      StopBean stopBean = _stopBeanService.getStopForId(id);

      if (stopBean == null)
        throw new NoSuchStopServiceException(
            AgencyAndIdLibrary.convertToString(id));

      stops.add(stopBean);

      List<ArrivalAndDepartureBean> arrivalsAndDepartures = _arrivalsAndDeparturesBeanService.getArrivalsAndDeparturesByStopId(
          id, timeFrom, timeTo);
      allArrivalsAndDepartures.addAll(arrivalsAndDepartures);

      List<AgencyAndId> nearbyStopIds = _nearbyStopsBeanService.getNearbyStops(
          stopBean, 100);
      allNearbyStopIds.addAll(nearbyStopIds);

      TimeZone timeZone = _agencyService.getTimeZoneForAgencyId(id.getAgencyId());
      timeZones.increment(timeZone);
    }

    allNearbyStopIds.removeAll(ids);
    List<StopBean> nearbyStops = new ArrayList<StopBean>();

    for (AgencyAndId id : allNearbyStopIds) {
      StopBean stop = _stopBeanService.getStopForId(id);
      nearbyStops.add(stop);
    }

    TimeZone timeZone = timeZones.getMax();
    if (timeZone == null)
      timeZone = TimeZone.getDefault();

    StopsWithArrivalsAndDeparturesBean result = new StopsWithArrivalsAndDeparturesBean();
    result.setStops(stops);
    result.setArrivalsAndDepartures(allArrivalsAndDepartures);
    result.setNearbyStops(nearbyStops);
    result.setTimeZone(timeZone.getID());
    return result;
  }

}
