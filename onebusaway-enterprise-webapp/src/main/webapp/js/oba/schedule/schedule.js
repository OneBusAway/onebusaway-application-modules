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