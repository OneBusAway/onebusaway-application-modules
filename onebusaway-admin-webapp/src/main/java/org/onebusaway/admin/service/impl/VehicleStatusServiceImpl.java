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
package org.onebusaway.admin.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.onebusaway.admin.comparator.InferredPhaseComparator;
import org.onebusaway.admin.comparator.LastUpdateComparator;
import org.onebusaway.admin.comparator.ObservedDSCComparator;
import org.onebusaway.admin.comparator.PullinTimeComparator;
import org.onebusaway.admin.comparator.PulloutTimeComparator;
import org.onebusaway.admin.comparator.VehicleIdComparator;
import org.onebusaway.admin.model.json.DestinationSignCode;
import org.onebusaway.admin.model.json.VehicleLastKnownRecord;
import org.onebusaway.admin.model.json.VehiclePullout;
import org.onebusaway.admin.model.ui.VehicleDetail;
import org.onebusaway.admin.model.ui.VehicleStatistics;
import org.onebusaway.admin.model.ui.VehicleStatus;
import org.onebusaway.admin.service.RemoteConnectionService;
import org.onebusaway.admin.service.VehicleSearchService;
import org.onebusaway.admin.service.VehicleStatusService;
import org.onebusaway.admin.util.VehicleDetailBuilder;
import org.onebusaway.admin.util.VehicleSearchParameters;
import org.onebusaway.admin.util.VehicleSortFields;
import org.onebusaway.admin.util.VehicleStatusBuilder;
import org.onebusaway.admin.util.VehicleStatusCache;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.stereotype.Component;


/**
 * Default implementation of {@link VehicleStatusService}
 * @author abelsare
 *
 */
@Component
public class VehicleStatusServiceImpl implements VehicleStatusService {

	private static Logger log = LoggerFactory.getLogger(VehicleStatusServiceImpl.class);
	private static final String DEFAULT_OPERATIONAL_API_HOST = "archive";
	
	private ConfigurationService configurationService;
	private RemoteConnectionService remoteConnectionService;
	private VehicleSearchService vehicleSearchService;
	
	private final ObjectMapper mapper = new ObjectMapper();
	private final VehicleStatusCache cache = new VehicleStatusCache();
	
	private int lastJsonHash;
	private Map<String, VehiclePullout> pullouts = new ConcurrentHashMap<String, VehiclePullout>();

	private class UpdateThread implements Runnable {
		String json;
		public UpdateThread(String json){
			log.debug("Update thread running!");
			this.json=json;
		}
	    @Override
	    public synchronized void run() {
			try {
				JSONArray pulloutContentArray = new JSONArray("[" +json + "]");
				for(int i=0; i<pulloutContentArray.length(); i++) {
					VehiclePullout pullout = convertToObject(pulloutContentArray.getString(i), VehiclePullout.class);
					//pullout can be null if no data is returned by web service call
					if(pullout !=null) {
						pullouts.put(pullout.getVehicleId(), pullout);
					}
				}
			} catch (JSONException e) {
				log.error("Error parsing json content : " +e);
				e.printStackTrace();
			}
	    }
	}
	
	  
	@Override
	public List<VehicleStatus> getVehicleStatus(boolean loadNew) {
		
		List<VehicleStatus> vehicleStatusRecords = null;
		//Load new data only if asked explicitly
		if(loadNew) {
			VehicleStatusBuilder builder = new VehicleStatusBuilder();
			
			//get last known record data from operational API
			List<VehicleLastKnownRecord> vehicleLastKnownRecords = getLastKnownRecordData();
			
			//get vehicle pipo data
			Map<String, VehiclePullout> vehiclePullouts = getPulloutData();
			
			vehicleStatusRecords = new ArrayList<VehicleStatus>();
			
			//Build vehicle status objects by getting the required fields from both collections
			for(VehicleLastKnownRecord lastknownRecord : vehicleLastKnownRecords) {
				VehiclePullout pullout = vehiclePullouts.get(lastknownRecord.getVehicleId());
				VehicleStatus vehicleStatus = builder.buildVehicleStatus(pullout, lastknownRecord);
				vehicleStatusRecords.add(vehicleStatus);
			}
			//Add these records to the cache
			cache.add(vehicleStatusRecords);
		} else {
			//return data from the cache to improve performance
			vehicleStatusRecords = cache.fetch();
		}
		
		return vehicleStatusRecords;
	}
	
	@Override
	public VehicleDetail getVehicleDetail(String vehicleId) {
	  VehicleDetailBuilder builder = new VehicleDetailBuilder();
	  VehicleLastKnownRecord lastKnownRecord = getLastKnownRecordData(vehicleId);
	  if (lastKnownRecord != null) { 
	    VehiclePullout pullout = getPulloutData(vehicleId);
	    String headSign = getHeadSign(lastKnownRecord.getDestinationSignCode());
	    String inferredHeadSign = getHeadSign(lastKnownRecord.getInferredDSC());
	    return builder.buildVehicleDetail(pullout, lastKnownRecord, headSign, inferredHeadSign);
	  }
	  return null;
	}

