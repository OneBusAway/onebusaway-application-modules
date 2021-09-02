/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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
package org.onebusaway.watchdog.api.realtime;

import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.watchdog.api.MetricResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * monitor vehicle collisions (assignment grabs) on a block.
 */
@Path("/metric/realtime/conflicts")
public class AssignmentConflictResource extends MetricResource {

  protected static final Logger _log = LoggerFactory.getLogger(AssignmentConflictResource.class);
  private ScheduledExecutorService _scheduledExecutorService;
  private ScheduledFuture<?> _refreshTask;
  private int _refreshInterval = 30;
  private Map<String, VehicleHistory> vehicleHistoryMap = new HashMap();
  private Map<String, ActiveVehicles> vehiclesByTrip = new HashMap<>();
  private Map<String, VehicleHistory> tripConflicts = new HashMap<>();
  private Map<String, VehicleHistory> blockConflicts = new HashMap<>();
  private Map<String, ActiveVehicles> vehicleConflicts = new HashMap<>();


  @Autowired
  public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
    _scheduledExecutorService = scheduledExecutorService;
  }

  @Path("trip/list")
  @GET
  @Produces("application/json")
  public Response getAllTripConflicts() {
    Map<String, VehicleHistory> trips = new HashMap<>(tripConflicts);
    return Response.ok(ok("trip-conflicts-list", printVehicleHistory(trips))).build();
  }

  @Path("block/list")
  @GET
  @Produces("application/json")
  public Response getAllBlockConflicts() {
    Map<String, VehicleHistory> blocks = new HashMap<>(blockConflicts);
    return Response.ok(ok("block-conflicts-list", printVehicleHistory(blocks))).build();
  }

  @Path("vehicle/list")
  @GET
  @Produces("application/json")
  public Response getAllVehicleConflicts() {
    Map<String, ActiveVehicles> vehiclesMap = new HashMap<>(vehicleConflicts);
    return Response.ok(ok("vehicle-conflicts-list", printActiveVehicle(vehiclesMap))).build();
  }

  public synchronized void refresh() {
    long now = System.currentTimeMillis();
    for (AgencyWithCoverageBean ab : getTDS().getAgenciesWithCoverage()) {

      ListBean<VehicleStatusBean> allVehiclesForAgency = getTDS().getAllVehiclesForAgency(ab.getAgency().getId(), now);
      for (VehicleStatusBean vsb : allVehiclesForAgency.getList()) {
        if (vsb.getTrip() != null)
          addVehicle(vsb.getVehicleId(), vsb.getTrip().getId(), vsb.getTrip().getBlockId());
      }
    }
    this.tripConflicts.clear();
    this.blockConflicts.clear();

    for (String vehicleId : this.vehicleHistoryMap.keySet()) {
      VehicleHistory vh = this.vehicleHistoryMap.get(vehicleId);
      if (vh.hasTripConflict()) {
        _log.debug("tripConflict " + vh);
        this.tripConflicts.put(vehicleId, vh);
      }
      if (vh.hasBlockConflict()) {
        _log.debug("blockConflict " + vh);
        this.blockConflicts.put(vehicleId, vh);
      }
    }

    this.vehicleConflicts.clear();

    int vcount = 0;
    for (String tripId : this.vehiclesByTrip.keySet()) {
      ActiveVehicles av = this.vehiclesByTrip.get(tripId);
      if (av.hasConflict()) {
        vcount++;
        _log.debug("vehicle conflict " + av + " for trip " + tripId);
        this.vehicleConflicts.put(tripId, av);
      }
    }
    if (vcount > 0) {
      _log.debug("" + vcount + " conflicts this run");
    }
  }

  private void addVehicle(String vehicleId, String tripId, String blockId) {
    if (vehicleHistoryMap.containsKey(vehicleId)) {
      vehicleHistoryMap.get(vehicleId).add(vehicleId, tripId, blockId);
    } else {
      VehicleHistory vh = new VehicleHistory(vehicleId, tripId, blockId);
      vehicleHistoryMap.put(vehicleId, vh);
    }
    if (vehiclesByTrip.containsKey(tripId)) {
      vehiclesByTrip.get(tripId).add(vehicleId);
    } else {
      ActiveVehicles av = new ActiveVehicles(vehicleId);
      vehiclesByTrip.put(tripId, av);
    }
  }

  private String printVehicleHistory(Map<String, VehicleHistory> map) {
    StringBuffer sb = new StringBuffer();
    sb.append("map = {");
    if (map == null) return sb.toString();
    int i = 0;
    for (String key : map.keySet()) {
      if (i > 0) sb.append(",");
      i++;
      VehicleHistory vh = map.get(key);
      sb.append(key).append('=').append(vh.toString());
    }
    sb.append("}");
    return sb.toString();
  }


  private String printActiveVehicle(Map<String, ActiveVehicles> map) {
    StringBuffer sb = new StringBuffer();
    sb.append("map = {");
    if (map == null) return sb.toString();
    int i = 0;
    for (String key : map.keySet()) {
      if (i > 0) sb.append(",");
      i++;
      ActiveVehicles av = map.get(key);
      sb.append(key).append('=').append(av.toString());
    }
    sb.append("}");
    return sb.toString();

  }



  @PostConstruct
  public void start() {
    _refreshTask = _scheduledExecutorService.scheduleAtFixedRate(new RefreshTask(), 0, _refreshInterval, TimeUnit.SECONDS);
  }

  @PreDestroy
  public void stop() {
    if (_refreshTask != null) {
      _refreshTask.cancel(true);
      _refreshTask = null;
    }
    if (_scheduledExecutorService != null) {
      _scheduledExecutorService.shutdown();
    }
  }

  private class RefreshTask implements Runnable {
    @Override
    public void run() {
      try {
        refresh();
      } catch (Throwable ex) {
        _log.warn("Error updating block info", ex);
      }
    }
  }

  private static class VehicleHistory {
      private ArrayList<Tupple> queue = new ArrayList<>(2);


    public VehicleHistory(String vehicleId, String tripId, String blockId) {
      add(vehicleId, tripId, blockId);
    }

    public void add(String vehicleId, String tripId, String blockId) {
      // circular queue of two elements -- this update, and last update
      if (queue.size() > 2) queue.remove(0);
      queue.add(new Tupple(vehicleId, tripId, blockId));
    }


    public boolean hasTripConflict() {
      if (queue.size() < 2) return false;
      return hasTripConflict(queue.get(0), queue.get(1));
    }

    private boolean hasTripConflict(Tupple a, Tupple b) {
      if (a.vehicleId.equals(b.vehicleId)) {
        if (!a.tripId.equals(b.tripId)) {
          // trip conflict
          return true;
        }
    }
      return false;
  }

    public boolean hasBlockConflict() {
      if (queue.size() < 2) return false;
      return hasBlockConflict(queue.get(0), queue.get(1));
    }

    private boolean hasBlockConflict(Tupple a, Tupple b) {
      if (a.vehicleId.equals(b.vehicleId)) {
        if (!a.blockId.equals(b.blockId)) {
          // block conflict
          return true;
        }
      }
      return false;
    }

    @Override
    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("[");

      if (queue.size() > 0)
        sb.append(queue.get(0).toString());
      if (queue.size() > 1)
        sb.append(queue.get(1).toString());


      sb.append("]");
      return sb.toString();
    }

  }

  private static class Tupple {
    String vehicleId;
    String tripId;
    String blockId;
    public Tupple(String vehicleId, String tripId, String blockId) {
      this.vehicleId = vehicleId;
      this.tripId = tripId;
      this.blockId = blockId;
    }

    @Override
    public String toString() {
      return "{"
              + "vehicleId=" + vehicleId
              + ", tripId=" + tripId
              + ", blockId=" + blockId
              + "}";
    }
  }

  private static class ActiveVehicles {
    private ArrayList<String> vehicles = new ArrayList<>(2);

    public ActiveVehicles(String vehicleId) {
      vehicles.add(vehicleId);
    }

    public void add(String vehicleId) {
      if (vehicles.size() > 2) vehicles.remove(0);
      vehicles.add(vehicleId);
    }

    public boolean hasConflict() {
      if (vehicles.size() < 2)
        return false;
        if (!vehicles.get(0).equals(vehicles.get(1)))
          return true;
        return false;
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("{");
      if (vehicles.size() > 0)
        sb.append(vehicles.get(0));
      if (vehicles.size() > 1)
        sb.append(", ").append(vehicles.get(1));
      sb.append("}");
      return sb.toString();
    }
  }
}

