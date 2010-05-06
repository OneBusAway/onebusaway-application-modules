package org.onebusaway.transit_data_federation.services;

import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.model.StopSequence;

public interface StopSequencesService {

  public List<StopSequence> getStopSequencesForTrips(
      Map<Trip, List<StopTime>> stopTimesByTrip);

}