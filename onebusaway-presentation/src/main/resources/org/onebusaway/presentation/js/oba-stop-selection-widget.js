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