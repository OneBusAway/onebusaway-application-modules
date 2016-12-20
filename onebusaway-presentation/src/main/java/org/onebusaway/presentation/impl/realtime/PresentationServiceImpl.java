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

import javax.annotation.PostConstruct;

import org.onebusaway.container.ConfigurationParameter;
import org.onebusaway.presentation.services.realtime.PresentationService;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data_federation.siri.SiriDistanceExtension;
import org.onebusaway.util.SystemTime;
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
  
  private static final String APPROACHING_TEXT = "approaching";
  private static final String ONE_STOP_WORD = "stop";
  private static final String MULTIPLE_STOPS_WORD = "stops";
  private static final String ONE_MILE_WORD = "mile";
  private static final String MULTIPLE_MILES_WORD = "miles";
  private static final String AWAY_WORD = "away";

  private boolean _showArrivals = false;

  private Long _now = null;

  private int _atStopThresholdInFeet = 100;
  private int _approachingThresholdInFeet = 500;
  private int _distanceAsStopsThresholdInFeet = 2640;
  private int _distanceAsStopsThresholdInStops = 3;
  private int _distanceAsStopsMaximumThresholdInFeet = 2640;
  private int _expiredTimeout = 300;
  private float _previousTripFilterDistanceMiles = 5.0f;
  private boolean _includeRequiresPhase = false;
  boolean firstLoad;

  @ConfigurationParameter
  public void setAtStopThresholdInFeet(int atStopThresholdInFeet) {
    _atStopThresholdInFeet = atStopThresholdInFeet;
  }

  @ConfigurationParameter
  public void setApproachingThresholdInFeet(int approachingThresholdInFeet) {
    _approachingThresholdInFeet = approachingThresholdInFeet;
  }

  @ConfigurationParameter
  public void setDistanceAsStopsThresholdInFeet(int distanceAsStopsThresholdInFeet) {
    _distanceAsStopsThresholdInFeet = distanceAsStopsThresholdInFeet;
  }

  @ConfigurationParameter
  public void setDistanceAsStopsThresholdInStops(int distanceAsStopsThresholdInStops) {
    _distanceAsStopsThresholdInStops = distanceAsStopsThresholdInStops;
  }

  @ConfigurationParameter
  public void setDistanceAsStopsMaximumThresholdInFeet(int distanceAsStopsMaximumThresholdInFeet) {
    _distanceAsStopsMaximumThresholdInFeet = distanceAsStopsMaximumThresholdInFeet;
  }

  @ConfigurationParameter
  public void setExpiredTimeout(int expiredTimeout) {
    _expiredTimeout = expiredTimeout;
  }

  @ConfigurationParameter
  public void setPreviousTripFilterDistanceMiles(float previousTripFilterDistanceMiles) {
    _previousTripFilterDistanceMiles = previousTripFilterDistanceMiles;
  }

  @ConfigurationParameter
  /**
   * is phase required (IN_PROGRESS) for realtime data to be displayed?
   * HINT:  NYC uses this, other tend not to.
   * @param needsPhase
   */
  public void setIncludeRequiresPhase(boolean needsPhase) {
    _includeRequiresPhase = needsPhase;
  }
  
  @Override
  public void setTime(long time) {
    _now = time;
  }

  public long getTime() {
    if(_now != null)
      return _now;
    else
      return SystemTime.currentTimeMillis();
  }

  @PostConstruct
  public void start(){
    firstLoad = true;
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
    return getPresentableDistance(distances, APPROACHING_TEXT, ONE_STOP_WORD, MULTIPLE_STOPS_WORD, 
        ONE_MILE_WORD, MULTIPLE_MILES_WORD, AWAY_WORD);
  }

  @Override
  public String getPresentableDistance(Double distanceFromStop, Integer numberOfStopsAway) {
    return getPresentableDistance(distanceFromStop, numberOfStopsAway, APPROACHING_TEXT, ONE_STOP_WORD, 
          MULTIPLE_STOPS_WORD, ONE_MILE_WORD, MULTIPLE_MILES_WORD, AWAY_WORD);
  }
