[Back to API parent page](../index.html)

# The &lt;tripStatus/&gt; Element

The `<tripStatus/>` element captures information about the current status of a transit vehicle serving a trip.  It is returned as a sub-element in a number of API calls.  For example:

* [arrivals-and-departures-for-stop](../methods/arrivals-and-departures-for-stop.html)
* Any method that returns a [`<tripDetails/>` element](trip-details.html).

## Example

~~~
<status>
  <activeTripId>1_15436438</activeTripId>
  <blockTripSequence>2</blockTripSequence>
  <serviceDate>1271401200000</serviceDate>
  <scheduledDistanceAlongTrip>14689.488542526902</scheduledDistanceAlongTrip>
  <totalDistanceAlongTrip>16801.282798624055</totalDistanceAlongTrip>
  <position>
    <lat>47.66166765182482</lat>
    <lon>-122.34439975182481</lon>
  </position>
  <orientation>202.79602796003044</orientation>
  <closestStop>1_29530</closestStop>
  <closestStopTimeOffset>-10</closestStopTimeOffset>
  <nextStop>1_1108</nextStop>
  <nextStopTimeOffset>72</nextStopTimeOffset>
  <occupancyStatus>MANY_SEATS_AVAILABLE</occupancyStatus>
  <phase>in_progress</phase>
  <status>default</status>
  <predicted>true</predicted>
  <lastUpdateTime>1289590007894</lastUpdateTime>
  <lastLocationUpdateTime>1289590007894</lastLocationUpdateTime>
  <lastKnownLocation>
    <lat>47.66166765182482</lat>
    <lon>-122.34439975182481</lon>
  </lastKnownLocation>
  <lastKnownOrientation>230.30</lastKnownOrientation>  
  <distanceAlongTrip>13756.12</distanceAlongTrip>
  <scheduleDeviation>13</scheduleDeviation>
  <vehicleId>1_4207</vehicleId>
  <situationIds>
    <string>1_1289972789869</string>
    <string>...</string>
  </situationIds>
</status>
~~~

## Details

* activeTripId - the trip id of the trip the vehicle is actively serving.  All trip-specific values will be in reference to this active trip
* blockTripSequence - the index of the active trip into the sequence of trips for the active block.  Compare to `blockTripSequence` in the [`<arrivalAndDeparture/>` element](arrival-and-departure.html) to determine where the active block location is relative to an arrival-and-departure.
* serviceDate - time, in ms since the unix epoch, of midnight for start of the service date for the trip.
* frequency - information about [frequency based scheduling](frequency.html), if applicable to the trip - *OPTIONAL*
* scheduledDistanceAlongTrip - the distance, in meters, the transit vehicle is scheduled to have progress along the active trip.  This is an optional value, and will only be present if the trip is in progress. *OPTIONAL*
* totalDistanceAlongTrip - the total length of the trip, in meters
* position - current position of the transit vehicle. This element is optional, and will only be present if the trip is actively running. If real-time arrival data is available, the position will take that into account, otherwise the position reflects the scheduled position of the vehicle. *OPTIONAL*
* orientation - the orientation of the transit vehicle, as an angle in degrees.  Here, 0º is east, 90º is north, 180º is west, and 270º is south.  This is an optional value that may be extrapolated from other data. *OPTIONAL*
* closestStop - the id of the closest stop to the current location of the transit vehicle, whether from schedule or real-time predicted location data
* closestStopTimeOffset - the time offset, in seconds, from the closest stop to the current position of the transit vehicle among the stop times of the current trip. If the number is positive, the stop is coming up. If negative, the stop has already been passed. 
* nextStop and nextStopTimeOffset - these are similar to the existing closestStop and closestStopTimeOffset, except that it always captures the next stop, not the closest stop.  Optional, as a vehicle may have progressed past the last stop in a trip. *OPTIONAL*
* occupancyStatus - name() values of GTFS-RT OccupancyStatus enum if available
* phase - the current journey phase of the trip (more docs to come)
* status - status modifiers for the trip (more docs to come)
* predicted - true if we have real-time arrival info available for this trip
* lastUpdateTime - the last known real-time update from the transit vehicle.  Will be zero if we haven't heard anything from the vehicle.
* lastLocationUpdateTime - the last known real-time *location* update from the transit vehicle.  This is different from `lastUpdateTime` in that it reflects the last know location update.  An update from a vehicle might not contain location info, which means this field will not be updated.  Will be zero if we haven't had a location update from the vehicle.
* lastKnownLocation - last known location of the transit vehicle.  This differs from the existing `position` field, in that the position field is potential extrapolated forward from the last known position and other data. *OPTIONAL*
* lastKnownDistanceAlongTrip - the last known distance along trip value received in real-time from the transit vehicle. *OPTIONAL*
* lastKnownOrientation - the last known orientation value received in real-time from the transit vehicle. *OPTIONAL*
* distanceAlongTrip - the distance, in meters, the transit vehicle has progressed along the active trip.  This is an optional value that will only be present if the underlying AVL system supplies it and is potential extrapolated from the last known reading to the current time.
* scheduleDeviation - if real-time arrival info is available, this lists the deviation from the schedule in seconds, where positive number indicates the trip is running late and negative indicates the trips is running early. If not real-time arrival info is available, this will be zero.
* vehicleId - if real-time arrival info is available, this lists the id of the transit vehicle currently running the trip. *OPTIONAL*
* situationIds - references to [`<situation/>` elements](situation.html), for active service alerts applicable to this trip. *OPTIONAL*
