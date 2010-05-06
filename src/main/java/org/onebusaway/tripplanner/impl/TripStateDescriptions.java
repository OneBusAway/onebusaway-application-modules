package org.onebusaway.tripplanner.impl;

import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.tripplanner.model.BlockTransferState;
import org.onebusaway.tripplanner.model.EndState;
import org.onebusaway.tripplanner.model.StartState;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.VehicleContinuationState;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.model.VehicleState;
import org.onebusaway.tripplanner.model.WaitingAtStopState;
import org.onebusaway.tripplanner.model.WalkFromStopState;
import org.onebusaway.tripplanner.model.WalkToStopState;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Point;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TripStateDescriptions {

  private static DateFormat _format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

  private GtfsDao _dao;

  private ProjectionService _projection;

  public TripStateDescriptions(GtfsDao dao, ProjectionService projection) {
    _dao = dao;
    _projection = projection;
  }

  public String getEncodedDescription(TripState state) {

    if (state instanceof StartState)
      return "state=start " + getTripStateEncodedDescription(state);

    if (state instanceof EndState)
      return "state=end " + getTripStateEncodedDescription(state);

    if (state instanceof BlockTransferState) {
      BlockTransferState bts = (BlockTransferState) state;
      return "state=blockTransfer " + getTripStateEncodedDescription(state) + " prevTrip=" + bts.getPrevTripId()
          + " nextTrip=" + bts.getNextTripId() + " serviceDate=" + bts.getServiceDate();
    }

    if (state instanceof WalkFromStopState) {
      WalkFromStopState wfss = (WalkFromStopState) state;
      return "state=walkFromStop " + getTripStateEncodedDescription(state) + " stop=" + wfss.getStopId();
    }

    if (state instanceof WalkToStopState) {
      WalkToStopState wfss = (WalkToStopState) state;
      return "state=walkToStop " + getTripStateEncodedDescription(state) + " stop=" + wfss.getStopId();
    }

    if (state instanceof WaitingAtStopState) {
      WaitingAtStopState wfss = (WaitingAtStopState) state;
      return "state=waitingAtStop " + getTripStateEncodedDescription(state) + " stop=" + wfss.getStopId();
    }

    if (state instanceof VehicleDepartureState) {
      VehicleDepartureState vds = (VehicleDepartureState) state;
      return "state=vehicleDeparture " + getTripStateEncodedDescription(state) + " "
          + getVehicleStateEncodedDescription(vds);
    }

    if (state instanceof VehicleArrivalState) {
      VehicleArrivalState vas = (VehicleArrivalState) state;
      return "state=vehicleArrival " + getTripStateEncodedDescription(state) + " "
          + getVehicleStateEncodedDescription(vas);
    }

    if (state instanceof VehicleContinuationState) {
      VehicleContinuationState vcs = (VehicleContinuationState) state;
      return "state=vehicleContinuation " + getTripStateEncodedDescription(state) + " "
          + getVehicleStateEncodedDescription(vcs);
    }

    throw new IllegalStateException();
  }

  private String getTripStateEncodedDescription(TripState state) {
    return "time=" + _format.format(state.getCurrentTime()) + " " + getLocationAsString(state.getLocation());
  }

  private String getVehicleStateEncodedDescription(VehicleState state) {
    Route route = _dao.getRouteById(state.getStopTimeInstance().getStopTime().getRouteId());
    return "stop=" + state.getStopId() + " route=" + route.getShortName();
  }

  private String getLocationAsString(Point location) {

    CoordinatePoint cp = _projection.getPointAsLatLong(location);

    StringBuilder b = new StringBuilder();
    b.append("x=").append(location.getX()).append(' ');
    b.append("y=").append(location.getY()).append(' ');
    b.append("lat=").append(cp.getLat()).append(' ');
    b.append("lon=").append(cp.getLon());

    return b.toString();
  }
}
