package org.onebusaway.transit_data_federation.bundle;

import java.util.Arrays;
import java.util.List;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;

public class FederatedTransitDataBundleCreatorTasks {

  /**
   * Process GTFS and load it into the database
   */
  public static final String GTFS = "GTFS";

  /**
   * Consolidate multiple {@link Route} instances that all refer to the same
   * semantic route into {@link RouteCollection} objects
   */
  public static final String ROUTE_COLLECTIONS = "ROUTE_COLLECTIONS";

  /**
   * Generate the search index from {@link RouteCollection} short and long name
   * to route instance
   */
  public static final String ROUTE_SEARCH_INDEX = "ROUTE_SEARCH_INDEX";

  /**
   * Generate the search index from {@link Stop} name and code to stop instance
   */
  public static final String STOP_SEARCH_INDEX = "STOP_SEARCH_INDEX";

  /**
   * Compile {@link ServiceCalendar} and {@link ServiceCalendarDate} information
   * into an optimized {@link CalendarServiceData} data structure
   */
  public static final String CALENDAR_SERVICE = "CALENDAR_SERVICE";

  /**
   * Construct the walk planner graph
   */
  public static final String WALK_GRAPH = "WALK_GRAPH";

  /**
   * Construct the trip planner graph
   */
  public static final String TRIP_GRAPH = "TRIP_GRAPH";

  /**
   * Construct the optimized set of transfer points in the transit graph
   */
  public static final String STOP_TRANSFERS = "STOP_TRANSFERS";

  /**
   * Construct the set of {@link StopNarrative}, {@link TripNarrative}, and
   * {@link StopTimeNarrative} objects.
   */
  public static final String NARRATIVES = "NARRATIVES";

  /**
   * Pre-cache many of the expensive to construct responses
   */
  public static final String PRE_CACHE = "PRE_CACHE";

  public static List<String> getDefaultStages() {
    return Arrays.asList(GTFS, ROUTE_COLLECTIONS, ROUTE_SEARCH_INDEX,
        STOP_SEARCH_INDEX, CALENDAR_SERVICE, WALK_GRAPH, TRIP_GRAPH,
        STOP_TRANSFERS, NARRATIVES, PRE_CACHE);
  }
}
