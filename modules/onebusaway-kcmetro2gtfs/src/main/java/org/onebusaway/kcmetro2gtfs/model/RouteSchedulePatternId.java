/**
 * 
 */
package org.onebusaway.kcmetro2gtfs.model;

public final class RouteSchedulePatternId {

  private String route;

  private int schedulePattern;

  public RouteSchedulePatternId() {

  }

  public RouteSchedulePatternId(String route, int schedulePattern) {
    this.route = route;
    this.schedulePattern = schedulePattern;
  }

  public String getRoute() {
    return route;
  }

  public void setRoute(String route) {
    this.route = route;
  }

  public int getSchedulePattern() {
    return schedulePattern;
  }

  public void setSchedulePattern(int schedulePattern) {
    this.schedulePattern = schedulePattern;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RouteSchedulePatternId))
      return false;
    RouteSchedulePatternId id = (RouteSchedulePatternId) obj;
    return route.equals(id.route) && schedulePattern == id.schedulePattern;
  }

  @Override
  public int hashCode() {
    return 7 * route.hashCode() + 13 * schedulePattern;
  }

  @Override
  public String toString() {
    return "id(route=" + route + " schedulePattern=" + schedulePattern + ")";
  }
}