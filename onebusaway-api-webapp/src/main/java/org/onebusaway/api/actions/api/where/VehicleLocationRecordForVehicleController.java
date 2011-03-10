package org.onebusaway.api.actions.api.where;

import java.io.IOException;
import java.sql.Date;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

public class VehicleLocationRecordForVehicleController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private String _id;

  private long _time = System.currentTimeMillis();

  public VehicleLocationRecordForVehicleController() {
    super(V2);
  }

  public void setId(String id) {
    _id = id;
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setTime(Date time) {
    _time = time.getTime();
  }

  public DefaultHttpHeaders show() throws IOException, ServiceException {

    if (!isVersion(V2))
      return setUnknownVersionResponse();

    if (hasErrors())
      return setValidationErrorsResponse();

    BeanFactoryV2 factory = getBeanFactoryV2();

    VehicleLocationRecordBean record = _service.getVehicleLocationRecordForVehicleId(
        _id, _time);
    if (record == null)
      return setResourceNotFoundResponse();
    return setOkResponse(factory.entry(factory.getVehicleLocationRecord(record)));
  }
}
