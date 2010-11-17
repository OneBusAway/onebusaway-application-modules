package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.EVehiclePhase;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
import org.onebusaway.transit_data_federation.services.beans.VehicleStatusBeanService;
import org.onebusaway.transit_data_federation.services.realtime.VehicleStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class VehicleStatusBeanServiceImpl implements VehicleStatusBeanService {

  private VehicleStatusService _vehicleStatusService;

  private TripDetailsBeanService _tripDetailsBeanService;

  @Autowired
  public void setVehicleStatusService(VehicleStatusService vehicleStatusService) {
    _vehicleStatusService = vehicleStatusService;
  }

  @Autowired
  public void setTripDetailsBeanService(
      TripDetailsBeanService tripDetailsBeanService) {
    _tripDetailsBeanService = tripDetailsBeanService;
  }

  public VehicleStatusBean getVehicleForId(AgencyAndId vehicleId, long time) {
    VehicleLocationRecord record = _vehicleStatusService.getVehicleLocationRecordForId(vehicleId);
    if (record == null)
      return null;
    return getRecordAsBean(record, time);
  }

  @Override
  public ListBean<VehicleStatusBean> getAllVehiclesForAgency(String agencyId,
      long time) {

    List<VehicleLocationRecord> records = _vehicleStatusService.getAllVehicleLocationRecords();

    List<VehicleStatusBean> beans = new ArrayList<VehicleStatusBean>();

    for (VehicleLocationRecord record : records) {
      AgencyAndId vid = record.getVehicleId();
      if (!vid.getAgencyId().equals(agencyId))
        continue;

      VehicleStatusBean bean = getRecordAsBean(record, time);
      beans.add(bean);
    }

    return new ListBean<VehicleStatusBean>(beans, false);
  }

  private VehicleStatusBean getRecordAsBean(VehicleLocationRecord record,
      long time) {

    VehicleStatusBean bean = new VehicleStatusBean();
    bean.setLastUpdateTime(record.getTimeOfRecord());

    EVehiclePhase phase = record.getPhase();
    if (phase != null)
      bean.setPhase(phase.toLabel());

    bean.setStatus(record.getStatus());

    if (record.isCurrentLocationSet())
      bean.setLocation(new CoordinatePoint(record.getCurrentLocationLat(),
          record.getCurrentLocationLon()));

    bean.setVehicleId(AgencyAndIdLibrary.convertToString(record.getVehicleId()));

    TripDetailsBean details = _tripDetailsBeanService.getTripForVehicle(
        record.getVehicleId(), time, new TripDetailsInclusionBean(true, false,
            true));
    if (details != null && details.getStatus() != null) {
      bean.setTrip(details.getTrip());
      bean.setTripStatus(details.getStatus());
    }

    return bean;
  }

}
