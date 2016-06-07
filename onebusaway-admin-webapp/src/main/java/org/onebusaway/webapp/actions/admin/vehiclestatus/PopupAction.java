/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.webapp.actions.admin.vehiclestatus;

import org.onebusaway.admin.model.ui.VehicleDetail;
import org.onebusaway.admin.model.ui.VehicleStatus;
import org.onebusaway.admin.service.VehicleStatusService;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PopupAction extends OneBusAwayNYCAdminActionSupport {
  private static Logger _log = LoggerFactory.getLogger(PopupAction.class);	
  private static final long serialVersionUID = 1L;
  private VehicleStatusService vehicleStatusService;
	private ConfigurationService configurationService;
	private VehicleDetail vehicleDetailRecord;
	
  private String vehicleId = null;


  public String input() {
    _log.info("in input");
    try {
      vehicleDetailRecord = vehicleStatusService.getVehicleDetail(vehicleId);
    } catch (Throwable t) {
      _log.error("input failed:", t);
      vehicleDetailRecord = null;
    }
    _log.info("retrieved vehicleDetail=" + vehicleDetailRecord);
    return INPUT;
  }
  
  
  /**
	 * Injects vehicle status service
	 * @param vehicleStatusService the vehicleStatusService to set
	 */
	@Autowired
	public void setVehicleStatusService(VehicleStatusService vehicleStatusService) {
		this.vehicleStatusService = vehicleStatusService;
	}

	@Autowired
	public void setConfigurationService(ConfigurationService configurationService) {
	  this.configurationService = configurationService;
	}

 public String getGoogleMapsClientId() {
    return configurationService.getConfigurationValueAsString("display.googleMapsClientId", "");    
  }

	public String getVehicleId() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getVehicleId();
	  }
	  return null;
	}
	
	public void setVehicleId(String vehicleId) {
	  this.vehicleId = vehicleId;
	}

	public String getLocation() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getLocation();
	  }
	  return null;
	}
	
	public Double getOrientation() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getDirection();
	  }
	  return null;
	}
	
	public String getDepot() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getDepot();
	  }
	  return null;
	}
	
	public String getHeadSign() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getHeadSign();
	  }
	  return null;
	}
	
	public String getInferredHeadSign() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getInferredHeadSign();
	  }
	  return null;
	}
	
	public String getServiceDate() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getServiceDate();
	  }
	  return null;
	}

	public String getOperatorId() {
	  if (vehicleDetailRecord != null){
	    return vehicleDetailRecord.getOperatorId();
	  }
	  return null;
	}
	
	public String getAgency() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getAgency();
	  }
	  return null;
	}
	
	public String getObservedRunId() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getObservedRunId();
	  }
	  return null;
	}
	
	public String getUtsRunId() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getUtsRunId();
	  }
	  return null;
	}
	
	public String getInferredRunId() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getInferredRunId();
	  }
	  return null;
	}
	
	// in seconds
	public Long getScheduleDeviation() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getScheduleDeviation();
	  }
	  return null;
	}
	
	public String getScheduleDeviationAsString() {
	  Long dev = getScheduleDeviation();
	  if (dev == null) return null;
	  if (dev > 120)
	    return (dev/60) + " min";
	  return dev + " s";
	}
	
	public String getTripId() {
	  if (vehicleDetailRecord != null) {
	    return vehicleDetailRecord.getTripId();
	  }
	  return null;
	}
}