	@Override
	public List<VehicleStatus> search(Map<VehicleSearchParameters, String> searchParameters, boolean newSearch) {
		List<VehicleStatus> vehicleStatusRecords = cache.fetch();
		if(vehicleStatusRecords.isEmpty()) {
			//return empty result if cache is empty as there are no records available.
			return vehicleStatusRecords;
		}
		List<VehicleStatus> matchingRecords = null;
		if(newSearch) {
			matchingRecords = vehicleSearchService.search(vehicleStatusRecords, 
					searchParameters);
			//Add search results to cache
			cache.addSearchResults(matchingRecords);
		} else {
			matchingRecords = cache.getSearchResults();
		}
		
		
		return matchingRecords;
	}
	
	@Override
	public VehicleStatistics getVehicleStatistics(String... parameters) {
		VehicleStatistics statistics = new VehicleStatistics();
		
		List<VehicleStatus> vehicleStatusRecords = cache.fetch();
		if(vehicleStatusRecords.isEmpty()) {
			//this should ideally never happen as statistics call should trigger after data
			//is loaded in the grid. Still get the records from web services if cache is empty to be
			//safe
			vehicleStatusRecords = getVehicleStatus(true);
		}
		List<VehicleStatus> vehiclesInEmergency = vehicleSearchService.searchVehiclesInEmergency(vehicleStatusRecords);
		List<VehicleStatus> vehiclesInRevenueService = vehicleSearchService.
				searchVehiclesInRevenueService(vehicleStatusRecords);
		List<VehicleStatus> vehiclesTracked = vehicleSearchService.searchVehiclesTracked(5, vehicleStatusRecords);
		
		statistics.setVehiclesInEmergency(vehiclesInEmergency.size());
		statistics.setVehiclesInRevenueService(vehiclesInRevenueService.size());
		statistics.setVehiclesTracked(vehiclesTracked.size());
		
		return statistics;
	}
	
	@Override
	public void sort(List<VehicleStatus> vehiclesPerPage, String field, String order) {
		VehicleSortFields sortField = VehicleSortFields.valueOf(field.toUpperCase());
		Comparator<VehicleStatus> fieldComparator = null;
		switch(sortField) {
		
			case VEHICLEID :
				fieldComparator = new VehicleIdComparator(order);
				break;
			
			case LASTUPDATE :
				fieldComparator = new LastUpdateComparator(order);
				break;
			
			case INFERREDPHASE :
				fieldComparator = new InferredPhaseComparator(order);
				break;
				
			case OBSERVEDDSC :
				fieldComparator = new ObservedDSCComparator(order);
				break;
				
			case PULLOUTTIME :
				fieldComparator = new PulloutTimeComparator(order);
				break;
				
			case PULLINTIME :
				fieldComparator = new PullinTimeComparator(order);
				break;
			
			default :
				fieldComparator = new VehicleIdComparator(order);
				break;
		}
		Collections.sort(vehiclesPerPage, fieldComparator);
	}
	
	private Map<String, VehiclePullout> getPulloutData() {
		String tdmHost = System.getProperty("tdm.host");
		String url = buildURL(tdmHost, "/pullouts/list");
		log.debug("making request for : " +url);

		String vehiclePipocontent = remoteConnectionService.getContent(url);

		String json = extractJsonArrayString(vehiclePipocontent);
		if(json!=null && json.hashCode()!=lastJsonHash){
			lastJsonHash=json.hashCode();
			new UpdateThread(json).run();
		}
		else log.debug("Constructing pullout map was skipped!");
		return pullouts;
	}

	private VehiclePullout getPulloutData(String vehicleId) {
		return pullouts.get(vehicleId);
	}

	private List<VehicleLastKnownRecord> getLastKnownRecordData() {
		List<VehicleLastKnownRecord> lastKnownRecords = new ArrayList<VehicleLastKnownRecord>();
		
		String operationalAPIHost = null;
		
		try {
			operationalAPIHost = configurationService.getConfigurationValueAsString("operational-api.host", DEFAULT_OPERATIONAL_API_HOST);
		} catch(RemoteConnectFailureException e) {
			log.error("Failed retrieving operational API host from TDM. Setting to default value");
			operationalAPIHost = DEFAULT_OPERATIONAL_API_HOST;
		}
		
		String url = buildURL(operationalAPIHost, "/record/last-known/list");
		log.debug("making request for : " +url);
		
		String lastknownContent = remoteConnectionService.getContent(url);
		lastknownContent = lastknownContent.replace(System.getProperty("line.separator"), "");
		
		String json = extractJsonArrayString(lastknownContent);
		
		try {
			JSONArray lastKnownContentArray = new JSONArray("[" +json + "]");
			for(int i=0; i<lastKnownContentArray.length(); i++) {
				VehicleLastKnownRecord lastKnownRecord = convertToObject(lastKnownContentArray.getString(i), VehicleLastKnownRecord.class);
				//lastknownrecord can be null if no data is returned by web service call
				if(lastKnownRecord != null) {
					lastKnownRecords.add(lastKnownRecord);
				}
			}
		} catch (JSONException e) {
			log.error("Error parsing json content : " +e);
			e.printStackTrace();
		}
		
		return lastKnownRecords;
	}

