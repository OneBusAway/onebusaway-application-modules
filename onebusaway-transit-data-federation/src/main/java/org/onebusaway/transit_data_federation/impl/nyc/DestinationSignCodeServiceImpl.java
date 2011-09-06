/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.transit_data_federation.impl.nyc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.nyc.DestinationSignCodeService;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class DestinationSignCodeServiceImpl implements DestinationSignCodeService {

  private Map<String, List<AgencyAndId>> _dscToTripMap;

  private Map<AgencyAndId, String> _tripToDscMap;
  
  private Set<String> _notInServiceDscs;

  @Autowired
  private FederatedTransitDataBundle _bundle;
  
  @PostConstruct
  public void setup() throws IOException, ClassNotFoundException {
	File dscToTripPath = _bundle.getDSCForTripIndex();
	if (dscToTripPath.exists()) {
		_dscToTripMap = ObjectSerializationLibrary.readObject(dscToTripPath);
	} else {
		_dscToTripMap = new HashMap<String, List<AgencyAndId>>();
	}

	File tripToDscPath = _bundle.getTripsForDSCIndex();
	if (tripToDscPath.exists()) {
		_tripToDscMap = ObjectSerializationLibrary.readObject(tripToDscPath);
	} else {
		_tripToDscMap = new HashMap<AgencyAndId, String>();
	}

	File notInServiceDSCPath = _bundle.getNotInServiceDSCs();
	if (notInServiceDSCPath.exists()) {
		_notInServiceDscs = ObjectSerializationLibrary.readObject(notInServiceDSCPath);
	} else {
		_notInServiceDscs = new HashSet<String>();
	}
  }	
	
  @Override
  public List<AgencyAndId> getTripIdsForDestinationSignCode(String destinationSignCode) {
	  return _dscToTripMap.get(destinationSignCode);
  }

  @Override
  public String getDestinationSignCodeForTripId(AgencyAndId tripId) {
	  return _tripToDscMap.get(tripId);
  }

  @Override
  public boolean isOutOfServiceDestinationSignCode(String destinationSignCode) {
	  return _notInServiceDscs.contains(destinationSignCode);
  }

  public boolean isMissingDestinationSignCode(String destinationSignCode) {
    return "0000".equals(destinationSignCode);
  }

  @Override
  public boolean isUnknownDestinationSignCode(String destinationSignCode) {
	  if(_notInServiceDscs.contains(destinationSignCode) 
			  || _dscToTripMap.containsKey(destinationSignCode))
		  return false;
	  else
		  return true;
  }
}
