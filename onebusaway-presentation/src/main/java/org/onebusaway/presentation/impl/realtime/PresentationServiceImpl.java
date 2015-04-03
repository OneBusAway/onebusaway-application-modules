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
package org.onebusaway.presentation.impl.realtime;

import org.onebusaway.presentation.services.realtime.PresentationService;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data_federation.siri.SiriDistanceExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A class to encapsulate agency-specific front-end configurations and display conventions.
 * @author jmaki
 *
 */
@Component
public class PresentationServiceImpl implements PresentationService {

  private static Logger _log = LoggerFactory.getLogger(PresentationServiceImpl.class);

  private Long _now = null;

  private int _atStopThresholdInFeet;
  private int _approachingThresholdInFeet;
  private int _distanceAsStopsThresholdInFeet;
  private int _distanceAsStopsThresholdInStops;
  private int _distanceAsStopsMaximumThresholdInFeet;
  private int _expiredTimeout;
  private float _previousTripFilterDistanceMiles;

  public void setAtStopThresholdInFeet(int atStopThresholdInFeet) {
    _atStopThresholdInFeet = atStopThresholdInFeet;
  }

  public void setApproachingThresholdInFeet(int approachingThresholdInFeet) {
    _approachingThresholdInFeet = approachingThresholdInFeet;
  }

  public void setDistanceAsStopsThresholdInFeet(int distanceAsStopsThresholdInFeet) {
    _distanceAsStopsThresholdInFeet = distanceAsStopsThresholdInFeet;
  }

  public void setDistanceAsStopsThresholdInStops(int distanceAsStopsThresholdInStops) {
    _distanceAsStopsThresholdInStops = distanceAsStopsThresholdInStops;
  }

  public void setDistanceAsStopsMaximumThresholdInFeet(int distanceAsStopsMaximumThresholdInFeet) {
    _distanceAsStopsMaximumThresholdInFeet = distanceAsStopsMaximumThresholdInFeet;
  }
  
  public void setExpiredTimeout(int expiredTimeout) {
    _expiredTimeout = expiredTimeout;
  }
  
  public void setPreviousTripFilterDistanceMiles(int previousTripFilterDistanceMiles) {
    _previousTripFilterDistanceMiles = previousTripFilterDistanceMiles;
  }

  @Override
  public void setTime(long time) {
    _now = time;
  }

  public long getTime() {
    if(_now != null)
      return _now;
    else
      return System.currentTimeMillis();
  }

  @Override
  public Boolean isInLayover(TripStatusBean statusBean) {
    if(statusBean != null) {
      String phase = statusBean.getPhase();

      if (phase != null &&
          (phase.toUpperCase().equals("LAYOVER_DURING") || phase.toUpperCase().equals("LAYOVER_BEFORE"))) {
        return true;
      } else
        return false;
    }

    return null;
  }

  @Override
  public Boolean isBlockLevelInference(TripStatusBean statusBean) {
    if(statusBean != null) {
      String status = statusBean.getStatus();

      if(status != null)
        return status.contains("blockInf");
      else
        return false;
    }

    return null;
  }
  
  @Override
  public Boolean isOnDetour(TripStatusBean statusBean) {
    if(statusBean != null) {
      String status = statusBean.getStatus();

      if(status != null)
        return status.contains("deviated");
      else
        return false;
    }

    return null;
  }
  
  @Override
  public String getPresentableDistance(SiriDistanceExtension distances) {
    return getPresentableDistance(distances, "approaching", "stop", "stops", "mile", "miles", "away");
  }
  
