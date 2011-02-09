package org.onebusaway.api.actions.api.where;

import java.io.IOException;
import java.sql.Date;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.VehicleStatusV2Bean;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

public class VehicleLocationRecordsController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private VehicleLocationRecordQueryBean _query = new VehicleLocationRecordQueryBean();

  private String _vehicleId;

  public VehicleLocationRecordsController() {
    super(V2);
  }

  public void setBlockId(String blockId) {
    _query.setBlockId(blockId);
  }

  public void setTripId(String tripId) {
    _query.setTripId(tripId);
  }

  public void setVehicleId(String vehicleId) {
    _query.setVehicleId(_vehicleId);
  }

  public void setServiceDate(long serviceDate) {
    _query.setServiceDate(serviceDate);
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setFromTime(Date fromTime) {
    _query.setFromTime(fromTime.getTime());
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setToTime(Date toTime) {
    _query.setToTime(toTime.getTime());
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (!isVersion(V2))
      return setUnknownVersionResponse();

    if (hasErrors())
      return setValidationErrorsResponse();

    BeanFactoryV2 factory = getBeanFactoryV2();

    try {
      ListBean<VehicleLocationRecordBean> vehicles = _service.getVehicleLocationRecords(_query);
      return setOkResponse(factory.getVehicleLocationRecordResponse(vehicles));
    } catch (OutOfServiceAreaServiceException ex) {
      return setOkResponse(factory.getEmptyList(VehicleStatusV2Bean.class, true));
    }
  }
}