	private VehicleLastKnownRecord getLastKnownRecordData(String vehicleId) {
		List<VehicleLastKnownRecord> lastKnownRecords = new ArrayList<VehicleLastKnownRecord>();
		
		String operationalAPIHost = null;
		
		try {
			operationalAPIHost = configurationService.getConfigurationValueAsString("operational-api.host", DEFAULT_OPERATIONAL_API_HOST);
		} catch(RemoteConnectFailureException e) {
			log.error("Failed retrieving operational API host from TDM. Setting to default value");
			operationalAPIHost = DEFAULT_OPERATIONAL_API_HOST;
		}
		
		String url = buildURL(operationalAPIHost, "/record/last-known/vehicle/" + vehicleId);
		log.info("making request for : " +url);
		
		String lastknownContent = remoteConnectionService.getContent(url);
		lastknownContent = lastknownContent.replace(System.getProperty("line.separator"), "");
		
		String json = extractJsonArrayString(lastknownContent);
		
		try {
			JSONArray lastKnownContentArray = new JSONArray("[" +json + "]");
			for(int i=0; i<lastKnownContentArray.length(); i++) {
				VehicleLastKnownRecord lastKnownRecord = convertToObject(lastKnownContentArray.getString(i), VehicleLastKnownRecord.class);
				//lastknownrecord can be null if no data is returned by web service call
				if(lastKnownRecord != null) {
					lastKnownRecords.add(lastKnownRecord);
				}
			}
		} catch (JSONException e) {
			log.error("Error parsing json content : " +e);
			e.printStackTrace();
		}
		
		if (!lastKnownRecords.isEmpty())
		  return lastKnownRecords.get(0);
		return null;
	}

  private String getHeadSign(String dsc) {
	    if (dsc==null) {
	      return null;
	    }
		String tdmHost = System.getProperty("tdm.host");
		String url = buildURL(tdmHost, "/dsc/" +  dsc + "/sign");
		log.debug("making request for : " +url);
		try {
		  String headSignContent = remoteConnectionService.getContent(url);
		  if (headSignContent == null) { return null;}
		  String json = extractJsonObjectString(headSignContent);
		  if (json == null) {
		    log.error("missing headSign for url=" + url);
		    return null;
		  }
		  DestinationSignCode headSign = null;
		  headSign = convertToObject("{" + json + "}", DestinationSignCode.class);
		
		  if (headSign != null){
		    return headSign.getMessageText();
		  }
		} catch (Exception any) {
		  log.error("getHeadSign failed:", any);
		}
		return null;
	}

	
	private <T> T convertToObject(String content, Class<T> objectType) {
		T object = null;
		try {
			object = mapper.readValue(content, objectType);
		} catch (JsonParseException e) {
			log.error("Error parsing json content : " +e);
			e.printStackTrace();
		} catch (JsonMappingException e) {
			log.error("Error parsing json content : " +e);
			e.printStackTrace();
		} catch (IOException e) {
			log.error("Error parsing json content : " +e);
			e.printStackTrace();
		}
		return object;
	}
	
	private String extractJsonArrayString(String content) {
		String json = null;
		final Pattern pattern = Pattern.compile("\\[(.+?)\\]");
		final Matcher matcher = pattern.matcher(content);
		if(matcher.find() ) {
			json = matcher.group(1);
		}
		return json;
	}

	private String extractJsonObjectString(String content) {
		String json = null;
		final Pattern pattern = Pattern.compile("^\\{.*\\{(.+?)\\}.*\\}$");
		final Matcher matcher = pattern.matcher(content);
		if(matcher.find() ) {
			json = matcher.group(1);
		}
		return json;
	}

	private String buildURL(String host, String api) {
		 return "http://" + host + "/api" + api;
	}
	
	/**
	 * Injects configuration service
	 * @param configurationService the configurationService to set
	 */
	@Autowired
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	/**
	 * Injects remote connection service
	 * @param remoteConnectionService the remoteConnectionService to set
	 */
	@Autowired
	public void setRemoteConnectionService(
			RemoteConnectionService remoteConnectionService) {
		this.remoteConnectionService = remoteConnectionService;
	}


	/**
	 * Injects vehicle search service
	 * @param vehicleSearchService the vehicleSearchService to set
	 */
	@Autowired
	public void setVehicleSearchService(VehicleSearchService vehicleSearchService) {
		this.vehicleSearchService = vehicleSearchService;
	}

}