  @Override
  public String getPresentableDistance(SiriDistanceExtension distances, String approachingText, 
      String oneStopWord, String multipleStopsWord, String oneMileWord, String multipleMilesWord, String awayWord) {

    String r = "";
    
    // meters->feet
    double feetAway = distances.getDistanceFromCall() * 3.2808399;

    if(feetAway < _atStopThresholdInFeet) {
      r = "at " + oneStopWord;

    } else if(feetAway < _approachingThresholdInFeet) {
      r = approachingText;
    
    } else {
      if(feetAway <= _distanceAsStopsMaximumThresholdInFeet && 
          (distances.getStopsFromCall() <= _distanceAsStopsThresholdInStops 
          || feetAway <= _distanceAsStopsThresholdInFeet)) {
        
        if(distances.getStopsFromCall() == 0)
          r = "< 1 " + oneStopWord + " " + awayWord;
        else
          r = distances.getStopsFromCall() == 1
          ? "1 " + oneStopWord + " " + awayWord
              : distances.getStopsFromCall() + " " + multipleStopsWord + " " + awayWord;

      } else {
        double milesAway = (float)feetAway / 5280;
        r = String.format("%1.1f " + multipleMilesWord + " " + awayWord, milesAway);
      }
    }
    
    return r;
  }
  
  /**
   * Filter logic: these methods determine which buses are shown in different request contexts. By 
   * default, OBA reports all vehicles both scheduled and tracked, which one may or may not want.
   */
  
  /***
   * These rules are common to vehicles coming to both SIRI SM and VM calls. 
   */
  @Override
  public boolean include(TripStatusBean statusBean) {
    if(statusBean == null)
      return false;

    _log.debug(statusBean.getVehicleId() + " running through filter: ");
    
    // hide non-realtime
    if(statusBean.isPredicted() == false) {
      _log.debug("  " + statusBean.getVehicleId() + " filtered out because is not realtime.");
      return false;
    }

    if(statusBean.getVehicleId() == null || statusBean.getPhase() == null) {
      _log.debug("  " + statusBean.getVehicleId() + " filtered out because phase or vehicle id is null.");
      return false;
    }

    if(Double.isNaN(statusBean.getDistanceAlongTrip())) {
      _log.debug("  " + statusBean.getVehicleId() + " filtered out because D.A.T. is NaN.");
      return false;
    }

    // TEMPORARY MTA THING FOR BX-RELEASE
    // hide buses that are on detour from a-d queries
    if(isOnDetour(statusBean))
      return false;
    
    // not in-service
    String phase = statusBean.getPhase();
    if(phase != null 
        && !phase.toUpperCase().equals("IN_PROGRESS")
        && !phase.toUpperCase().equals("LAYOVER_BEFORE") 
        && !phase.toUpperCase().equals("LAYOVER_DURING")) {
      _log.debug("  " + statusBean.getVehicleId() + " filtered out because phase is not in progress.");      
      return false;
    }

    // disabled
    String status = statusBean.getStatus();
    if(status != null && status.toUpperCase().equals("DISABLED")) {
      _log.debug("  " + statusBean.getVehicleId() + " filtered out because it is disabled.");
      return false;
    }

    if (getTime() - statusBean.getLastUpdateTime() >= 1000 * _expiredTimeout) {
      _log.debug("  " + statusBean.getVehicleId() + " filtered out because data is expired.");
      return false;
    }

    return true;
  }
  
