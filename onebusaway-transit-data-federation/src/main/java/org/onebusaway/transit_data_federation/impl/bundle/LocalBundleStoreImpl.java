/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

package org.onebusaway.transit_data_federation.impl.bundle;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.config.BundleMetadata;
import org.onebusaway.transit_data_federation.impl.config.BundleConfigDao;
import org.onebusaway.transit_data_federation.model.bundle.BundleItem;
import org.onebusaway.transit_data_federation.services.bundle.BundleStoreService;
import org.onebusaway.utility.ObjectSerializationLibrary;

/*import org.joda.time.DateTime;*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A bundle source backed by an on-disk directory.
 * 
 * @author jmaki
 *
 */
public class LocalBundleStoreImpl implements BundleStoreService {

	private static Logger _log = LoggerFactory.getLogger(LocalBundleStoreImpl.class);
	
	private final String CALENDAR_DATA = "CalendarServiceData.obj";
	
	private final String METADATA = "metadata.json";
	
	private boolean legacyBundle = false;

	private String _bundleRootPath = null;

	public LocalBundleStoreImpl(String bundleRootPath) {
		_bundleRootPath = bundleRootPath;
	}

	@Override
	public List<BundleItem> getBundles() {
		ArrayList<BundleItem> output = new ArrayList<BundleItem>();

		File bundleRoot = new File(_bundleRootPath);

		if(!bundleRoot.isDirectory()) {    
			return output;
		}

		for(String filename : bundleRoot.list()) {
			File possibleBundle = new File(bundleRoot, filename);

			if(possibleBundle.isDirectory()) {
				File calendarServiceObjectFile = new File(possibleBundle, CALENDAR_DATA);
				File metadataFile = new File(possibleBundle, METADATA);

				if(!calendarServiceObjectFile.exists()) {
					_log.warn("Could not find " + CALENDAR_DATA + " in local bundle '" + filename + "'; skipping. Not a bundle?");
					continue;
				}
				
				if(!metadataFile.exists()) {
					_log.warn("Could not find " + METADATA + " in local bundle '" + filename + "'; skipping. Not a bundle?");
					continue;
				}
				
				try{
	        BundleItem validLocalBundle = createBundleItem(calendarServiceObjectFile, metadataFile, filename);
	        output.add(validLocalBundle);
	      }catch(Exception e) {
	        continue;
	      }   
			}
			else if(possibleBundle.isFile() && possibleBundle.getName().equalsIgnoreCase(CALENDAR_DATA)){
			  try{
			    String parentFilename = bundleRoot.getName();
	        BundleItem validLocalBundle = createBundleItem(possibleBundle, parentFilename);
	        output.add(validLocalBundle);
	        setLegacyBundle(true);
	        break;
	      }catch(Exception e) {
	        continue;
	      }   
			}

		}
		
		return output;
	}
	
	private BundleItem createBundleItem(File calendarServiceObjectFile, File metadataFile, String filename) throws Exception{
    // get data to fill in the BundleItem for this bundle.
	
    ServiceDate minServiceDate = null;
    ServiceDate maxServiceDate = null;
    try {
    	ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        BundleMetadata meta = null;
        
        meta = mapper.readValue(metadataFile, BundleMetadata.class);
       
    	// Convert metadata String Dates to Date Objects
    	Date fromDate = new Date(Long.parseLong(meta.getServiceDateFrom()));
    	Date toDate = new Date(Long.parseLong(meta.getServiceDateTo()));

    	// Convert Date Objects to ServiceDate Objects
    	minServiceDate = new ServiceDate(fromDate);
        maxServiceDate = new ServiceDate(toDate);
        
        
    } catch(Exception e) {
    	_log.error(e.getMessage());
      _log.error("Deserialization of metadata.json in local bundle " + filename + "; skipping.");
      throw new Exception(e);
    }        
	  
    _log.info("Found local bundle " + filename + " with service range " + 
        minServiceDate + " => " + maxServiceDate);
	  
	  BundleItem bundleItem = new BundleItem();
	  bundleItem.setId(filename);
	  bundleItem.setName(filename);

	  bundleItem.setServiceDateFrom(minServiceDate);
	  bundleItem.setServiceDateTo(maxServiceDate);  
	  
	  
	  
	  DateTime lastModified = new DateTime(calendarServiceObjectFile.lastModified());
	  
	  bundleItem.setCreated(lastModified);
	  bundleItem.setUpdated(lastModified);
	  
    return bundleItem;
	}
	private BundleItem createBundleItem(File calendarServiceObjectFile, String filename) throws Exception{
	    // get data to fill in the BundleItem for this bundle.
	    ServiceDate minServiceDate = null;
	    ServiceDate maxServiceDate = null;

	    try {
	      CalendarServiceData data = 
	          ObjectSerializationLibrary.readObject(calendarServiceObjectFile);

	      // loop through all service IDs and find the minimum and max--most likely they'll all
	      // be the same range, but not necessarily...
	      for(AgencyAndId serviceId : data.getServiceIds()) {
	        for(ServiceDate serviceDate : data.getServiceDatesForServiceId(serviceId)) {
	          if(minServiceDate == null || serviceDate.compareTo(minServiceDate) <= 0) {
	            minServiceDate = serviceDate;
	          }

	          if(maxServiceDate == null || serviceDate.compareTo(maxServiceDate) >= 0) {
	            maxServiceDate = serviceDate;
	          }
	        }
	      }             
	    } catch(Exception e) {
	      _log.info("Deserialization of " + CALENDAR_DATA + " in local bundle " + filename + "; skipping.");
	      throw new Exception(e);
	    }        
		  
	    _log.info("Found local bundle " + filename + " with service range " + 
	        minServiceDate + " => " + maxServiceDate);
		  
		  BundleItem bundleItem = new BundleItem();
		  bundleItem.setId(filename);
		  bundleItem.setName(filename);

		  bundleItem.setServiceDateFrom(minServiceDate);
		  bundleItem.setServiceDateTo(maxServiceDate);  
		  
		  DateTime lastModified = new DateTime(calendarServiceObjectFile.lastModified());
		  
		  bundleItem.setCreated(lastModified);
		  bundleItem.setUpdated(lastModified);
		  
	    return bundleItem;
	 }
  public boolean isLegacyBundle() {
    return legacyBundle;
  }

  public void setLegacyBundle(boolean legacyBundle) {
    this.legacyBundle = legacyBundle;
  }
      
}
