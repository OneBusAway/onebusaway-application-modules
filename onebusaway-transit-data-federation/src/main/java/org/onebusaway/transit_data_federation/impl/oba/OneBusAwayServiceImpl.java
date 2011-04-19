package org.onebusaway.transit_data_federation.impl.oba;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.ItineraryBean;
import org.onebusaway.transit_data.model.tripplanning.Modes;
import org.onebusaway.transit_data.model.tripplanning.TransitLocationBean;
import org.onebusaway.transit_data_federation.impl.beans.ApplicationBeanLibrary;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.ItinerariesBeanService;
import org.onebusaway.transit_data_federation.services.oba.OneBusAwayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class OneBusAwayServiceImpl implements OneBusAwayService {

  private ItinerariesBeanService _itinerariesService;

  @Autowired
  public void setItinerariesBeanService(
      ItinerariesBeanService itinerariesService) {
    _itinerariesService = itinerariesService;
  }

  public List<TimedPlaceBean> getLocalPaths(ConstraintsBean constraints,
      MinTravelTimeToStopsBean travelTimes, List<LocalSearchResult> localResults)
      throws ServiceException {

    long maxTripLength = constraints.getMaxTripDuration() * 1000;

    List<TimedPlaceBean> beans = new ArrayList<TimedPlaceBean>();

    double walkingVelocity = travelTimes.getWalkingVelocity() / 1000;

    ConstraintsBean walkConstraints = new ConstraintsBean(constraints);
    walkConstraints.setModes(CollectionsLibrary.set(Modes.WALK));
    long time = System.currentTimeMillis();

    for (LocalSearchResult result : localResults) {

      double placeLat = result.getLat();
      double placeLon = result.getLon();

      List<TripToStop> closestStops = new ArrayList<TripToStop>();

      for (int index = 0; index < travelTimes.getSize(); index++) {

        String stopIdAsString = travelTimes.getStopId(index);
        AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(stopIdAsString);

        long currentTripDuration = travelTimes.getTravelTime(index);
        double stopLat = travelTimes.getStopLat(index);
        double stopLon = travelTimes.getStopLon(index);
        double d = SphericalGeometryLibrary.distance(stopLat, stopLon,
            placeLat, placeLon);
        double t = currentTripDuration + d / walkingVelocity;
        if (d <= constraints.getMaxWalkingDistance() && t < maxTripLength) {
          closestStops.add(new TripToStop(stopId, currentTripDuration, t, index));
        }
      }

      if (closestStops.isEmpty())
        continue;

      Collections.sort(closestStops);

      double minTime = 0;
      TripToStop minStop = null;

      TransitLocationBean place = new TransitLocationBean();
      place.setLat(result.getLat());
      place.setLon(result.getLon());

      for (TripToStop o : closestStops) {

        long currentTripDuration = o.getTransitTimeToStop();
        double minTimeToPlace = o.getMinTansitTimeToPlace();

        // Short circuit if there is no way any of the remaining trips is going
        // to be better than our current winner
        if (minStop != null && minTimeToPlace > minTime)
          break;

        int remainingTime = (int) ((maxTripLength - currentTripDuration) / 1000);
        walkConstraints.setMaxTripDuration(remainingTime);

        int index = o.getIndex();
        TransitLocationBean stopLocation = new TransitLocationBean();
        stopLocation.setLat(travelTimes.getStopLat(index));
        stopLocation.setLon(travelTimes.getStopLon(index));
        
        ItinerariesBean itineraries = _itinerariesService.getItinerariesBetween(
            stopLocation, place, time, System.currentTimeMillis(), walkConstraints);

        for (ItineraryBean plan : itineraries.getItineraries()) {
          double t = currentTripDuration
              + (plan.getEndTime() - plan.getStartTime());
          if (minStop == null || t < minTime) {
            minTime = t;
            minStop = o;
          }
        }
      }

      if (minStop != null && minTime <= maxTripLength) {
        TimedPlaceBean bean = new TimedPlaceBean();
        bean.setPlaceId(result.getId());
        bean.setStopId(ApplicationBeanLibrary.getId(minStop.getStopId()));
        bean.setTime((int) (minTime / 1000));
        beans.add(bean);
      }
    }

    return beans;
  }

  /****
   * Private Methods
   ****/

  private static class TripToStop implements Comparable<TripToStop> {

    private AgencyAndId _stopId;
    private long _transitTimeToStop;
    private double _minTransitTimeToPlace;
    private int _index;

    public TripToStop(AgencyAndId stopId, long transitTimeToStop,
        double minTransitTimeToPlace, int index) {
      _stopId = stopId;
      _transitTimeToStop = transitTimeToStop;
      _minTransitTimeToPlace = minTransitTimeToPlace;
      _index = index;
    }

    public AgencyAndId getStopId() {
      return _stopId;
    }

    public long getTransitTimeToStop() {
      return _transitTimeToStop;
    }

    public double getMinTansitTimeToPlace() {
      return _minTransitTimeToPlace;
    }

    public int getIndex() {
      return _index;
    }

    public int compareTo(TripToStop o) {
      return _minTransitTimeToPlace == o._minTransitTimeToPlace ? 0
          : (_minTransitTimeToPlace < o._minTransitTimeToPlace ? -1 : 1);
    }
  }
}
