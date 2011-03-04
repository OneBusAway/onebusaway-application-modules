var oba_where_standard_schedule = function(data) {
	
	var processEntry = function(entry) {

		var inputs = entry.find("input");
		
		jQuery.each( inputs, function() {
			processInput(entry, jQuery(this));			
		});
	};
	
	var processInput = function(entry,input) {
		
		var groupId = input.attr('data-groupid');
		if( ! groupId )
			groupId = '';
		
		input.click(function() {
			handleClickInput(entry, groupId);
		});
	};
	
	var handleClickInput = function(entry,groupId) {
		
		var minEntries = entry.find('.stopScheduleMinutes');
		
		jQuery.each(minEntries, function() {
			var minEntry = jQuery(this);
			var data = minEntry.attr('data-groupids');
			var groupIds = [];
			if( data )
				groupIds = data.split(/\s+/);
			var match = jQuery.inArray(groupId,groupIds) != -1;
			if( match )
				minEntry.addClass('highlightedTrip');
			else
				minEntry.removeClass('highlightedTrip');
		});
	};
	
	var entries = jQuery(".stopScheduleRouteEntry");
	
	jQuery.each( entries, function() {
		var entry = jQuery(this);
		processEntry(entry);
	});
};