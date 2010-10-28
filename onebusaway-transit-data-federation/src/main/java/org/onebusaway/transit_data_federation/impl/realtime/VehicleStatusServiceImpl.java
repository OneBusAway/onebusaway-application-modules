package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockVehicleLocationListener;
import org.onebusaway.transit_data_federation.services.realtime.VehicleStatusService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class VehicleStatusServiceImpl implements VehicleLocationListener,
    VehicleStatusService {

  private ConcurrentHashMap<AgencyAndId, VehicleLocationRecord> _vehicleRecordsById = new ConcurrentHashMap<AgencyAndId, VehicleLocationRecord>();

  private TransitGraphDao _transitGraphDao;

  private BlockVehicleLocationListener _blockVehicleLocationService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockVehicleLocationService(
      BlockVehicleLocationListener service) {
    _blockVehicleLocationService = service;
  }

  /****
   * {@link VehicleLocationListener} Interface
   ****/

  @Override
  public void handleVehicleLocationRecord(VehicleLocationRecord record) {

    if (record.getTimeOfRecord() == 0)
      throw new IllegalArgumentException("you must specify a record time");

    _vehicleRecordsById.put(record.getVehicleId(), record);

    AgencyAndId blockId = record.getBlockId();

    if (blockId == null) {
      AgencyAndId tripId = record.getTripId();
      if (tripId != null) {
        TripEntry tripEntry = _transitGraphDao.getTripEntryForId(tripId);
        if (tripEntry == null)
          throw new IllegalArgumentException("trip not found with id=" + tripId);
        BlockEntry block = tripEntry.getBlock();
        blockId = block.getId();
      }
    }

    if (blockId != null && record.getServiceDate() != 0)
      _blockVehicleLocationService.handleVehicleLocationRecord(record);
  }

  @Override
  public void handleVehicleLocationRecords(List<VehicleLocationRecord> records) {
    for (VehicleLocationRecord record : records)
      handleVehicleLocationRecord(record);
  }

  @Override
  public void resetVehicleLocation(AgencyAndId vehicleId) {
    _vehicleRecordsById.remove(vehicleId);
    _blockVehicleLocationService.resetVehicleLocation(vehicleId);
  }

  /****
   * {@link VehicleStatusService} Interface
   ****/
  
  @Override
  public VehicleLocationRecord getVehicleLocationRecordForId(AgencyAndId vehicleId) {
    return _vehicleRecordsById.get(vehicleId);
  }


  @Override
  public List<VehicleLocationRecord> getAllVehicleLocationRecords() {
    return new ArrayList<VehicleLocationRecord>(_vehicleRecordsById.values());
  }
}
