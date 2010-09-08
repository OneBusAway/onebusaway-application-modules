package org.onebusaway.transit_data_federation.services.realtime;

import java.util.Set;

import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;

/**
 * A block instance is the combination of a {@link BlockEntry} and a service
 * date for which that block is active. The "service date" is the
 * "midnight time" from which the {@link StopTimeEntry} entries are relative.
 * 
 * @author bdferris
 * @see TripEntry
 */
public class BlockInstance {

  private final BlockEntry _block;

  private final long _serviceDate;

  private final Set<LocalizedServiceId> _serviceIds;

  public BlockInstance(BlockEntry block, long serviceDate,
      Set<LocalizedServiceId> serviceIds) {
    if (block == null || serviceIds == null)
      throw new IllegalArgumentException();
    _block = block;
    _serviceDate = serviceDate;
    _serviceIds = serviceIds;
  }

  public BlockEntry getBlock() {
    return _block;
  }

  /**
   * The service date that the trip instance is operating. This is the
   * "midnight" time relative to the stop times for the trip.
   * 
   * @return the service date on which the trip is operating (Unix-time)
   */
  public long getServiceDate() {
    return _serviceDate;
  }

  public Set<LocalizedServiceId> getServiceIds() {
    return _serviceIds;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_block == null) ? 0 : _block.hashCode());
    result = prime * result + (int) (_serviceDate ^ (_serviceDate >>> 32));
    result = prime * result
        + ((_serviceIds == null) ? 0 : _serviceIds.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof BlockInstance))
      return false;
    BlockInstance other = (BlockInstance) obj;
    if (_serviceDate != other._serviceDate)
      return false;
    if (!_block.equals(other._block))
      return false;
    if (!_serviceIds.equals(other._serviceIds))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return _block.toString() + " " + _serviceDate;
  }

}
