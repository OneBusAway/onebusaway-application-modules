[Back to API parent page](../index.html)

# Method: schedule-for-route

Retrieve the full schedule for a route on a particular day

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/schedule-for-route/97_9.xml?key=TEST


## Sample Response
    
    <response>
    	<version>2</version>
    	<code>200</code>
    	<currentTime>1611851550617</currentTime>
    	<text>OK</text>
    	<data class="entryWithReferences">
    		<references>...</references>
    		<entry class="routeSchedule">
    			<routeId>97_9</routeId>
    			<serviceIds>
    				<serviceId>1_1</serviceId>
    				<!-- More serviceId entries... -->
    			</serviceIds>
    			<scheduleDate>1611810000000</scheduleDate>
    			<stopTripGroupings>
    				<stopTripGrouping>
    					<directionId>0</directionId>
    					<tripHeadsign>E to Boeing - W to Mukilteo</tripHeadsign>
    					<stopIds>
    						<stopId>1_33299999</stopId>
    						<!-- More stopId entries... -->
    					</stopIds>
    					<tripIds>
    						<tripId>97_556</tripId>
    						<!-- More tripId entries... -->
    					</tripIds>
    					<tripsWithStopTimes>
                            <tripWithStopTimes>
                                <TripId>97_556</TripId>
                                <scheduledStopTimes>
                                    <arrivalEnabled>true</arrivalEnabled>
                                    <arrivalTime>52260</arrivalTime>
                                    <departureEnabled>true</departureEnabled>
                                    <departureTime>52260</departureTime>
                                    <tripId>97_556</tripId>
                                </scheduleStopTime>
                            </tripWithStopTimes>
                            <!-- More tripWithStopTimes entries... -->
                        </tripsWithStopTimes>
    				</stopTripGrouping>
    				<!-- More StopTripGrouping entries... -->
    			</stopTripGroupings>
    		</entry>
    	</data>
    </response>
    

## Request Parameters

* id - the route id to request the schedule for, encoded directly in the URL:
	* `http://api.pugetsound.onebusaway.org/api/where/schedule-for-route/[ID_GOES_HERE].xml`
* date - The date for which you want to request a schedule of the format YYYY-MM-DD (optional, defaults to current date)
    * `http://api.pugetsound.onebusaway.org/api/where/schedule-for-route/[ID_GOES_HERE].json?key=[KEY]&date=[DATE-GOES-HERE]`


## Response


The intent of this response is to mimic a traditional schedule-table format for viewing a route. As such the entry includes traditional header information, as well as a section of concentrated schedule information (in the form of stopTripGroupings).

The header information is:
* `<routeId/>` - the route being looked into -  this information is presented in the format `[agency]_[routeIdentifier]`
* `<scheduleDate/>` - the date being looked at  -  the date of service in milliseconds since the Unix epoch
* `<serviceIds>` - the Service Ids which contain that route and are live on the specified date -  for more information see the [GTFS spec](http://code.google.com/transit/spec/transit_feed_specification.html)

The entry also has concentrated schedule information in the form of stopTripGroupings. Each grouping includes:
* `<directionId\>` - the direction the trips are heading -  for more information see the [GTFS spec](http://code.google.com/transit/spec/transit_feed_specification.html)
* `<tripHeadsign\>` - the trip headsign - a string indicting the destination of the trip
* `<stopIds\>` - an ordered list of stop Ids - Each id is of the format `[agency]_[stopIdentifier]`
* `<tripIds\>` - a list of trip Ids that matched by shared direction- Each trip Id is of the format `[agency]_[tripIdentifier]`


Alternate codes:
404 - returned if the route ID in the request is not found
510 - returned if the route has no schedules for the day requested