  /***
   * These rules are just for SIRI SM calls. 
   */
  @Override
  public boolean include(ArrivalAndDepartureBean adBean, TripStatusBean status) {
    if(adBean == null || status == null)
      return false;
    
    // hide buses that left the stop recently
    if(adBean.getDistanceFromStop() < 0)
      return false;
    
    // hide buses that are on detour from a-d queries
    if(isOnDetour(status))
      return false;

    // wrap-around logic
    String phase = status.getPhase();
    TripBean activeTrip = status.getActiveTrip();
    TripBean adTripBean = adBean.getTrip();

    if(isBlockLevelInference(status)) {
    	// if ad is not on the trip this bus is on, or the previous trip, filter out
    	if(!adTripBean.getId().equals(activeTrip.getId()) 
    			&& !(adBean.getBlockTripSequence() - 1 == status.getBlockTripSequence())) {
		  _log.debug("  " + status.getVehicleId() + " filtered out due to trip block sequence");
		  return false;
		}
    	
    	// only buses that are on the same or previous trip as the a-d make it to this point:
    	if(activeTrip != null
          && !adTripBean.getId().equals(activeTrip.getId())) {
    		
  	      double distanceAlongTrip = status.getDistanceAlongTrip();
  	      double totalDistanceAlongTrip = status.getTotalDistanceAlongTrip();

  	      double distanceFromTerminalMeters = totalDistanceAlongTrip - distanceAlongTrip;

  	      if(distanceFromTerminalMeters > (_previousTripFilterDistanceMiles * 1609)) {
  	    	  _log.debug("  " + status.getVehicleId() + " filtered out due to distance from terminal on prev. trip");
		      return false;
  	      }
		}
    	
    	// filter out buses that are in layover at the beginning of the previous trip
    	if(phase != null && 
	        (phase.toUpperCase().equals("LAYOVER_BEFORE") || phase.toUpperCase().equals("LAYOVER_DURING"))) {
	
	      double distanceAlongTrip = status.getDistanceAlongTrip();
	      double totalDistanceAlongTrip = status.getTotalDistanceAlongTrip();
	      double ratio = distanceAlongTrip / totalDistanceAlongTrip;
	      
	      if(activeTrip != null
	            && !adTripBean.getId().equals(activeTrip.getId()) 
	            && ratio < 0.50) {
	        _log.debug("  " + status.getVehicleId() + " filtered out due to beginning of previous trip");
	        return false;
	      }
	    }
    } else {
	    /**
	     * So this complicated thing-a-ma-jig is to filter out buses that are at the terminals
	     * when considering arrivals and departures for a stop.
	     * 
	     * The idea is that we label all buses that are in layover "at terminal" headed towards us, then filter 
	     * out ones where that isn't true. The ones we need to specifically filter out are the ones that
	     * are at the other end of the route--the other terminal--waiting to begin service on the trip
	     * we're actually interested in.
	     * 
	     * Consider a route with terminals A and B:
	     * A ----> B 
	     *   <----
	     *   
	     * If we request arrivals and departures for the first trip from B to A, we'll see buses within a block
	     * that might be at A waiting to go to B (trip 2), if the vehicle's block includes a trip from B->A later on. 
	     * Those are the buses we want to filter out here.  
	     */

    	// only consider buses that are in layover
	    if(phase != null && 
	        (phase.toUpperCase().equals("LAYOVER_BEFORE") || phase.toUpperCase().equals("LAYOVER_DURING"))) {
	
	      double distanceAlongTrip = status.getDistanceAlongTrip();
	      double totalDistanceAlongTrip = status.getTotalDistanceAlongTrip();
	      double ratio = distanceAlongTrip / totalDistanceAlongTrip;
	      
	      // if the bus isn't serving the trip this arrival and departure is for AND 
	      // the bus is NOT on the previous trip in the block, but at the end of that trip (ready to serve
	      // the trip this arrival and departure is for), filter that out.
	      if(activeTrip != null
	            && !adTripBean.getId().equals(activeTrip.getId())
	            && !((adBean.getBlockTripSequence() - 1) == status.getBlockTripSequence() && ratio > 0.50)) {
	        _log.debug("  " + status.getVehicleId() + " filtered out due to at terminal/ratio");
	        return false;
	      }
	    } else {
	      // if the bus isn't serving the trip this arrival and departure is for, filter out--
	      // since the bus is not in layover now.
	      if (activeTrip != null
	          && !adTripBean.getId().equals(activeTrip.getId())) {
	        _log.debug("  " + status.getVehicleId() + " filtered out due to trip " + activeTrip.getId() + " not serving trip for A/D " + adTripBean.getId());
	        return false;
	      }
	    }
    }
    
    return true;
  }

}