var oba_where_standard_arrival_and_departure_for_stop = function(data) {
	
	var intervalId = null;
	var notificationIntervalInMinutes = 5;
	
	
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
		alert('Go catch that bus!');
	};
	
	var handleNotification = function(entry) {
		if( jQuery('#notification_sound_input').is(':checked') ) {
			handleSound(entry);
		}
		if( jQuery('#notification_alert_input').is(':checked') ) {
			handleAlert(entry);
		}
	};
	
	
	var arrivalAndDepartureForStopCallback = function(entry) {
		var departureTime = entry.scheduledDepartureTime;
		if( entry.predictedDepartureTime )
			departureTime = entry.predictedDepartureTime;
		
		var d = new Date();
		var now = d.getTime();
		
		var t = now + notificationIntervalInMinutes * 60 * 1000;
		
		if( t >= departureTime ) {
			if( intervalId )
				clearInterval(intervalId);
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