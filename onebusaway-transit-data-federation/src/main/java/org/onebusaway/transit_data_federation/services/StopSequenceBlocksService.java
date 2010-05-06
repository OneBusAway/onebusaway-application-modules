package org.onebusaway.transit_data_federation.services;

import java.util.List;

import org.onebusaway.transit_data_federation.model.StopSequence;
import org.onebusaway.transit_data_federation.model.StopSequenceBlock;

public interface StopSequenceBlocksService {

  public List<StopSequenceBlock> getStopSequencesAsBlocks(
      List<StopSequence> sequences);

}