package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;

public class BlockIndex {

  private final List<BlockEntry> _blocks;

  private final ServiceIdIntervals _serviceIdIntervals;

  private final Map<LocalizedServiceId, ServiceIntervalBlock> _intervalsByServiceId;

  public BlockIndex(List<BlockEntry> blocks,
      ServiceIdIntervals serviceIdIntervals,
      Map<LocalizedServiceId, ServiceIntervalBlock> intervalsByServiceId) {
    _blocks = blocks;
    _serviceIdIntervals = serviceIdIntervals;
    _intervalsByServiceId = intervalsByServiceId;
  }

  public ServiceIdIntervals getServiceIdIntervals() {
    return _serviceIdIntervals;
  }
  
  public Map<LocalizedServiceId, ServiceIntervalBlock> getIntervalsByServiceId() {
    return _intervalsByServiceId;
  }

  public List<BlockEntry> getBlocks() {
    return _blocks;
  }

  public ServiceIntervalBlock getIntervalForServiceId(
      LocalizedServiceId serviceId) {
    return _intervalsByServiceId.get(serviceId);
  }
}
