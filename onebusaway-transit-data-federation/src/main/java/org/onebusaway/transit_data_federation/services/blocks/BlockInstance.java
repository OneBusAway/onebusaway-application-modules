package org.onebusaway.transit_data_federation.services.blocks;

import java.util.HashSet;
import java.util.Set;

import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstance;

/**
 * A block instance is the combination of a {@link BlockEntry} and a service
 * date for which that block is active. The "service date" is the
 * "midnight time" from which the {@link StopTimeEntry} entries are relative.
 * Blocks are slightly more complicated than {@link TripInstance}, because a
 * block can be composed of trips with different service ids, not all which are
 * necessarily active on a given service date.
 * 
 * @author bdferris
 * @see BlockEntry
 * @see LocalizedServiceId
 */
public class BlockInstance {

  private final BlockEntry _block;

  private final long _serviceDate;

  private final Set<LocalizedServiceId> _serviceIds;

  private final boolean _allServiceIdsActive;

  public BlockInstance(BlockEntry block, long serviceDate,
      Set<LocalizedServiceId> serviceIds, boolean allServiceIdsActive) {
    if (block == null || serviceIds == null)
      throw new IllegalArgumentException();
    _block = block;
    _serviceDate = serviceDate;
    _serviceIds = serviceIds;
    _allServiceIdsActive = allServiceIdsActive;
  }

  public BlockInstance(BlockEntry block, long serviceDate) {
    this(block, serviceDate, new HashSet<LocalizedServiceId>(), false);
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

  public boolean isAllServiceIdsActive() {
    return _allServiceIdsActive;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_allServiceIdsActive ? 1231 : 1237);
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
    if (getClass() != obj.getClass())
      return false;
    BlockInstance other = (BlockInstance) obj;
    if (_allServiceIdsActive != other._allServiceIdsActive)
      return false;
    if (_block == null) {
      if (other._block != null)
        return false;
    } else if (!_block.equals(other._block))
      return false;
    if (_serviceDate != other._serviceDate)
      return false;
    if (_serviceIds == null) {
      if (other._serviceIds != null)
        return false;
    } else if (!_serviceIds.equals(other._serviceIds))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return _block.toString() + " " + _serviceDate;
  }

}
