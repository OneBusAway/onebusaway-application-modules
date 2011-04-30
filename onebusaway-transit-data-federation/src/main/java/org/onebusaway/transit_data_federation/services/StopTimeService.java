package org.onebusaway.transit_data_federation.services;

import java.util.Date;
import java.util.List;

import org.onebusaway.collections.Range;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockSequence;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

/**
 * Service methods for determining the set of active stop times at a particular
 * stop and time.
 * 
 * @author bdferris
 * @see StopTimeInstance
 */
public interface StopTimeService {

  /**
   * Determines the set of active stop time instances at a given stop, taking
   * into account information like active service dates, etc
   * 
   * @param stopId the starget stop id
   * @param from
   * @param to
   * @return the set of active stop time instances in the specified time range
   */
  public List<StopTimeInstance> getStopTimeInstancesInTimeRange(
      AgencyAndId stopId, Date from, Date to);

  /**
   * Determines the set of active stop time instances at a given stop, taking
   * into account information like active service dates, etc
   * 
   * @param stopEntry
   * @param from
   * @param to
   * @param frequencyBehavior how to handle frequency-based stop times
   * @return the set of active stop time instances in the specified time range
   */
  public List<StopTimeInstance> getStopTimeInstancesInTimeRange(
      StopEntry stopEntry, Date from, Date to,
      EFrequencyStopTimeBehavior frequencyBehavior);

  public Range getDepartureForStopAndServiceDate(AgencyAndId stopId,
      ServiceDate serviceDate);

  /**
   * Given the set of {@link BlockSequence} sequences incident on a particular
   * stop, compute the next departure for each sequence at or after the
   * specified time.
   * 
   * @param stop
   * @param time
   * @return
   */
  public List<StopTimeInstance> getNextBlockSequenceDeparturesForStop(
      StopEntry stop, long time);

  public List<Pair<StopTimeInstance>> getNextDeparturesBetweenStopPair(
      StopEntry fromStop, StopEntry toStop, Date fromTime, int lookBehind,
      int lookAhead, int resultCount);

  public List<Pair<StopTimeInstance>> getPreviousArrivalsBetweenStopPair(
      StopEntry fromStop, StopEntry toStop, Date toTime, int lookBehind,
      int lookAhead, int resultCount);

  /**
   * When calculating frequency-based stop times, we have a couple different
   * options when return results
   */
  public enum EFrequencyStopTimeBehavior {

    /**
     * Include a stop time with the frequency information set but no frequency
     * offset set.
     */
    INCLUDE_UNSPECIFIED,

    /**
     * Interpolate out all the stop-times in a frequency interval, including
     * both frequency information AND a frequency offset so that the stop time
     * has an actual arrival and departure time set. An interpolated stop time
     * that would be included in the time interval will be included.
     */
    INCLUDE_INTERPOLATED
  }
}
