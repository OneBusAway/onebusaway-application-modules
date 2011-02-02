package org.onebusaway.api.model.transit;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.api.model.transit.tripplanning.ItinerariesV2Bean;
import org.onebusaway.api.model.transit.tripplanning.ItineraryV2Bean;
import org.onebusaway.api.model.transit.tripplanning.LegV2Bean;
import org.onebusaway.api.model.transit.tripplanning.LocationV2Bean;
import org.onebusaway.api.model.transit.tripplanning.StreetLegV2Bean;
import org.onebusaway.api.model.transit.tripplanning.TransitLegV2Bean;
import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.ItineraryBean;
import org.onebusaway.transit_data.model.tripplanning.LegBean;
import org.onebusaway.transit_data.model.tripplanning.LocationBean;
import org.onebusaway.transit_data.model.tripplanning.StreetLegBean;
import org.onebusaway.transit_data.model.tripplanning.TransitLegBean;
import org.onebusaway.transit_data.model.trips.TripBean;

public class ItineraryV2BeanFactory {

  private final BeanFactoryV2 _factory;

  public ItineraryV2BeanFactory(BeanFactoryV2 factory) {
    _factory = factory;
  }

  public ItinerariesV2Bean getItineraries(ItinerariesBean itineraries) {

    ItinerariesV2Bean bean = new ItinerariesV2Bean();

    bean.setFrom(getLocation(itineraries.getFrom()));
    bean.setTo(getLocation(itineraries.getTo()));

    List<ItineraryBean> its = itineraries.getItineraries();

    if (!CollectionsLibrary.isEmpty(its)) {
      List<ItineraryV2Bean> beans = new ArrayList<ItineraryV2Bean>(its.size());
      for (ItineraryBean itinerary : its) {
        ItineraryV2Bean itBean = getItinerary(itinerary);
        beans.add(itBean);
      }
      bean.setItineraries(beans);
    }

    return bean;
  }

  public LocationV2Bean getLocation(LocationBean location) {

    LocationV2Bean bean = new LocationV2Bean();
    bean.setLocation(location.getLocation());
    bean.setName(location.getName());
    StopBean stop = location.getStopBean();

    if (stop != null) {
      bean.setStopId(stop.getId());
      _factory.addToReferences(stop);
    }

    return bean;
  }

  public ItineraryV2Bean getItinerary(ItineraryBean itinerary) {

    ItineraryV2Bean bean = new ItineraryV2Bean();

    bean.setStartTime(itinerary.getStartTime());
    bean.setEndTime(itinerary.getEndTime());

    List<LegBean> legs = itinerary.getLegs();
    if (!CollectionsLibrary.isEmpty(legs)) {
      List<LegV2Bean> legBeans = new ArrayList<LegV2Bean>(legs.size());
      for (LegBean leg : legs) {
        LegV2Bean legBean = getLeg(leg);
        legBeans.add(legBean);
      }
      bean.setLegs(legBeans);
    }

    return bean;
  }

  public LegV2Bean getLeg(LegBean leg) {

    LegV2Bean bean = new LegV2Bean();

    bean.setStartTime(leg.getStartTime());
    bean.setEndTime(leg.getEndTime());
    bean.setDistance(leg.getDistance());
    bean.setMode(leg.getMode());

    TransitLegBean transitLeg = leg.getTransitLeg();
    if (transitLeg != null) {
      TransitLegV2Bean transitLegBean = getTransitLeg(transitLeg);
      bean.setTransitLeg(transitLegBean);
    }

    List<StreetLegBean> streetLegs = leg.getStreetLegs();
    if (!CollectionsLibrary.isEmpty(streetLegs)) {
      List<StreetLegV2Bean> streetLegBeans = new ArrayList<StreetLegV2Bean>();
      for (StreetLegBean streetLeg : streetLegs) {
        StreetLegV2Bean streetLegBean = getStreetLeg(streetLeg);
        streetLegBeans.add(streetLegBean);
      }
      bean.setStreetLegs(streetLegBeans);
    }

    return bean;
  }

  public TransitLegV2Bean getTransitLeg(TransitLegBean leg) {

    TransitLegV2Bean bean = new TransitLegV2Bean();

    TripBean trip = leg.getTrip();
    if (trip != null) {
      bean.setTripId(trip.getId());
      _factory.addToReferences(trip);
    }

    bean.setServiceDate(leg.getServiceDate());
    bean.setVehicleId(leg.getVehicleId());

    FrequencyBean frequency = leg.getFrequency();
    if (frequency != null) {
      FrequencyV2Bean freqBean = _factory.getFrequency(frequency);
      bean.setFrequency(freqBean);
    }

    StopBean fromStop = leg.getFromStop();
    if (fromStop != null) {
      bean.setFromStopId(fromStop.getId());
      bean.setFromStopSequence(leg.getFromStopSequence());
      _factory.addToReferences(fromStop);
    }

    bean.setScheduledDepartureTime(leg.getScheduledDepartureTime());
    bean.setPredictedDepartureTime(leg.getPredictedDepartureTime());

    StopBean toStop = leg.getToStop();
    if (toStop != null) {
      bean.setToStopId(toStop.getId());
      bean.setToStopSequence(leg.getToStopSequence());
      _factory.addToReferences(toStop);
    }

    bean.setScheduledArrivalTime(leg.getScheduledArrivalTime());
    bean.setPredictedArrivalTime(leg.getPredictedArrivalTime());

    bean.setRouteShortName(leg.getRouteShortName());
    bean.setRouteLongName(leg.getRouteLongName());
    bean.setTripHeadsign(leg.getTripHeadsign());
    bean.setPath(leg.getPath());
    
    List<SituationBean> situations = leg.getSituations();
    if (!CollectionsLibrary.isEmpty(situations)) {
      List<String> situationIds = new ArrayList<String>(situations.size());
      for (SituationBean situation : situations) {
        situationIds.add(situation.getId());
        _factory.addToReferences(situation);
      }
      bean.setSituationIds(situationIds);
    }

    return bean;
  }

  public StreetLegV2Bean getStreetLeg(StreetLegBean leg) {

    StreetLegV2Bean bean = new StreetLegV2Bean();

    bean.setStreetName(leg.getStreetName());

    bean.setPath(leg.getPath());
    bean.setDistance(leg.getDistance());

    return bean;
  }
}
