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
package org.onebusaway.transit_data_federation.impl.realtime.orbcad;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;

@CsvFields(filename = "records.csv", fieldOrder = {
    "vehicle_id", "route_id", "direction", "block", "service_type",
    "schedule_deviation", "timepoint_arrival_time", "timepoint_id",
    "timepoint_time", "lat", "lon", "time", "logon_route_id", "block_number",
    "off_route", "run_id", "next_sign_timepoint_id", "trip_id"})
public class OrbcadRecord {

  private static final double LAT_MISSING_VALUE = 99.000000;

  private static final double LON_MISSING_VALUE = 999.000000;

  private static final int SCHEDULE_DEVIATION_MISSING_VALUE = 99999;

  private static final int TIMEPOINT_TIME_MISSING_VALUE = 9999;

  private static final int DIRECTION_MISSING_VALUE = 99;

  private static final int SERVICE_TYPE_MISSING_VALUE = 99;

  private int vehicleId;

  private int routeId;

  /**
   * If Direction_Code_ID_Type is set to 0 in the OrbCAD server registry, Route
   * Direction will be in the following format. 0: East, South, Clockwise; 1:
   * West, North, Counterclockwise
   * 
   * If Direction_Code_ID_Type is set to 1 in the OrbCAD server registry, Route
   * Direction will be the corresponding direction_code_id from direction_codes
   * table for each direction. Valid range is 1 - Last direction code ID entry
   * from the direction_codes table in the OrbCAD database.
   * 
   * The direction is not valid when Route ID is set to 0. A value of 99
   * indicates that the direction was unavailable. As default in the database,
   * Direction Code ID of 0 is not used.
   */
  private int direction = DIRECTION_MISSING_VALUE;

  /**
   * Identifies the work assigned to a bus (maximum of 6 digits). If this value
   * is unknown, this field is set to 999999.
   */
  private int block;

  private int serviceType = SERVICE_TYPE_MISSING_VALUE;

  /**
   * Vehicle's schedule deviation, in seconds early [+XXXXX] or late [-XXXXX].
   * Schedule deviation resolution is in minutes not seconds. If schedule
   * deviation is unknown (due to non-revenue service, Off-route, Logon, No
   * Show, No Response, Deahead, NoResponse, RSA Disable, etc), this field is
   * set to 99999.
   */
  private int scheduleDeviation = SCHEDULE_DEVIATION_MISSING_VALUE;

  /**
   * The estimated time in minutes past midnight that the vehicle will arrive at
   * its next timepoint, as defined in the scheduling system. If this value is
   * unknown (due to non-revenue service, Off-route, Logon, No Show, No
   * Response, Deahead, NoResponse, RSA Disable, etc), this field is set to
   * 9999.
   */
  private int timepointArrivalTime = TIMEPOINT_TIME_MISSING_VALUE;

  /**
   * The next timepoint location the vehicle will arrive at as defined in the
   * scheduling system. If this value is unknown, the field is set to null.
   */
  @CsvField(optional = true)
  private String timepointId;

  /**
   * The time, in minutes past midnight, which the vehicle is scheduled to
   * arrive at the next scheduling system timepoint. Schedule refers to the
   * current active schedule to which the vehicle is adhering. If this value is
   * unknown, this field is set to 9999.
   */
  private int timepointTime = TIMEPOINT_TIME_MISSING_VALUE;

  private double lat = LAT_MISSING_VALUE;

  private double lon = LON_MISSING_VALUE;

  /**
   * Time of receipt of the last location report from the vehicle. Time is in
   * seconds since 00:00:00 GMT January 1, 1970
   */
  private long time;

  private int logonRouteId;

  private int blockNumber;

  private int offRoute;

  private int runId;
  
  @CsvField(optional = true)
  private int tripId;

  @CsvField(optional = true)
  private String nextSignTimepointId;

  public int getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(int vehicleId) {
    this.vehicleId = vehicleId;
  }

  public int getRouteId() {
    return routeId;
  }

  public void setRouteId(int routeId) {
    this.routeId = routeId;
  }

  public boolean hasDirection() {
    return direction != DIRECTION_MISSING_VALUE;
  }

  public int getDirection() {
    return direction;
  }

  public void setDirection(int direction) {
    this.direction = direction;
  }

  public int getBlock() {
    return block;
  }

  public void setBlock(int block) {
    this.block = block;
  }

  public boolean hasServiceType() {
    return serviceType != SERVICE_TYPE_MISSING_VALUE;
  }

  public int getServiceType() {
    return serviceType;
  }

  public void setServiceType(int serviceType) {
    this.serviceType = serviceType;
  }

  public boolean hasScheduleDeviation() {
    return scheduleDeviation != SCHEDULE_DEVIATION_MISSING_VALUE;
  }

  /**
   * Vehicle's schedule deviation, in seconds early [+XXXXX] or late [-XXXXX].
   * Schedule deviation resolution is in minutes not seconds. If schedule
   * deviation is unknown (due to non-revenue service, Off-route, Logon, No
   * Show, No Response, Deahead, NoResponse, RSA Disable, etc), this field is
   * set to 99999.
   * 
   * @return schedule deviation in seconds, where positive is early and negative
   *         is late
   */
  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(int scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public boolean hasTimepointArrivalTime() {
    return timepointArrivalTime != TIMEPOINT_TIME_MISSING_VALUE;
  }

  public int getTimepointArrivalTime() {
    return timepointArrivalTime;
  }

  public void setTimepointArrivalTime(int timepointArrivalTime) {
    this.timepointArrivalTime = timepointArrivalTime;
  }

  public String getTimepointId() {
    return timepointId;
  }

  public void setTimepointId(String timepointId) {
    this.timepointId = timepointId;
  }

  public boolean hasTimepointTime() {
    return timepointTime != TIMEPOINT_TIME_MISSING_VALUE;
  }

  /**
   * @return the time, in minutes past midnight, which the vehicle is scheduled
   *         to arrive at the next scheduling system timepoint
   */
  public int getTimepointTime() {
    return timepointTime;
  }

  /**
   * @param timepointTime - the time, in minutes past midnight, which the
   *          vehicle is scheduled to arrive at the next scheduling system
   *          timepoint
   */
  public void setTimepointTime(int timepointTime) {
    this.timepointTime = timepointTime;
  }

  public boolean hasLat() {
    return lat != LAT_MISSING_VALUE;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public boolean hasLon() {
    return lon != LON_MISSING_VALUE;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  /**
   * Time of receipt of the last location report from the vehicle.
   * 
   * @return time in seconds since 00:00:00 GMT January 1, 1970
   */
  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public int getLogonRouteId() {
    return logonRouteId;
  }

  public void setLogonRouteId(int logonRouteId) {
    this.logonRouteId = logonRouteId;
  }

  public int getBlockNumber() {
    return blockNumber;
  }

  public void setBlockNumber(int blockNumber) {
    this.blockNumber = blockNumber;
  }

  public int getOffRoute() {
    return offRoute;
  }

  public void setOffRoute(int offRoute) {
    this.offRoute = offRoute;
  }

  public int getRunId() {
    return runId;
  }

  public void setRunId(int runId) {
    this.runId = runId;
  }

  public String getNextSignTimepointId() {
    return nextSignTimepointId;
  }

  public void setNextSignTimepointId(String nextSignTimepointId) {
    this.nextSignTimepointId = nextSignTimepointId;
  }
  
  public int getTripId() {
    return tripId;
  }
  
  public void setTripId(int tripId) {
    this.tripId = tripId;
  }
}
