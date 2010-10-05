package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;

public class BlockEntryImpl implements BlockEntry, Serializable {

  private static final long serialVersionUID = 3L;

  private AgencyAndId _id;

  private List<BlockConfigurationEntry> _configurations;

  public void setId(AgencyAndId id) {
    _id = id;
  }
  
  public void setConfigurations(List<BlockConfigurationEntry> configurations) {
    _configurations = configurations;
  }

  /****
   * {@link BlockEntry} Interface
   ****/

  @Override
  public AgencyAndId getId() {
    return _id;
  }
  

  @Override
  public List<BlockConfigurationEntry> getConfigurations() {
    return _configurations;
  }

  @Override
  public String toString() {
    return _id.toString();
  }
}
