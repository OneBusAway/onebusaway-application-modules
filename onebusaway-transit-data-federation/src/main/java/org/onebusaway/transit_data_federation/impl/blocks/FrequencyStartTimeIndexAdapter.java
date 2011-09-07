package org.onebusaway.transit_data_federation.impl.blocks;

import org.onebusaway.transit_data_federation.impl.blocks.GenericBinarySearch.IndexAdapter;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;

public class FrequencyStartTimeIndexAdapter implements
    IndexAdapter<FrequencyBlockStopTimeIndex> {

  public static final FrequencyStartTimeIndexAdapter INSTANCE = new FrequencyStartTimeIndexAdapter();

  @Override
  public double getValue(FrequencyBlockStopTimeIndex source, int index) {
    return source.getStartTimeForIndex(index);
  }

}
