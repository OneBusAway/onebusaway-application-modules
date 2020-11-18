/**
 * Copyright (C) 2010 OpenPlans
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
package org.onebusaway.presentation.impl.realtime;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.presentation.services.realtime.PresentationService;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleOccupancyRecord;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.blocks.BlockStopTimeBean;
import org.onebusaway.transit_data.model.blocks.BlockTripBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.siri.SiriDistanceExtension;
import org.onebusaway.transit_data_federation.siri.SiriExtensionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.siri.siri.BlockRefStructure;
import uk.org.siri.siri.DataFrameRefStructure;
import uk.org.siri.siri.DestinationRefStructure;
import uk.org.siri.siri.DirectionRefStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.FramedVehicleJourneyRefStructure;
import uk.org.siri.siri.JourneyPatternRefStructure;
import uk.org.siri.siri.JourneyPlaceRefStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.LocationStructure;
import uk.org.siri.siri.MonitoredCallStructure;
import uk.org.siri.siri.MonitoredVehicleJourneyStructure;
import uk.org.siri.siri.NaturalLanguageStringStructure;
import uk.org.siri.siri.OccupancyEnumeration;
import uk.org.siri.siri.OnwardCallStructure;
import uk.org.siri.siri.OnwardCallsStructure;
import uk.org.siri.siri.OperatorRefStructure;
import uk.org.siri.siri.ProgressRateEnumeration;
import uk.org.siri.siri.SituationRefStructure;
import uk.org.siri.siri.SituationSimpleRefStructure;
import uk.org.siri.siri.StopPointRefStructure;
import uk.org.siri.siri.VehicleModesEnumeration;
import uk.org.siri.siri.VehicleRefStructure;

import static org.apache.commons.lang.StringUtils.isBlank;

public final class SiriSupport {

	private static Logger _log = LoggerFactory.getLogger(SiriSupport.class);

	
	public enum OnwardCallsMode {
		VEHICLE_MONITORING,
		STOP_MONITORING
	}

	/**
	 * NOTE: The tripDetails bean here may not be for the trip the vehicle is currently on 
	 * in the case of A-D for stop!
	 */
	@SuppressWarnings("unused")
	public static void fillMonitoredVehicleJourney(MonitoredVehicleJourneyStructure monitoredVehicleJourney, 
			TripBean framedJourneyTripBean, TripStatusBean currentVehicleTripStatus, StopBean monitoredCallStopBean, OnwardCallsMode onwardCallsMode,
			PresentationService presentationService, TransitDataService transitDataService,
			int maximumOnwardCalls, List<TimepointPredictionRecord> stopLevelPredictions, boolean hasRealtimeData, long responseTimestamp, boolean showRawLocation, boolean showApc) {


		if (currentVehicleTripStatus != null && TransitDataConstants.STATUS_CANCELED.equals(currentVehicleTripStatus.getStatus())) {
			_log.error("aborting fillMVJ as trip is canceled");
			return;
		}

		BlockInstanceBean blockInstance = 
				transitDataService.getBlockInstance(currentVehicleTripStatus.getActiveTrip().getBlockId(), currentVehicleTripStatus.getServiceDate());
		
		List<BlockTripBean> blockTrips = blockInstance.getBlockConfiguration().getTrips();

		if(monitoredCallStopBean == null) {
			monitoredCallStopBean = currentVehicleTripStatus.getNextStop();
		}
		
		/////////////

		LineRefStructure lineRef = new LineRefStructure();
		lineRef.setValue(framedJourneyTripBean.getRoute().getId());
		monitoredVehicleJourney.setLineRef(lineRef);

		OperatorRefStructure operatorRef = new OperatorRefStructure();
		operatorRef.setValue( framedJourneyTripBean.getRoute().getId().split("_")[0] );
		monitoredVehicleJourney.setOperatorRef(operatorRef);

		DirectionRefStructure directionRef = new DirectionRefStructure();
		directionRef.setValue(framedJourneyTripBean.getDirectionId());
		monitoredVehicleJourney.setDirectionRef(directionRef);

		NaturalLanguageStringStructure routeShortName = new NaturalLanguageStringStructure();
		String shortName = framedJourneyTripBean.getRoute().getShortName();
		if (shortName == null) {
		  shortName = framedJourneyTripBean.getRoute().getId().split("_")[1];
		}
		if (!isBlank(currentVehicleTripStatus.getActiveTrip().getRouteShortName())) {
			// look for an override like an express desginator
			routeShortName.setValue(currentVehicleTripStatus.getActiveTrip().getRouteShortName());
		} else {
			routeShortName.setValue(shortName);
		}
		monitoredVehicleJourney.setPublishedLineName(routeShortName);

		JourneyPatternRefStructure journeyPattern = new JourneyPatternRefStructure();
		journeyPattern.setValue(framedJourneyTripBean.getShapeId());
		monitoredVehicleJourney.setJourneyPatternRef(journeyPattern);

		NaturalLanguageStringStructure headsign = new NaturalLanguageStringStructure();
		headsign.setValue(framedJourneyTripBean.getTripHeadsign());
		monitoredVehicleJourney.setDestinationName(headsign);
		
		VehicleRefStructure vehicleRef = new VehicleRefStructure();
		
		if(currentVehicleTripStatus.getVehicleId() == null){
			String tripId = framedJourneyTripBean.getId();
			String blockId = framedJourneyTripBean.getBlockId();
			String directionId = framedJourneyTripBean.getDirectionId();
			String vehicleIdHash = Integer.toString((tripId + blockId + directionId).hashCode());
			String agencyName = tripId.split("_")[0];
			String vehicleId = agencyName + "_" + vehicleIdHash;
			
			vehicleRef.setValue(vehicleId);
		}
		else{
			vehicleRef.setValue(currentVehicleTripStatus.getVehicleId());
		}
		
		monitoredVehicleJourney.setVehicleRef(vehicleRef);

		monitoredVehicleJourney.getVehicleMode().add(toVehicleMode(currentVehicleTripStatus.getVehicleType()));

		monitoredVehicleJourney.setMonitored(currentVehicleTripStatus.isPredicted());

		monitoredVehicleJourney.setBearing((float)currentVehicleTripStatus.getOrientation());

		monitoredVehicleJourney.setProgressRate(getProgressRateForPhaseAndStatus(
				currentVehicleTripStatus.getStatus(), currentVehicleTripStatus.getPhase()));

		if (showApc) {
			fillOccupancy(monitoredVehicleJourney, transitDataService, currentVehicleTripStatus);
		}

		// origin-destination
		for(int i = 0; i < blockTrips.size(); i++) {
			BlockTripBean blockTrip = blockTrips.get(i);

			if(blockTrip.getTrip().getId().equals(framedJourneyTripBean.getId())) {
				List<BlockStopTimeBean> stops = blockTrip.getBlockStopTimes();
				
				JourneyPlaceRefStructure origin = new JourneyPlaceRefStructure();
				origin.setValue(stops.get(0).getStopTime().getStop().getId());
				monitoredVehicleJourney.setOriginRef(origin);
				
				StopBean lastStop = stops.get(stops.size() - 1).getStopTime().getStop();
				DestinationRefStructure dest = new DestinationRefStructure();
				dest.setValue(lastStop.getId());
				monitoredVehicleJourney.setDestinationRef(dest);
				
				break;
			}
		}

		// framed journey 
		FramedVehicleJourneyRefStructure framedJourney = new FramedVehicleJourneyRefStructure();
		DataFrameRefStructure dataFrame = new DataFrameRefStructure();
		dataFrame.setValue(String.format("%1$tY-%1$tm-%1$td", currentVehicleTripStatus.getServiceDate()));
		framedJourney.setDataFrameRef(dataFrame);
		framedJourney.setDatedVehicleJourneyRef(framedJourneyTripBean.getId());
		monitoredVehicleJourney.setFramedVehicleJourneyRef(framedJourney);

		// location
		// if vehicle is detected to be on detour, use actual lat/lon, not snapped location.
		LocationStructure location = new LocationStructure();

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(6);

        //if we want to show the raw location AND we have realtime or if its on detour, show actual location
        if ((showRawLocation && currentVehicleTripStatus.getLastKnownLocation() != null) || (presentationService.isOnDetour(currentVehicleTripStatus))) {
            location.setLatitude(new BigDecimal(df.format(currentVehicleTripStatus.getLastKnownLocation().getLat())));
            location.setLongitude(new BigDecimal(df.format(currentVehicleTripStatus.getLastKnownLocation().getLon())));
        } else { //show snapped location
            if (currentVehicleTripStatus.getLocation() != null) {
                location.setLatitude(new BigDecimal(df.format(currentVehicleTripStatus.getLocation().getLat())));
                location.setLongitude(new BigDecimal(df.format(currentVehicleTripStatus.getLocation().getLon())));
            }
        }

		monitoredVehicleJourney.setVehicleLocation(location);

		// progress status
		List<String> progressStatuses = new ArrayList<String>();

		if (presentationService.isInLayover(currentVehicleTripStatus)) {
			progressStatuses.add("layover");
		}

		// "prevTrip" really means not on the framedvehiclejourney trip
		if(!framedJourneyTripBean.getId().equals(currentVehicleTripStatus.getActiveTrip().getId())) {
			progressStatuses.add("prevTrip");
		}

		if(!progressStatuses.isEmpty()) {
			NaturalLanguageStringStructure progressStatus = new NaturalLanguageStringStructure();
			progressStatus.setValue(StringUtils.join(progressStatuses, ","));
			monitoredVehicleJourney.setProgressStatus(progressStatus);    	
		}

		// block ref (including it all the time in case needed)
			BlockRefStructure blockRef = new BlockRefStructure();
			blockRef.setValue(framedJourneyTripBean.getBlockId());
			monitoredVehicleJourney.setBlockRef(blockRef);

		// scheduled depature time
		if (presentationService.isBlockLevelInference(currentVehicleTripStatus) 
				&& (presentationService.isInLayover(currentVehicleTripStatus) 
				|| !framedJourneyTripBean.getId().equals(currentVehicleTripStatus.getActiveTrip().getId()))) {
			BlockStopTimeBean originDepartureStopTime = null;

			for(int t = 0; t < blockTrips.size(); t++) {
				BlockTripBean thisTrip = blockTrips.get(t);
				BlockTripBean nextTrip = null;    		
				if(t + 1 < blockTrips.size()) {
					nextTrip = blockTrips.get(t + 1);
				}

				if(thisTrip.getTrip().getId().equals(currentVehicleTripStatus.getActiveTrip().getId())) {    			
					// just started new trip
					if(currentVehicleTripStatus.getDistanceAlongTrip() < (0.5 * currentVehicleTripStatus.getTotalDistanceAlongTrip())) {
						originDepartureStopTime = thisTrip.getBlockStopTimes().get(0);

					// at end of previous trip
					} else {
						if(nextTrip != null) {
							originDepartureStopTime = nextTrip.getBlockStopTimes().get(0);
						}
					}

					break;
				}
			}

			if(originDepartureStopTime != null) {            	
				Date departureTime = new Date(currentVehicleTripStatus.getServiceDate() + (originDepartureStopTime.getStopTime().getDepartureTime() * 1000));
				monitoredVehicleJourney.setOriginAimedDepartureTime(departureTime);
			}
		}    
		
		Map<String, TimepointPredictionRecord> stopIdToPredictionRecordMap = new HashMap<String, TimepointPredictionRecord>();
		
		// (build map of vehicle IDs to TPRs)
		if(stopLevelPredictions != null) {
			for(TimepointPredictionRecord tpr : stopLevelPredictions) {
				if (!tpr.isSkipped()) {
					// prune skipped stops from prediction map
					stopIdToPredictionRecordMap.put(AgencyAndId.convertToString(tpr.getTimepointId()), tpr);
				}
			}
		}
		
		// monitored call
		if(!presentationService.isOnDetour(currentVehicleTripStatus))
			fillMonitoredCall(monitoredVehicleJourney, blockInstance, currentVehicleTripStatus, monitoredCallStopBean, 
				presentationService, transitDataService, stopIdToPredictionRecordMap, hasRealtimeData, responseTimestamp);

		// onward calls
		if(!presentationService.isOnDetour(currentVehicleTripStatus))
			fillOnwardCalls(monitoredVehicleJourney, blockInstance, framedJourneyTripBean, currentVehicleTripStatus, onwardCallsMode,
				presentationService, transitDataService, stopIdToPredictionRecordMap, maximumOnwardCalls, hasRealtimeData, responseTimestamp);

		// situations
		fillSituations(monitoredVehicleJourney, currentVehicleTripStatus);

		return;
	}

	private static void fillOccupancy(MonitoredVehicleJourneyStructure mvj, TransitDataService tds, TripStatusBean tripStatus) {
		if (tripStatus == null
				|| tripStatus.getActiveTrip() == null
				|| tripStatus.getActiveTrip().getRoute() ==  null
				|| tripStatus.getVehicleId() == null) {
			return;
		}
		VehicleOccupancyRecord vor =
				tds.getVehicleOccupancyRecordForVehicleIdAndRoute(
						AgencyAndId.convertFromString(tripStatus.getVehicleId()),
						tripStatus.getActiveTrip().getRoute().getId(),
						tripStatus.getActiveTrip().getDirectionId());
		mvj.setOccupancy(mapOccupancyStatusToEnumeration(vor));
	}

	private static OccupancyEnumeration mapOccupancyStatusToEnumeration(VehicleOccupancyRecord vor) {
		if (vor == null) return null;
		switch (vor.getOccupancyStatus()) {
			case UNKNOWN:
				return null;
			case EMPTY:
			case MANY_SEATS_AVAILABLE:
				return OccupancyEnumeration.SEATS_AVAILABLE;
			case FEW_SEATS_AVAILABLE:
			case STANDING_ROOM_ONLY:
				return OccupancyEnumeration.STANDING_AVAILABLE;
			case FULL:
			case CRUSHED_STANDING_ROOM_ONLY:
			case NOT_ACCEPTING_PASSENGERS:
				return OccupancyEnumeration.FULL;
			default:
				return null;
		}
	}

	public static String mapSiriEnumToOccupancyStatus(String siriEnum) {
		if (siriEnum == null) return null;

		if (OccupancyEnumeration.SEATS_AVAILABLE.name().equals(siriEnum))
			return OccupancyStatus.MANY_SEATS_AVAILABLE.name();
		if (OccupancyEnumeration.STANDING_AVAILABLE.name().equals(siriEnum))
			return OccupancyStatus.FEW_SEATS_AVAILABLE.name();
		if (OccupancyEnumeration.FULL.name().equals(siriEnum))
			return OccupancyStatus.FULL.name();
		return OccupancyStatus.UNKNOWN.name();
	}


	/***
	 * PRIVATE STATIC METHODS
	 */

	public static VehicleModesEnumeration toVehicleMode(String typeString) {
		VehicleModesEnumeration mode;
		if (typeString == null) {
			mode = VehicleModesEnumeration.BUS;
			return mode;
		}
		switch (typeString) {
			case "bus":
				mode = VehicleModesEnumeration.BUS;
				break;
			case "light_rail":
				mode = VehicleModesEnumeration.TRAM;
				break;
			case "rail":
				mode = VehicleModesEnumeration.RAIL;
				break;
			case "ferry":
				mode = VehicleModesEnumeration.FERRY;
				break;
			default:
				_log.error("Unknown vehicleMode " + typeString + ", defaulting to BUS");
				mode = VehicleModesEnumeration.BUS;
		}
		return mode;
	}


	private static void fillOnwardCalls(MonitoredVehicleJourneyStructure monitoredVehicleJourney, 
			BlockInstanceBean blockInstance, TripBean framedJourneyTripBean, TripStatusBean currentVehicleTripStatus, OnwardCallsMode onwardCallsMode,
			PresentationService presentationService, TransitDataService transitDataService, 
			Map<String, TimepointPredictionRecord> stopLevelPredictions, int maximumOnwardCalls, boolean hasRealtimeData, long responseTimestamp) {

		String tripIdOfMonitoredCall = framedJourneyTripBean.getId();

		monitoredVehicleJourney.setOnwardCalls(new OnwardCallsStructure());
		

		//////////

		// no need to go further if this is the case!
		if(maximumOnwardCalls == 0) { 
			return;
		}

		List<BlockTripBean> blockTrips = blockInstance.getBlockConfiguration().getTrips();

		double distanceOfVehicleAlongBlock = 0;
		int blockTripStopsAfterTheVehicle = 0; 
		int onwardCallsAdded = 0;

		boolean foundActiveTrip = false;
		for(int i = 0; i < blockTrips.size(); i++) {
			BlockTripBean blockTrip = blockTrips.get(i);

			if(!foundActiveTrip) {
				if(currentVehicleTripStatus.getActiveTrip().getId().equals(blockTrip.getTrip().getId())) {
					distanceOfVehicleAlongBlock += currentVehicleTripStatus.getDistanceAlongTrip();
					foundActiveTrip = true;
				} else {
					// a block trip's distance along block is the *beginning* of that block trip along the block
					// so to get the size of this one, we have to look at the next.
					if(i + 1 < blockTrips.size()) {
						distanceOfVehicleAlongBlock = blockTrips.get(i + 1).getDistanceAlongBlock();
					}

					// bus has already served this trip, so no need to go further
					continue;
				}
			}

			if(onwardCallsMode == OnwardCallsMode.STOP_MONITORING) {
				// always include onward calls for the trip the monitored call is on ONLY.
				if(!blockTrip.getTrip().getId().equals(tripIdOfMonitoredCall)) {
					continue;
				}
			}
			
			boolean foundMatch = false;
			
			HashMap<String, Integer> visitNumberForStopMap = new HashMap<String, Integer>();	   
			for(BlockStopTimeBean stopTime : blockTrip.getBlockStopTimes()) {
				int visitNumber = getVisitNumber(visitNumberForStopMap, stopTime.getStopTime().getStop());
				
				StopBean stop = stopTime.getStopTime().getStop();
				double distanceOfCallAlongTrip = stopTime.getDistanceAlongBlock() - blockTrip.getDistanceAlongBlock();
				double distanceOfVehicleFromCall = stopTime.getDistanceAlongBlock() - distanceOfVehicleAlongBlock;
	
				// block trip stops away--on this trip, only after we've passed the stop, 
				// on future trips, count always.
				if(currentVehicleTripStatus.getActiveTrip().getId().equals(blockTrip.getTrip().getId())) {
					
					if(!hasRealtimeData){
						
						if(stop.getId().equals(currentVehicleTripStatus.getNextStop().getId()))
							foundMatch = true;
						
						if (foundMatch){
							blockTripStopsAfterTheVehicle++;
							ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
							StopWithArrivalsAndDeparturesBean result = transitDataService.getStopWithArrivalsAndDepartures(stop.getId(), query);
							// We can't assume the first result is the correct result
							Collections.sort(result.getArrivalsAndDepartures(), new SortByTime());
							if (result.getArrivalsAndDepartures().isEmpty()) {
							  // bad data?  abort!
							  continue;
							}
							ArrivalAndDepartureBean arrivalAndDeparture = result.getArrivalsAndDepartures().get(0);
							distanceOfVehicleFromCall = arrivalAndDeparture.getDistanceFromStop();
							//responseTimestamp = arrivalAndDeparture.getScheduledArrivalTime();
						}
						else
							continue;					
					}
					else if(stopTime.getDistanceAlongBlock() >= distanceOfVehicleAlongBlock) {
						blockTripStopsAfterTheVehicle++;
					} else {
						// stop is behind the bus--no need to go further
						continue;
					}

				// future trip--bus hasn't reached this trip yet, so count all stops
				} else {
					blockTripStopsAfterTheVehicle++;
				}
				OnwardCallStructure ocs = getOnwardCallStructure(stop, presentationService,
						distanceOfCallAlongTrip,
						distanceOfVehicleFromCall,
						visitNumber, blockTripStopsAfterTheVehicle - 1,
						stopLevelPredictions.get(stopTime.getStopTime().getStop().getId()),
						hasRealtimeData, responseTimestamp,
						getScheduledArrivalTime(currentVehicleTripStatus, stopTime),
						getScheduledDepartureTime(currentVehicleTripStatus, stopTime),
						currentVehicleTripStatus.getScheduleDeviation());

				if (ocs != null)
					monitoredVehicleJourney.getOnwardCalls().getOnwardCall().add(ocs);

				onwardCallsAdded++;

				if(onwardCallsAdded >= maximumOnwardCalls) {
					return;
				}
			}

			// if we get here, we added our stops
			return;
		}

		return;
	}

	private static void fillMonitoredCall(MonitoredVehicleJourneyStructure monitoredVehicleJourney, 
			BlockInstanceBean blockInstance, TripStatusBean tripStatus, StopBean monitoredCallStopBean, 
			PresentationService presentationService, TransitDataService transitDataService,
			Map<String, TimepointPredictionRecord> stopLevelPredictions, boolean hasRealtimeData, long responseTimestamp) {

		List<BlockTripBean> blockTrips = blockInstance.getBlockConfiguration().getTrips();
		
		double distanceOfVehicleAlongBlock = 0;
		int blockTripStopsAfterTheVehicle = 0;

		boolean foundActiveTrip = false;
		for(int i = 0; i < blockTrips.size(); i++) {
			BlockTripBean blockTrip = blockTrips.get(i);

			if(!foundActiveTrip) {
				if(tripStatus.getActiveTrip().getId().equals(blockTrip.getTrip().getId())) {
					
					double distanceAlongTrip = tripStatus.getDistanceAlongTrip();
					
					if(!hasRealtimeData){
						distanceAlongTrip = tripStatus.getScheduledDistanceAlongTrip();
					}

					distanceOfVehicleAlongBlock += distanceAlongTrip;

					foundActiveTrip = true;
				} else {
					// a block trip's distance along block is the *beginning* of that block trip along the block
					// so to get the size of this one, we have to look at the next.
					if(i + 1 < blockTrips.size()) {
						distanceOfVehicleAlongBlock = blockTrips.get(i + 1).getDistanceAlongBlock();
					}

					// bus has already served this trip, so no need to go further
					continue;
				}
			}
			
			HashMap<String, Integer> visitNumberForStopMap = new HashMap<String, Integer>();
			

			for(BlockStopTimeBean stopTime : blockTrip.getBlockStopTimes()) {
				int visitNumber = getVisitNumber(visitNumberForStopMap, stopTime.getStopTime().getStop());

				// block trip stops away--on this trip, only after we've passed the stop, 
				// on future trips, count always.
				if(tripStatus.getActiveTrip().getId().equals(blockTrip.getTrip().getId())) {
					if(stopTime.getDistanceAlongBlock() >= distanceOfVehicleAlongBlock) {
						blockTripStopsAfterTheVehicle++;
					} else {
						// bus has passed this stop already--no need to go further
						continue;
					}

				// future trip--bus hasn't reached this trip yet, so count all stops
				} else {
					blockTripStopsAfterTheVehicle++;
				}
								
				// monitored call
				if(stopTime.getStopTime().getStop().getId().equals(monitoredCallStopBean.getId())) {
					if(!presentationService.isOnDetour(tripStatus)) {
							MonitoredCallStructure msc = getMonitoredCallStructure(stopTime.getStopTime().getStop(), presentationService,
								stopTime.getDistanceAlongBlock() - blockTrip.getDistanceAlongBlock(),
								stopTime.getDistanceAlongBlock() - distanceOfVehicleAlongBlock,
								visitNumber, blockTripStopsAfterTheVehicle - 1,
								stopLevelPredictions.get(monitoredCallStopBean.getId()),
								hasRealtimeData,
								responseTimestamp,
								getScheduledArrivalTime(tripStatus, stopTime),
								getScheduledDepartureTime(tripStatus, stopTime),
								tripStatus.getScheduleDeviation());
							if(msc != null)
								monitoredVehicleJourney.setMonitoredCall(msc);
					}

					// we found our monitored call--stop
					return;
				}
			}    	
		}
	}

	private static long getScheduledArrivalTime(TripStatusBean tripStatus, BlockStopTimeBean stopTime){
		return tripStatus.getServiceDate() + (stopTime.getStopTime().getArrivalTime() * 1000);
	}

	private static long getScheduledDepartureTime(TripStatusBean tripStatus, BlockStopTimeBean stopTime){
		return tripStatus.getServiceDate() + (stopTime.getStopTime().getDepartureTime() * 1000);
	}

	private static void fillSituations(MonitoredVehicleJourneyStructure monitoredVehicleJourney, TripStatusBean tripStatus) {
		if (tripStatus == null || tripStatus.getSituations() == null || tripStatus.getSituations().isEmpty()) {
			return;
		}

		List<SituationRefStructure> situationRef = monitoredVehicleJourney.getSituationRef();

		for (ServiceAlertBean situation : tripStatus.getSituations()) {
			SituationRefStructure sitRef = new SituationRefStructure();
			SituationSimpleRefStructure sitSimpleRef = new SituationSimpleRefStructure();
			sitSimpleRef.setValue(situation.getId());
			sitRef.setSituationSimpleRef(sitSimpleRef);
			situationRef.add(sitRef);
		}
	}

	private static OnwardCallStructure getOnwardCallStructure(StopBean stopBean, 
			PresentationService presentationService, 
			double distanceOfCallAlongTrip, double distanceOfVehicleFromCall, int visitNumber, int index,
			TimepointPredictionRecord prediction, boolean hasRealtimeData, long responseTimestamp,
		    long scheduledArrivalTime, long scheduledDepartureTime, double scheduleDeviation) {

		boolean hasPrediction = prediction != null;
		Long predictedArrivalTime = null;
		Long predictedDepartureTime = null;

		if(hasPrediction){
			predictedArrivalTime = prediction.getTimepointPredictedArrivalTime();
			predictedDepartureTime = prediction.getTimepointPredictedDepartureTime();
		}




		OnwardCallStructure onwardCallStructure = new OnwardCallStructure();
		onwardCallStructure.setVisitNumber(BigInteger.valueOf(visitNumber));

		StopPointRefStructure stopPointRef = new StopPointRefStructure();
		stopPointRef.setValue(stopBean.getId());
		onwardCallStructure.setStopPointRef(stopPointRef);
		
		if (stopBean.getCode() != null) {
			// Agency's prefer stop code display in UI, so override platform name for this use
			NaturalLanguageStringStructure platform = new NaturalLanguageStringStructure();
			platform.setValue(stopBean.getCode());
			onwardCallStructure.setArrivalPlatformName(platform);
		}

		NaturalLanguageStringStructure stopPoint = new NaturalLanguageStringStructure();
		stopPoint.setValue(stopBean.getName());
		onwardCallStructure.setStopPointName(stopPoint);


		if (prediction != null && prediction.getScheduleRelationship() != null && prediction.isSkipped()) {
			_log.info("SKIPPED STOP: " + stopBean.getId());
			return null;
		}

		Date expectedArrivalTime = getExpectedTime(hasRealtimeData,hasPrediction,
				predictedArrivalTime, scheduledArrivalTime, scheduleDeviation, responseTimestamp);

		Date expectedDepartureTime = getExpectedTime(hasRealtimeData,hasPrediction,
				predictedDepartureTime, scheduledDepartureTime, scheduleDeviation, responseTimestamp);

		if(expectedArrivalTime.after(expectedDepartureTime)){
			expectedDepartureTime = expectedArrivalTime;
		}

		onwardCallStructure.setExpectedArrivalTime(expectedArrivalTime);
		onwardCallStructure.setExpectedDepartureTime(expectedDepartureTime);
		onwardCallStructure.setAimedArrivalTime(new Date(scheduledArrivalTime));

		// siri extensions
		SiriExtensionWrapper wrapper = new SiriExtensionWrapper();
		ExtensionsStructure distancesExtensions = new ExtensionsStructure();
		SiriDistanceExtension distances = new SiriDistanceExtension();

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setGroupingUsed(false);

		distances.setStopsFromCall(index);
		distances.setCallDistanceAlongRoute(NumberUtils.toDouble(df.format(distanceOfCallAlongTrip)));
		distances.setDistanceFromCall(NumberUtils.toDouble(df.format(distanceOfVehicleFromCall)));
		distances.setPresentableDistance(presentationService.getPresentableDistance(distances));

		wrapper.setDistances(distances);
		distancesExtensions.setAny(wrapper);    
		onwardCallStructure.setExtensions(distancesExtensions);

		return onwardCallStructure;
	}

	private static MonitoredCallStructure getMonitoredCallStructure(StopBean stopBean, 
			PresentationService presentationService, 
			double distanceOfCallAlongTrip, double distanceOfVehicleFromCall, int visitNumber, int index,
			TimepointPredictionRecord prediction, boolean hasRealtimeData, long responseTimestamp,
			long scheduledArrivalTime, long scheduledDepartureTime, double scheduleDeviation) {

		boolean hasPrediction = prediction != null;
		Long predictedArrivalTime = null;
		Long predictedDepartureTime = null;

		if(hasPrediction){
			predictedArrivalTime = prediction.getTimepointPredictedArrivalTime();
			predictedDepartureTime = prediction.getTimepointPredictedDepartureTime();
		}

		if (prediction != null && prediction.getScheduleRelationship() != null && prediction.isSkipped()) {
			_log.info("SKIPPED STOP (MONITORED): " + stopBean.getId());
			return null;
		}

		MonitoredCallStructure monitoredCallStructure = new MonitoredCallStructure();
		monitoredCallStructure.setVisitNumber(BigInteger.valueOf(visitNumber));

		StopPointRefStructure stopPointRef = new StopPointRefStructure();
		stopPointRef.setValue(stopBean.getId());
		monitoredCallStructure.setStopPointRef(stopPointRef);

		NaturalLanguageStringStructure stopPoint = new NaturalLanguageStringStructure();
		stopPoint.setValue(stopBean.getName());
		monitoredCallStructure.setStopPointName(stopPoint);

		Date expectedArrivalTime = getExpectedTime(hasRealtimeData,hasPrediction,
				predictedArrivalTime, scheduledArrivalTime, scheduleDeviation, responseTimestamp);

		Date expectedDepartureTime = getExpectedTime(hasRealtimeData,hasPrediction,
				predictedDepartureTime, scheduledDepartureTime, scheduleDeviation, responseTimestamp);

		if(expectedArrivalTime.after(expectedDepartureTime)){
			expectedDepartureTime = expectedArrivalTime;
		}

		monitoredCallStructure.setExpectedArrivalTime(expectedArrivalTime);
		monitoredCallStructure.setExpectedDepartureTime(expectedDepartureTime);

        //setting the scheduled arrival time.
        if (monitoredCallStructure.getExpectedArrivalTime()!= null) {
            monitoredCallStructure.setAimedArrivalTime(new Date(scheduledArrivalTime));
        }

		// siri extensions
		SiriExtensionWrapper wrapper = new SiriExtensionWrapper();
		ExtensionsStructure distancesExtensions = new ExtensionsStructure();
		SiriDistanceExtension distances = new SiriDistanceExtension();

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setGroupingUsed(false);

		distances.setStopsFromCall(index);
		distances.setCallDistanceAlongRoute(NumberUtils.toDouble(df.format(distanceOfCallAlongTrip)));
		distances.setDistanceFromCall(NumberUtils.toDouble(df.format(distanceOfVehicleFromCall)));		
		distances.setPresentableDistance(presentationService.getPresentableDistance(distances));

        long deviation = 0L;
        if (monitoredCallStructure.getExpectedArrivalTime() != null &&
                monitoredCallStructure.getAimedArrivalTime() != null) {
            //get schedule deviation in milliseconds
            long deviationSeconds = monitoredCallStructure.getExpectedArrivalTime().getTime() -
                    monitoredCallStructure.getAimedArrivalTime().getTime();
            deviation = Math.round(deviationSeconds/(1000.0 * 60.0));
        }

        wrapper.setDeviation(String.valueOf(deviation));
        wrapper.setDistances(distances);
		distancesExtensions.setAny(wrapper);
		monitoredCallStructure.setExtensions(distancesExtensions);

		return monitoredCallStructure;
	}

	private static Date getExpectedTime(boolean hasRealtimeData,
											   boolean hasPrediction,
											   Long predictedTime,
											   long scheduledTime,
											   double scheduleDeviation,
											   long responseTimestamp){
		if(hasRealtimeData) {
			if (!hasPrediction || predictedTime < responseTimestamp) {
				// we have a bad prediction, try schedule deviation + schedule and see if that's better
				// TODO FIXME!!! If this is due to terminal arrival / departure of next trip, this should
				// be fixed upstream
				long timeWithDeviation = scheduledTime + (long) scheduleDeviation * 1000;

				if (timeWithDeviation < responseTimestamp) {
					return new Date(responseTimestamp + 1000);
				} else {
					return new Date(timeWithDeviation);
				}

			} else {
				return new Date(predictedTime);
			}
		} else {
			return new Date(scheduledTime);
		}
	}

	private static int getVisitNumber(HashMap<String, Integer> visitNumberForStop, StopBean stop) {
		int visitNumber;

		if (visitNumberForStop.containsKey(stop.getId())) {
			visitNumber = visitNumberForStop.get(stop.getId()) + 1;
		} else {
			visitNumber = 1;
		}

		visitNumberForStop.put(stop.getId(), visitNumber);

		return visitNumber;
	}

	private static ProgressRateEnumeration getProgressRateForPhaseAndStatus(String status, String phase) {
		if (phase == null) {
			return ProgressRateEnumeration.UNKNOWN;
		}

		if (phase.toLowerCase().startsWith("layover")
				|| phase.toLowerCase().startsWith("deadhead")
				|| phase.toLowerCase().equals("at_base")) {
			return ProgressRateEnumeration.NO_PROGRESS;
		}

		if (status != null && status.toLowerCase().equals("stalled")) {
			return ProgressRateEnumeration.NO_PROGRESS;
		}

		if (phase.toLowerCase().equals("in_progress")) {
			return ProgressRateEnumeration.NORMAL_PROGRESS;
		}

		return ProgressRateEnumeration.UNKNOWN;
	}
	
	public static class SortByTime implements Comparator<ArrivalAndDepartureBean> {
	    public int compare(ArrivalAndDepartureBean o1, ArrivalAndDepartureBean o2) {
	      long a = o1.computeBestDepartureTime();
	      long b = o2.computeBestDepartureTime();
	      return a == b ? 0 : (a < b ? -1 : 1);
	    }
	}
}
