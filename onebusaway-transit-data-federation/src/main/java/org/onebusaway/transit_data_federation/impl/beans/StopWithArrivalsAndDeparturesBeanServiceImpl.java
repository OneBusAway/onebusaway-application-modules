package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.collections.Counter;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.NearbyStopsBeanService;
import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
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

  @Autowired
  private ServiceAlertsBeanService _serviceAlertsBeanService;

  public StopWithArrivalsAndDeparturesBean getArrivalsAndDeparturesByStopId(
      AgencyAndId id, ArrivalsAndDeparturesQueryBean query) {

    StopBean stop = _stopBeanService.getStopForId(id);

    if (stop == null)
      return null;

    List<ArrivalAndDepartureBean> arrivalsAndDepartures = _arrivalsAndDeparturesBeanService.getArrivalsAndDeparturesByStopId(
        id, query);

    List<AgencyAndId> nearbyStopIds = _nearbyStopsBeanService.getNearbyStops(
        stop, 100);
    List<StopBean> nearbyStops = new ArrayList<StopBean>();
    for (AgencyAndId nearbyStopId : nearbyStopIds)
      nearbyStops.add(_stopBeanService.getStopForId(nearbyStopId));

    List<SituationBean> situations = _serviceAlertsBeanService.getSituationsForStopId(
        query.getTime(), id);

    return new StopWithArrivalsAndDeparturesBean(stop, arrivalsAndDepartures,
        nearbyStops, situations);
  }

  public StopsWithArrivalsAndDeparturesBean getArrivalsAndDeparturesForStopIds(
      Set<AgencyAndId> ids, ArrivalsAndDeparturesQueryBean query)
      throws NoSuchStopServiceException {

    List<StopBean> stops = new ArrayList<StopBean>();
    List<ArrivalAndDepartureBean> allArrivalsAndDepartures = new ArrayList<ArrivalAndDepartureBean>();
    Set<AgencyAndId> allNearbyStopIds = new HashSet<AgencyAndId>();
    Map<String, SituationBean> situationsById = new HashMap<String, SituationBean>();
    Counter<TimeZone> timeZones = new Counter<TimeZone>();

    for (AgencyAndId id : ids) {

      StopBean stopBean = _stopBeanService.getStopForId(id);
      stops.add(stopBean);

      List<ArrivalAndDepartureBean> arrivalsAndDepartures = _arrivalsAndDeparturesBeanService.getArrivalsAndDeparturesByStopId(
          id, query);
      allArrivalsAndDepartures.addAll(arrivalsAndDepartures);

      List<AgencyAndId> nearbyStopIds = _nearbyStopsBeanService.getNearbyStops(
          stopBean, 100);
      allNearbyStopIds.addAll(nearbyStopIds);

      TimeZone timeZone = _agencyService.getTimeZoneForAgencyId(id.getAgencyId());
      timeZones.increment(timeZone);

      List<SituationBean> situations = _serviceAlertsBeanService.getSituationsForStopId(
          query.getTime(), id);
      for (SituationBean situation : situations)
        situationsById.put(situation.getId(), situation);
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
    result.setSituations(new ArrayList<SituationBean>(situationsById.values()));
    result.setTimeZone(timeZone.getID());
    return result;
  }

}
