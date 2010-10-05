package org.onebusaway.transit_data_federation.services;

import java.util.List;

import org.onebusaway.transit_data_federation.model.StopSequence;
import org.onebusaway.transit_data_federation.model.StopSequenceCollection;

/**
 * Service method for grouping a set of {@link StopSequence} objects into
 * {@link StopSequenceCollection} objects. Recall that a route typically has a
 * set of unique stop sequences visited by the various trips serving that route.
 * Here we group those stop sequences into collections, typically using
 * direction of travel as the main grouping strategy.
 * 
 * @author bdferris
 * 
 */
public interface StopSequenceCollectionService {

  /**
   * Group a set of stop sequence objects into a smaller set of stop sequence
   * collections, typically grouping the stop sequences by direction of travel
   * for the parent route.
   * 
   * @param sequences the set of stop sequences
   * @return the collection of stop sequence collection groups
   */
  public List<StopSequenceCollection> getStopSequencesAsCollections(
      List<StopSequence> sequences);
}