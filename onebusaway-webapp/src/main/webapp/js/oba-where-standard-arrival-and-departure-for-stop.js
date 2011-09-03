/*
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
var oba_where_standard_arrival_and_departure_for_stop = function(data) {
	
	var intervalId = null;
	var notificationIntervalInMinutes = 5;
	var notified = false;
	
	var rowId = 'stopId_' + data.stopId.replace(' ','_') + '-tripId_' + data.tripId.replace(' ','_');
	if( data.vehicleId )
		rowId += '-vehicleId_' + data.vehicleId.replace(' ','_');
	
	var notificationTimeInputElem = jQuery('#notification_time_input');
	
	if( notificationTimeInputElem ) {
		var changeHandler = function() {
			var value = notificationTimeInputElem.attr('value');
			var intValue = parseInt(value);
			if( ! isNaN(intValue))
				notificationIntervalInMinutes = intValue;
		};		
		notificationTimeInputElem.change(changeHandler);
		// Run the value change handler just once to get the initial value
		changeHandler();
	}
	
	var handleSound = function(entry) {
		jQuery("#jquery_jplayer").jPlayer({
	        ready: function() {
	          jQuery(this).jPlayer("setMedia", {
	            mp3: data.notificationSoundUrl
	          });
	          jQuery(this).jPlayer("play");
	        },
	        swfPath: OBA.Config.baseUrl + "/js"
	      });
	};
	
	var handleAlert = function(entry) {
		setTimeout(function() {
			alert('Go catch that bus!');
		}, 1*1000);		
	};
	
	var handleNotification = function(entry) {
		
		if( notified )
			return;
		notified = true;
		
		if( jQuery('#notification_sound_input').is(':checked') ) {
			handleSound(entry);
		}
		if( jQuery('#notification_alert_input').is(':checked') ) {
			handleAlert(entry);
		}
	};
	
	var getBestTime = function(entry) {
		var departureTime = entry.scheduledDepartureTime;
		if( entry.predictedDepartureTime )
			departureTime = entry.predictedDepartureTime;
		return departureTime;
	};
	
	var getStatusLabel = function(entry,now) {

		var predicted = entry.predictedDepartureTime;
		var scheduled = entry.scheduledDepartureTime;
		var m = OBA.Resources.ArrivalAndDepartureMessages;

		if (predicted > 0) {

		  var diff = ((predicted - scheduled) / (1000.0 * 60));
		  var minutes = Math.abs(Math.round(diff));

		  var pastTense = predicted < now;

		  if (diff < -1.5) {
		      if (pastTense)
		    	      return OBA.L10n.format(m.departedEarly,minutes);
		          else
		        	  return OBA.L10n.format(m.early,minutes);
		      } else if (diff < 1.5) {
		    	  if (pastTense)
		    		  return m.departedOnTime;
		          else
		              return m.onTime;
		      } else {
		          if (pastTense)
		        	  return OBA.L10n.format(m.departedLate,minutes);
		          else
		              return OBA.L10n.format(m.delayed,minutes);
		      }
		} else {
			return m.scheduledDeparture;
        }
	};
	
	var getStatusLabelStyle = function(entry, now) {

		var predicted = entry.predictedDepartureTime;
		var scheduled = entry.scheduledDepartureTime;

		if (predicted > 0) {

		    var diff = ((predicted - scheduled) / (1000.0 * 60));

		    if (predicted < now) {

		        if (diff < -1.5) {
		            return "arrivalStatusDepartedEarly";
		        } else if (diff < 1.5) {
		            return "arrivalStatusDepartedOnTime";
		        } else {
		            return "arrivalStatusDepartedDelayed";
		        }
		    } else {
		          if (diff < -1.5) {
		              return "arrivalStatusEarly";
		          } else if (diff < 1.5) {
		              return "arrivalStatusOnTime";
		          } else {
		              return "arrivalStatusDelayed";
		          }
		    }

		} else {
		    if (scheduled < now)
		        return "arrivalStatusDepartedNoInfo";
		    else
		        return "arrivalStatusNoInfo";
		}
	};	
	
	var getMinutesLabel = function(now,departureTime) {

		var minutes = Math.round((departureTime - now) / (1000.0 * 60.0));
	    if( Math.abs(minutes) <= 1 )
	    	return OBA.L10n.format(OBA.Resources.ArrivalAndDepartureMessages['NOW'],minutes);
	    return minutes;
	};
	
	var arrivalAndDepartureForStopCallback = function(entry) {
		
		var d = new Date();
		var now = d.getTime();
		
		var departureTime = getBestTime(entry);
		
		var minutesLabel = getMinutesLabel(now,departureTime);
		var statusLabel = getStatusLabel(entry,now);
		var statusLabelStyle = getStatusLabelStyle(entry,now);
		
		var spanElement = jQuery("tr#" + rowId + " span.statusLabel");
		spanElement.text(statusLabel);
		spanElement.removeClass().addClass('statusLabel ' + statusLabelStyle);
		
		var tdElement = jQuery("tr#" + rowId + " td.arrivalsStatusEntry");
		tdElement.text(minutesLabel);
		tdElement.removeClass().addClass('arrivalsStatusEntry ' + statusLabelStyle);
		
		var t = now + notificationIntervalInMinutes * 60 * 1000;
		
		if( t >= departureTime ) {
			handleNotification(entry);			
		}
	};
	
	var apiCall = function() {
		OBA.Api.arrivalAndDepartureForStop(data,arrivalAndDepartureForStopCallback);
	};
	
	// Make the api call once immediately
	apiCall();
	
	// And then schedule it to run every 30 seconds
	intervalId = setInterval(apiCall, 30 * 1000 );
	
};