function oba-admin-problems-trip-problem-reports() {
	
	var mapElement = jQuery("#agencies_map").get(0);
	var map = OBA.Maps.map(mapElement);
	var infoWindow = new google.maps.InfoWindow();
	var bounds = new google.maps.LatLngBounds();
	
	var handleAgencyWithCoverage = function(awc) {
		var agency = awc.agency;
		
		var point = new google.maps.LatLng(awc.lat,awc.lon);
		var markerOptions = {
		  position: point,
	      map: map
	    };

	    var marker = new google.maps.Marker(markerOptions);
	    
	    bounds.extend(point);
	    
	    var content = jQuery('.agencyInfoWindowTemplate').clone();
	    content.find("h3>a").text(agency.name);
	    content.find("h3>a").attr("href",agency.url);
	    
	    var mapUrl = "index.html#m(location)lat(" + awc.lat + ")lon(" + awc.lon + ")accuracy(4)";
	    content.find("p>a").attr("href",mapUrl);
	    
	    google.maps.event.addListener(marker, 'click', function() {
	      infoWindow.setContent(content.show().get(0));
	      infoWindow.open(map,marker);
	    });
	};

	OBA.Api.agenciesWithCoverage(function(list) {

		for( var i=0; i<list.length; i++) {
			handleAgencyWithCoverage(list[i]);
		}
		
		if( ! bounds.isEmpty() )
			map.fitBounds(bounds);
	});
};
    