/*  Used by nyc
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
*/  

  @Override
  public String getPresentableDistance(SiriDistanceExtension distances, String approachingText, 
      String oneStopWord, String multipleStopsWord, String oneMileWord, String multipleMilesWord, String awayWord) {

    Double distanceFromStop = distances.getDistanceFromCall();
    Integer numberOfStopsAway = distances.getStopsFromCall();

    return getPresentableDistance(distanceFromStop, numberOfStopsAway, approachingText, oneStopWord, 
        multipleStopsWord, oneMileWord, multipleMilesWord, awayWord);
  }

  @Override
  public String getPresentableDistance(Double distanceFromStop, Integer numberOfStopsAway, String approachingText, 
        String oneStopWord, String multipleStopsWord, String oneMileWord, String multipleMilesWord, String awayWord){
    String r = "";
    
    // meters->feet
    double feetAway = distanceFromStop * 3.2808399;
    
    if(feetAway < _atStopThresholdInFeet) {
      r = "at " + oneStopWord;

    } else if(feetAway < _approachingThresholdInFeet) {
      r = approachingText;

    } else {
      if(feetAway <= _distanceAsStopsMaximumThresholdInFeet && 
          (numberOfStopsAway <= _distanceAsStopsThresholdInStops 
          || feetAway <= _distanceAsStopsThresholdInFeet)) {
        
        if(numberOfStopsAway == 0)
          r = "< 1 " + oneStopWord + " " + awayWord;
        else
          r = numberOfStopsAway == 1
          ? "1 " + oneStopWord + " " + awayWord
              : numberOfStopsAway + " " + multipleStopsWord + " " + awayWord;

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

    // always show schedule
    if(statusBean.isPredicted()) {
    	_log.debug(statusBean.getVehicleId() + " running through filter: ");
	    if(statusBean.getVehicleId() == null ) {
	      _log.debug("  " + statusBean.getVehicleId() + " filtered out because vehicle id is null.");
	      return false;
	    }

	    if(Double.isNaN(statusBean.getDistanceAlongTrip())) {
	      _log.debug("  " + statusBean.getVehicleId() + " filtered out because D.A.T. is NaN.");
	      return false;
	    }

	    // onebusaway-application-modules does not use phase!
	    if (_includeRequiresPhase  && statusBean.getPhase() == null) {
	      _log.debug("  " + statusBean.getVehicleId() + " filtered out because phase is null.");
	      return false;
	    }

	    // TEMPORARY MTA THING FOR BX-RELEASE
	    // hide buses that are on detour from a-d queries
	    if(isOnDetour(statusBean)) {
	      _log.debug("  " + statusBean.getVehicleId() + " filtered out because of detour.");
	      return false;
	    }

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
    }
    _log.debug("include passed for " + statusBean.getVehicleId());
    return true;
  }

  /***
   * These rules are just for SIRI SM calls. 
   */
  @Override
  public boolean include(ArrivalAndDepartureBean adBean, TripStatusBean status) {
	if(adBean == null || status == null) {
	  _log.debug("null info");
		return false;
	}
	
	if(status.isPredicted()) {
		if(adBean.getPredictedArrivalTime() > 0 && adBean.getPredictedArrivalTime() < getTime()
		    && adBean.getDistanceFromStop() != 0.0) {
		  _log.debug(" prediction in past drop");
			return false;
		}
	    // hide buses that are on detour from a-d queries
	    if(isOnDetour(status)) {
	      _log.debug(" detour drop");
	      return false;
	    }
	}
	else{
		if(adBean.getScheduledArrivalTime() > 0 && adBean.getScheduledArrivalTime() < getTime()) {
		  _log.debug("shcheduled arrival in past drop");
			return false;
		}
	}

    // wrap-around logic
    String phase = status.getPhase();
    TripBean activeTrip = status.getActiveTrip();
    TripBean adTripBean = adBean.getTrip();

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
    	  _log.debug("  " + status.getVehicleId() + " filtered out due to distance from terminal on prev. trip "
    	      + distanceFromTerminalMeters + " > " + (_previousTripFilterDistanceMiles * 1609));
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
    } 

    /*else {
      // if the bus isn't serving the trip this arrival and departure is for, filter out--
      // since the bus is not in layover now.
      if (activeTrip != null
          && !adTripBean.getId().equals(activeTrip.getId())) {
        _log.debug("  " + status.getVehicleId() + " filtered out due to trip " + activeTrip.getId() + " not serving trip for A/D " + adTripBean.getId());
        //return false;
      }
    }*/

    _log.debug("include ad passed for " + status.getVehicleId());
    return true;
  }

}