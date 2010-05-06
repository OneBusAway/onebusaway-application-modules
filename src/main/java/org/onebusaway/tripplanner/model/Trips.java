package org.onebusaway.tripplanner.model;

import edu.washington.cs.rse.collections.FactoryMap;

import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.tripplanner.impl.RouteKey;
import org.onebusaway.where.model.StopTimeInstance;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Trips {

  private static DateFormat _format = new SimpleDateFormat("hh:mm:ss aa");

  private Map<RouteKey, List<List<TripState>>> _trips = new FactoryMap<RouteKey, List<List<TripState>>>(
      new ArrayList<List<TripState>>());

  public int getSize() {
    return _trips.size();
  }

  public String getTripSegmentAsString(TripState state) {
    if (state instanceof VehicleDepartureState) {
      VehicleDepartureState vds = (VehicleDepartureState) state;
      StopTimeInstance sti = vds.getStopTimeInstance();
      StopTime st = sti.getStopTime();
      Trip trip = st.getTrip();
      Route route = trip.getRoute();
      Stop stop = st.getStop();
      return "Board Route # " + route.getShortName() + " at "
          + _format.format(sti.getDepartureTime()) + " at stop " + stop.getId();
    } else if (state instanceof VehicleArrivalState) {
      VehicleArrivalState vds = (VehicleArrivalState) state;
      StopTimeInstance sti = vds.getStopTimeInstance();
      StopTime st = sti.getStopTime();
      Trip trip = st.getTrip();
      Route route = trip.getRoute();
      Stop stop = st.getStop();
      return "Exit Route # " + route.getShortName() + " at "
          + _format.format(sti.getArrivalTime()) + " at stop " + stop.getId();
    } else if (state instanceof WalkToAnotherStopState) {
      WalkToAnotherStopState ws = (WalkToAnotherStopState) state;
      return "Walk from stop " + ws.getStop().getId();
    } else if (state instanceof BlockTransferState) {
      BlockTransferState bs = (BlockTransferState) state;
      Trip trip = bs.getNextTrip();
      Route route = trip.getRoute();
      return "Continues As Route # " + route.getShortName();
    } else if (state instanceof VehicleContinuationState) {
      VehicleContinuationState vcs = (VehicleContinuationState) state;
      StopTimeInstance sti = vcs.getStopTimeInstance();
      StopTime st = sti.getStopTime();
      Trip trip = st.getTrip();
      Route route = trip.getRoute();
      Stop stop = st.getStop();
      return "Continue on Route # " + route.getShortName() + " at "
          + _format.format(sti.getDepartureTime()) + " at stop " + stop.getId();
    } else if (state instanceof StartState) {
      return "start";
    } else if (state instanceof WaitingAtStopState) {
      WaitingAtStopState ws = (WaitingAtStopState) state;
      Stop stop = ws.getStop();
      return "waiting at " + _format.format(ws.getCurrentTime()) + " at stop "
          + stop.getId();
    } else if (state instanceof EndState) {
      return "end";
    }

    return "unknown";
  }

  public String getTripSegmentAsString2(TripState state) {
    if (state instanceof VehicleDepartureState) {
      VehicleDepartureState vds = (VehicleDepartureState) state;
      StopTimeInstance sti = vds.getStopTimeInstance();
      StopTime st = sti.getStopTime();
      Trip trip = st.getTrip();
      Route route = trip.getRoute();
      Stop stop = st.getStop();
      return "Board Route # " + route.getShortName() + " at "
          + _format.format(sti.getDepartureTime()) + " at stop " + stop.getId();
    } else if (state instanceof VehicleArrivalState) {
      VehicleArrivalState vds = (VehicleArrivalState) state;
      StopTimeInstance sti = vds.getStopTimeInstance();
      StopTime st = sti.getStopTime();
      Trip trip = st.getTrip();
      Route route = trip.getRoute();
      Stop stop = st.getStop();
      return "Exit Route # " + route.getShortName() + " at "
          + _format.format(sti.getArrivalTime()) + " at stop " + stop.getId();
    }

    return "";
  }

  public Map<RouteKey, List<List<TripState>>> getTrips() {
    return _trips;
  }

  public void addTrip(RouteKey key, List<TripState> trip) {
    _trips.get(key).add(trip);
  }
}
