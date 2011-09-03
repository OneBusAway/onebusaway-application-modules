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
var OBA = window.OBA || {};

/*******************************************************************************
 * Stop Selection Widget
 ******************************************************************************/

OBA.StopSelectionWidget = function(parentElement, params) {

	var that = {};
	
	params = params || {};
	
	/****
	 * Wire up the UI
	 ****/
	
	var content = jQuery('.StopSelectionWidgetTemplate').clone();
	content.removeClass('StopSelectionWidgetTemplate');
	content.addClass('StopSelectionWidget');
	
	content.show();
	content.appendTo(parentElement);
	
	var mapElement = content.find('.map');
	var map = OBA.Maps.map(mapElement);
	
	var transitMapParams = {};
	if( params.stopClickHandler )
		transitMapParams.stopClickHandler = params.stopClickHandler;
	
	var transitMap = OBA.TransitMap(map, transitMapParams);
	
	if ( params.routeId ) {
		if( params.directionId )
			transitMap.showStopsForRouteAndDirectionId(params.routeId, params.directionId);
		else
			transitMap.showStopsForRouteId(params.routeId);
	}
	

	/***************************************************************************
	 * Public Methods
	 **************************************************************************/
	
	that.map = map;
	
	return that;
};