package org.onebusaway.transit_data_federation.services.transit_graph;

public interface FrequencyBlockStopTimeEntry {

  public BlockStopTimeEntry getStopTime();

  public FrequencyEntry getFrequency();

  /**
   * If you were to extrapolate out a schedule for all the frequency-based stop
   * times for a given frequency-based trip, we'd start with the first stop time
   * at {@link FrequencyEntry#getStartTime()} and then schedule the successive
   * stops relative to their change in arrival+departure time in the underlying
   * stop time. Repeating the scheduled at the specified frequency, each stop
   * time at a particular stop will occur every n seconds, as determined by the
   * headway. Each stop time at the same stop will also be consistently offset
   * from the {@link FrequencyEntry#getStartTime()} by the same amount, modulo
   * the headway. This method returns that offset.
   * 
   * As if that explanation is clear at all...
   * 
   * @return
   */
  public int getStopTimeOffset();
}
