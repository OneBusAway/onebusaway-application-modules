package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.TripBean;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.narrative.StopTimeNarrativeService;
import org.onebusaway.transit_data_federation.services.predictions.StopTimePredictionService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

@Component
class ArrivalsAndDeparturesBeanServiceImpl implements
    ArrivalsAndDeparturesBeanService {

  @Autowired
  private StopTimeService _stopTimeService;

  @Autowired
  private StopTimePredictionService _stopTimePredictionService;

  @Autowired
  private TripBeanService _tripBeanService;

  @Autowired
  private RouteBeanService _routeBeanService;

  @Autowired
  private StopTimeNarrativeService _stopTimeNarrativeService;

  @Transactional
  public List<ArrivalAndDepartureBean> getArrivalsAndDeparturesByStopId(
      AgencyAndId id, Date targetTime) {

    Calendar c = Calendar.getInstance();
    c.setTime(targetTime);
    c.add(Calendar.MINUTE, -30);
    Date from = c.getTime();
    c.add(Calendar.MINUTE, 65);
    Date to = c.getTime();

    List<StopTimeInstanceProxy> stis = _stopTimeService.getStopTimeInstancesInTimeRange(
        id, from, to);
    _stopTimePredictionService.applyPredictions(stis);

    List<ArrivalAndDepartureBean> beans = new ArrayList<ArrivalAndDepartureBean>();
    for (StopTimeInstanceProxy sti : stis)
      beans.add(getStopTimeInstanceAsBean(sti));

    List<ArrivalAndDepartureBean> filtered = new ArrayList<ArrivalAndDepartureBean>();

    long fromReduced = targetTime.getTime() - 5 * 60 * 1000;
    long toReduced = targetTime.getTime() + 35 * 60 * 1000;

    for (ArrivalAndDepartureBean bean : beans) {
      if (isArrivalAndDepartureBeanInRange(bean, fromReduced, toReduced))
        filtered.add(bean);
    }

    Collections.sort(filtered, new ArrivalAndDepartureComparator());

    return filtered;
  }

  private ArrivalAndDepartureBean getStopTimeInstanceAsBean(
      StopTimeInstanceProxy sti) {

    ArrivalAndDepartureBean pab = new ArrivalAndDepartureBean();

    pab.setScheduledArrivalTime(sti.getArrivalTime());
    pab.setScheduledDepartureTime(sti.getDepartureTime());

    if (sti.hasPredictedArrivalOffset())
      pab.setPredictedArrivalTime(sti.getDepartureTime()
          + sti.getPredictedArrivalOffset() * 1000);

    if (sti.hasPredictedDepartureOffset())
      pab.setPredictedDepartureTime(sti.getDepartureTime()
          + sti.getPredictedDepartureOffset() * 1000);

    StopTimeEntry stopTime = sti.getStopTime();
    StopEntry stop = sti.getStop();
    TripEntry trip = sti.getTrip();

    StopTimeNarrative stopTimeNarrative = _stopTimeNarrativeService.getStopTimeForEntry(stopTime);
    TripBean tripBean = _tripBeanService.getTripForId(trip.getId());
    RouteBean route = _routeBeanService.getRouteForId(trip.getRouteCollectionId());

    String routeShortName = ApplicationBeanLibrary.getBestName(
        stopTimeNarrative.getRouteShortName(), tripBean.getRouteShortName(),
        route.getShortName());
    pab.setRouteShortName(routeShortName);

    String tripHeadsign = ApplicationBeanLibrary.getBestName(
        stopTimeNarrative.getStopHeadsign(), tripBean.getTripHeadsign(),
        route.getLongName());
    pab.setTripHeadsign(tripHeadsign);

    pab.setRouteId(ApplicationBeanLibrary.getId(trip.getRouteCollectionId()));
    pab.setTripId(ApplicationBeanLibrary.getId(trip.getId()));
    pab.setStopId(ApplicationBeanLibrary.getId(stop.getId()));

    pab.setStatus("default");

    return pab;
  }

  private boolean isArrivalAndDepartureBeanInRange(
      ArrivalAndDepartureBean bean, long timeFrom, long timeTo) {
    if (timeFrom <= bean.getScheduledArrivalTime()
        && bean.getScheduledArrivalTime() <= timeTo)
      return true;
    if (timeFrom <= bean.getScheduledDepartureTime()
        && bean.getScheduledDepartureTime() <= timeTo)
      return true;
    if (bean.hasPredictedArrivalTime()
        && timeFrom <= bean.getPredictedArrivalTime()
        && bean.getPredictedArrivalTime() <= timeTo)
      return true;
    if (bean.hasPredictedDepartureTime()
        && timeFrom <= bean.getPredictedDepartureTime()
        && bean.getPredictedDepartureTime() <= timeTo)
      return true;
    return false;
  }

  private static class ArrivalAndDepartureComparator implements
      Comparator<ArrivalAndDepartureBean> {

    public int compare(ArrivalAndDepartureBean o1, ArrivalAndDepartureBean o2) {
      long t1 = o1.getScheduledArrivalTime();
      if (o1.hasPredictedArrivalTime())
        t1 = o1.getPredictedArrivalTime();
      long t2 = o2.getScheduledArrivalTime();
      if (o2.hasPredictedArrivalTime())
        t2 = o2.getPredictedArrivalTime();
      return (int) (t1 - t2);
    }
  }

}
