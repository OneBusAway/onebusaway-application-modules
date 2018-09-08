/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.api.actions.api.where;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.Date;

public class VehiclePositionForVehicleAction extends ApiActionSupport {

    private static Logger _log = LoggerFactory.getLogger(VehiclePositionForVehicleAction.class);

    private static final long serialVersionUID = 1L;

    private static final int V2 = 2;

    @Autowired
    private TransitDataService _service;

    private String _id;

    private long _time = SystemTime.currentTimeMillis();

    public VehiclePositionForVehicleAction() {
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

        VehicleLocationRecordBean record = _service.getVehiclePositionForVehicleId(_id);

        if (record == null)
            return setResourceNotFoundResponse();
        return setOkResponse(record);
    }

}
