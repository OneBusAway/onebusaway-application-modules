package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;

public class BlockIndexData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final List<AgencyAndId> _blockIds;

  private final ServiceIdIntervals _serviceIdIntervals;

  private final Map<LocalizedServiceId, ServiceIntervalBlock> _intervalsByServiceId;

  public BlockIndexData(List<AgencyAndId> blockIds,
      ServiceIdIntervals serviceIdIntervals,
      Map<LocalizedServiceId, ServiceIntervalBlock> intervalsByServiceId) {
    _blockIds = blockIds;
    _serviceIdIntervals = serviceIdIntervals;
    _intervalsByServiceId = intervalsByServiceId;
  }

  public List<AgencyAndId> getBlockIds() {
    return _blockIds;
  }

  public ServiceIdIntervals getServiceIdIntervals() {
    return _serviceIdIntervals;
  }

  public Map<LocalizedServiceId, ServiceIntervalBlock> getIntervalsByServiceId() {
    return _intervalsByServiceId;
  }

  public BlockIndex createIndex(TransitGraphDao dao) {
    List<BlockEntry> blocks = new ArrayList<BlockEntry>();
    for (AgencyAndId blockId : _blockIds) {
      BlockEntry block = dao.getBlockEntryForId(blockId);
      if (block == null)
        throw new IllegalStateException("block does not exist: " + blockId);
      blocks.add(block);
    }

    return new BlockIndex(blocks, _serviceIdIntervals, _intervalsByServiceId);
  }
}
