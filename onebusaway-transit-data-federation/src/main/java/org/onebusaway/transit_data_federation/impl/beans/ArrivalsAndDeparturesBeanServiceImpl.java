package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.TripBean;
import org.onebusaway.transit_data.model.RouteBean.Builder;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.predictions.StopTimePredictionService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource("org.onebusaway.transit_data_federation.impl.beans:name=ArrivalsAndDeparturesBeanServiceImpl")
public class ArrivalsAndDeparturesBeanServiceImpl implements
    ArrivalsAndDeparturesBeanService {

  @Autowired
  private StopTimeService _stopTimeService;

  @Autowired
  private StopTimePredictionService _stopTimePredictionService;

  @Autowired
  private NarrativeService _narrativeService;

  @Autowired
  private TripBeanService _tripBeanService;
  
  private AtomicInteger _stopTimesTotal = new AtomicInteger();
  
  private AtomicInteger _stopTimesWithPredictions = new AtomicInteger();
  
  @ManagedAttribute()
  public int getStopTimesTotal() {
    return _stopTimesTotal.intValue();
  }

  @ManagedAttribute
  public int getStopTimesWithPredictions() {
    return _stopTimesWithPredictions.intValue();
  }

  public List<ArrivalAndDepartureBean> getArrivalsAndDeparturesByStopId(
      AgencyAndId id, Date timeFrom, Date timeTo) {

    Calendar c = Calendar.getInstance();
    
    // We add a buffer before and after to catch late and early buses
    c.setTime(timeFrom);
    c.add(Calendar.MINUTE, -30);
    Date from = c.getTime();
    
    c.setTime(timeTo);
    c.add(Calendar.MINUTE, 10);
    Date to = c.getTime();

    List<StopTimeInstanceProxy> stis = _stopTimeService.getStopTimeInstancesInTimeRange(
        id, from, to);
    _stopTimePredictionService.applyPredictions(stis);

    List<ArrivalAndDepartureBean> beans = new ArrayList<ArrivalAndDepartureBean>();
    for (StopTimeInstanceProxy sti : stis)
      beans.add(getStopTimeInstanceAsBean(sti));

    List<ArrivalAndDepartureBean> filtered = new ArrayList<ArrivalAndDepartureBean>();

    long fromReduced = timeFrom.getTime();
    long toReduced = timeTo.getTime();

    for (ArrivalAndDepartureBean bean : beans) {
      if (isArrivalAndDepartureBeanInRange(bean, fromReduced, toReduced)) {
        filtered.add(bean);
        _stopTimesTotal.incrementAndGet();
        if( bean.hasPredictedArrivalTime() )
          _stopTimesWithPredictions.incrementAndGet();
      }
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

    TripBean tripBean = _tripBeanService.getTripForId(trip.getId());
    StopTimeNarrative stopTimeNarrative = _narrativeService.getStopTimeForEntry(stopTime);

    tripBean = applyStopTimeNarrativeToTripBean(stopTimeNarrative, tripBean);

    pab.setTrip(tripBean);
    pab.setStopId(ApplicationBeanLibrary.getId(stop.getId()));

    pab.setStatus("default");

    return pab;
  }

  private TripBean applyStopTimeNarrativeToTripBean(
      StopTimeNarrative stopTimeNarrative, TripBean tripBean) {

    tripBean = new TripBean(tripBean);
    
    String stopHeadsign = stopTimeNarrative.getStopHeadsign();
    if (stopHeadsign != null && stopHeadsign.length() > 0)
      tripBean.setTripHeadsign(stopHeadsign);
    
    String routeShortName = stopTimeNarrative.getRouteShortName();
    if( routeShortName != null && routeShortName.length() > 0) {
      RouteBean routeBean = tripBean.getRoute();
      Builder builder = RouteBean.builder(routeBean);
      builder.setShortName(routeShortName);
      routeBean = builder.create();
      tripBean.setRoute(routeBean);
    }

    return tripBean;
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
