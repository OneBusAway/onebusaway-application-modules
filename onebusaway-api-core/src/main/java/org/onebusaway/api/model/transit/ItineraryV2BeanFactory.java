/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.api.model.transit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.api.model.transit.tripplanning.EdgeV2Bean;
import org.onebusaway.api.model.transit.tripplanning.GraphResultV2Bean;
import org.onebusaway.api.model.transit.tripplanning.ItinerariesV2Bean;
import org.onebusaway.api.model.transit.tripplanning.ItineraryV2Bean;
import org.onebusaway.api.model.transit.tripplanning.LegV2Bean;
import org.onebusaway.api.model.transit.tripplanning.LocationV2Bean;
import org.onebusaway.api.model.transit.tripplanning.StreetLegV2Bean;
import org.onebusaway.api.model.transit.tripplanning.TransitLegV2Bean;
import org.onebusaway.api.model.transit.tripplanning.VertexV2Bean;
import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.tripplanning.EdgeNarrativeBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.ItineraryBean;
import org.onebusaway.transit_data.model.tripplanning.LegBean;
import org.onebusaway.transit_data.model.tripplanning.LocationBean;
import org.onebusaway.transit_data.model.tripplanning.StreetLegBean;
import org.onebusaway.transit_data.model.tripplanning.TransitLegBean;
import org.onebusaway.transit_data.model.tripplanning.VertexBean;
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
    bean.setProbability(itinerary.getProbability());
    bean.setSelected(itinerary.isSelected());

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

  public ItineraryBean reverseItinerary(ItineraryV2Bean bean) {
    if (bean == null)
      return null;
    ItineraryBean itinerary = new ItineraryBean();
    itinerary.setStartTime(bean.getStartTime());
    itinerary.setEndTime(bean.getEndTime());
    itinerary.setProbability(bean.getProbability());
    List<LegV2Bean> legBeans = bean.getLegs();
    if (!CollectionsLibrary.isEmpty(legBeans)) {
      List<LegBean> legs = new ArrayList<LegBean>();
      for (LegV2Bean legBean : legBeans) {
        LegBean leg = reverseLeg(legBean);
        legs.add(leg);
      }
      itinerary.setLegs(legs);
    }
    return itinerary;
  }

  public LegV2Bean getLeg(LegBean leg) {

    LegV2Bean bean = new LegV2Bean();

    bean.setStartTime(leg.getStartTime());
    bean.setEndTime(leg.getEndTime());
    bean.setFrom(_factory.getPoint(leg.getFrom()));
    bean.setTo(_factory.getPoint(leg.getTo()));
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

  public LegBean reverseLeg(LegV2Bean bean) {

    LegBean leg = new LegBean();

    leg.setStartTime(bean.getStartTime());
    leg.setEndTime(bean.getEndTime());
    leg.setFrom(_factory.reversePoint(bean.getFrom()));
    leg.setTo(_factory.reversePoint(bean.getTo()));
    leg.setDistance(bean.getDistance());
    if (_factory.isStringSet(bean.getMode()))
      leg.setMode(bean.getMode());

    TransitLegV2Bean transitLegBean = bean.getTransitLeg();
    if (transitLegBean != null) {
      TransitLegBean transitLeg = reverseTransitLeg(transitLegBean);
      leg.setTransitLeg(transitLeg);
    }

    List<StreetLegV2Bean> streetLegBeans = bean.getStreetLegs();
    if (!CollectionsLibrary.isEmpty(streetLegBeans)) {
      List<StreetLegBean> streetLegs = new ArrayList<StreetLegBean>();
      for (StreetLegV2Bean streetLegBean : streetLegBeans) {
        StreetLegBean streetLeg = reverseStreetLeg(streetLegBean);
        streetLegs.add(streetLeg);
      }
      leg.setStreetLegs(streetLegs);
    }

    return leg;
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
    bean.setScheduledDepartureInterval(_factory.getTimeInterval(leg.getScheduledDepartureInterval()));
    bean.setPredictedDepartureInterval(_factory.getTimeInterval(leg.getPredictedDepartureInterval()));

    StopBean toStop = leg.getToStop();
    if (toStop != null) {
      bean.setToStopId(toStop.getId());
      bean.setToStopSequence(leg.getToStopSequence());
      _factory.addToReferences(toStop);
    }

    bean.setScheduledArrivalTime(leg.getScheduledArrivalTime());
    bean.setPredictedArrivalTime(leg.getPredictedArrivalTime());
    bean.setScheduledArrivalInterval(_factory.getTimeInterval(leg.getScheduledArrivalInterval()));
    bean.setPredictedArrivalInterval(_factory.getTimeInterval(leg.getPredictedArrivalInterval()));

    bean.setRouteShortName(leg.getRouteShortName());
    bean.setRouteLongName(leg.getRouteLongName());
    bean.setTripHeadsign(leg.getTripHeadsign());
    bean.setPath(leg.getPath());

    List<ServiceAlertBean> situations = leg.getSituations();
    if (!CollectionsLibrary.isEmpty(situations)) {
      List<String> situationIds = new ArrayList<String>(situations.size());
      for (ServiceAlertBean situation : situations) {
        situationIds.add(situation.getId());
        _factory.addToReferences(situation);
      }
      bean.setSituationIds(situationIds);
    }

    return bean;
  }

  private TransitLegBean reverseTransitLeg(TransitLegV2Bean leg) {

    TransitLegBean bean = new TransitLegBean();

    String tripId = leg.getTripId();
    if (tripId != null && !tripId.isEmpty()) {
      TripBean trip = new TripBean();
      trip.setId(tripId);
      bean.setTrip(trip);
    }

    bean.setServiceDate(leg.getServiceDate());
    if (_factory.isStringSet(leg.getVehicleId()))
      bean.setVehicleId(leg.getVehicleId());

    FrequencyV2Bean frequency = leg.getFrequency();
    if (frequency != null) {
      FrequencyBean freqBean = _factory.reverseFrequency(frequency);
      bean.setFrequency(freqBean);
    }

    String fromStopId = leg.getFromStopId();
    if (_factory.isStringSet(fromStopId)) {
      StopBean stop = new StopBean();
      stop.setId(fromStopId);
      bean.setFromStop(stop);
      bean.setFromStopSequence(leg.getFromStopSequence());
    }

    bean.setScheduledDepartureTime(leg.getScheduledDepartureTime());

    String toStopId = leg.getToStopId();
    if (_factory.isStringSet(toStopId)) {
      StopBean stop = new StopBean();
      stop.setId(toStopId);
      bean.setToStop(stop);
      bean.setToStopSequence(leg.getToStopSequence());
    }

    bean.setScheduledArrivalTime(leg.getScheduledArrivalTime());

    return bean;
  }

  public StreetLegV2Bean getStreetLeg(StreetLegBean leg) {

    StreetLegV2Bean bean = new StreetLegV2Bean();

    bean.setStreetName(leg.getStreetName());

    bean.setPath(leg.getPath());
    bean.setDistance(leg.getDistance());

    return bean;
  }

  public StreetLegBean reverseStreetLeg(StreetLegV2Bean leg) {

    StreetLegBean bean = new StreetLegBean();

    if (_factory.isStringSet(leg.getStreetName()))
      bean.setStreetName(leg.getStreetName());

    if (_factory.isStringSet(leg.getPath()))
      bean.setPath(leg.getPath());
    bean.setDistance(leg.getDistance());

    return bean;
  }

  public GraphResultV2Bean getGraphResult(List<VertexBean> list) {

    GraphResultV2Bean result = new GraphResultV2Bean();

    List<VertexV2Bean> vertices = new ArrayList<VertexV2Bean>();

    for (VertexBean vertex : list) {
      VertexV2Bean bean = new VertexV2Bean();
      bean.setId(vertex.getId());
      bean.setLocation(vertex.getLocation());

      Map<String, String> tags = getTags(vertex.getTags());

      bean.setTags(tags);
      vertices.add(bean);
    }

    if (!vertices.isEmpty())
      result.setVertices(vertices);

    List<EdgeV2Bean> edges = new ArrayList<EdgeV2Bean>();

    for (VertexBean vertex : list) {
      List<EdgeNarrativeBean> out = vertex.getOutgoing();
      if (out != null) {
        for (EdgeNarrativeBean narrative : out) {
          EdgeV2Bean bean = new EdgeV2Bean();
          bean.setFromId(narrative.getFrom().getId());
          bean.setToId(narrative.getTo().getId());
          bean.setName(narrative.getName());
          bean.setPath(narrative.getPath());
          Map<String, String> tags = getTags(narrative.getTags());
          bean.setTags(tags);

          edges.add(bean);
        }
      }
    }

    if (!edges.isEmpty())
      result.setEdges(edges);

    return result;
  }

  private Map<String, String> getTags(Map<String, Object> tags) {

    Map<String, String> tagBeans = new HashMap<String, String>();

    if (tags == null)
      return null;

    for (Map.Entry<String, Object> entry : tags.entrySet()) {
      String key = entry.getKey();
      String value = getValueAsString(entry.getValue());
      tagBeans.put(key, value);
    }
    return tagBeans;
  }

  private String getValueAsString(Object value) {

    if (value instanceof StopBean) {
      StopBean stop = (StopBean) value;
      _factory.addToReferences(stop);
      return stop.getId();
    } else {
      return value.toString();
    }
  }

}
