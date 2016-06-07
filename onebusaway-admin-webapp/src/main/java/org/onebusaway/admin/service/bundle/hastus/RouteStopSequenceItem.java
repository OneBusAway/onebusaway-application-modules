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
package org.onebusaway.admin.service.bundle.hastus;

public class RouteStopSequenceItem {

  private long sequenceArc;

  private long sequenceArcId;

  private long sequence;

  private double length;

  private String route;

  private String routeDirection;

  private String routeDirectionAlternate;

  private String schedule;

  private String routeVariation;

  private long stopId;

  private String timePoint;

  private Object geometry;
  
  private String boarding = null;

  public long getSequenceArc() {
    return sequenceArc;
  }

  public void setSequenceArc(long sequenceArc) {
    this.sequenceArc = sequenceArc;
  }

  public long getSequenceArcId() {
    return sequenceArcId;
  }

  public void setSequenceArcId(long sequenceArcId) {
    this.sequenceArcId = sequenceArcId;
  }

  public long getSequence() {
    return sequence;
  }

  public void setSequence(long sequence) {
    this.sequence = sequence;
  }

  public double getLength() {
    return length;
  }

  public void setLength(double length) {
    this.length = length;
  }

  public String getRoute() {
    return route;
  }

  public void setRoute(String route) {
    this.route = route;
  }

  public String getRouteDirection() {
    return routeDirection;
  }

  public void setRouteDirection(String routeDirection) {
    this.routeDirection = routeDirection;
  }

  public String getRouteDirectionAlternate() {
    return routeDirectionAlternate;
  }

  public void setRouteDirectionAlternate(String routeDirectionAlternate) {
    this.routeDirectionAlternate = routeDirectionAlternate;
  }

  public String getSchedule() {
    return schedule;
  }

  public void setSchedule(String schedule) {
    this.schedule = schedule;
  }

  public String getRouteVariation() {
    return routeVariation;
  }

  public void setRouteVariation(String routeVariation) {
    this.routeVariation = routeVariation;
  }

  public long getStopId() {
    return stopId;
  }

  public void setStopId(long stopId) {
    this.stopId = stopId;
  }

  public String getTimePoint() {
    return timePoint;
  }

  public void setTimePoint(String timePoint) {
    this.timePoint = timePoint;
  }

  public Object getGeometry() {
    return geometry;
  }

  public void setGeometry(Object geometry) {
    this.geometry = geometry;
  }

  public String getBoarding() {
    return boarding;
  }
  
  public void setBoarding(String boarding) {
    this.boarding = boarding;
  }
  @Override
  public String toString() {
    return length + "\t" + sequence + "\t" + stopId + "\t" + timePoint;
  }

}
