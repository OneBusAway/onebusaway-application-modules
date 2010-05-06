package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.NearbyStopsBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopWithArrivalsAndDeparturesBeanService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
class StopWithArrivalsAndDeparturesBeanServiceImpl implements StopWithArrivalsAndDeparturesBeanService {

  @Autowired
  private StopBeanService _stopBeanService;

  @Autowired
  private ArrivalsAndDeparturesBeanService _arrivalsAndDeparturesBeanService;

  @Autowired
  private NearbyStopsBeanService _nearbyStopsBeanService;

  public StopWithArrivalsAndDeparturesBean getArrivalsAndDeparturesByStopId(AgencyAndId id, Date time) {

    StopBean stop = _stopBeanService.getStopForId(id);

    if (stop == null)
      return null;

    List<ArrivalAndDepartureBean> arrivalsAndDepartures = _arrivalsAndDeparturesBeanService.getArrivalsAndDeparturesByStopId(
        id, time);

    List<AgencyAndId> nearbyStopIds = _nearbyStopsBeanService.getNearbyStops(stop, 100);
    List<StopBean> nearbyStops = new ArrayList<StopBean>();
    for (AgencyAndId nearbyStopId : nearbyStopIds)
      nearbyStops.add(_stopBeanService.getStopForId(nearbyStopId));

    return new StopWithArrivalsAndDeparturesBean(stop, arrivalsAndDepartures, nearbyStops);
  }

  public StopsWithArrivalsAndDeparturesBean getArrivalsAndDeparturesForStopIds(
      Set<AgencyAndId> ids, Date time) {

    List<StopBean> stops = new ArrayList<StopBean>();
    List<ArrivalAndDepartureBean> allArrivalsAndDepartures = new ArrayList<ArrivalAndDepartureBean>();
    Set<AgencyAndId> allNearbyStopIds = new HashSet<AgencyAndId>();
    
    for( AgencyAndId id : ids){

      StopBean stopBean = _stopBeanService.getStopForId(id);
      stops.add(stopBean);
      
      List<ArrivalAndDepartureBean> arrivalsAndDepartures = _arrivalsAndDeparturesBeanService.getArrivalsAndDeparturesByStopId(
          id, time);
      allArrivalsAndDepartures.addAll(arrivalsAndDepartures);
      
      List<AgencyAndId> nearbyStopIds = _nearbyStopsBeanService.getNearbyStops(stopBean, 100);
      allNearbyStopIds.addAll(nearbyStopIds);
    }
    
    allNearbyStopIds.removeAll(ids);
    List<StopBean> nearbyStops = new ArrayList<StopBean>();
    
    for( AgencyAndId id : allNearbyStopIds ) {
      StopBean stop = _stopBeanService.getStopForId(id);
      nearbyStops.add(stop);
    }
    
    StopsWithArrivalsAndDeparturesBean result = new StopsWithArrivalsAndDeparturesBean();
    result.setStops(stops);
    result.setArrivalsAndDepartures(allArrivalsAndDepartures);
    result.setNearbyStops(nearbyStops);
    return result;
  }

}
