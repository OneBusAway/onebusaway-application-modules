[Back to API parent page](../index.html)

# The &lt;arrivalAndDeparture/&gt; Element

The `<arrivalAndDeparture/>` element captures information about the arrival and departure of a transit vehicle at a transit stop.  The element is returned as a sub-element in the following api methods:

* [arrivals-and-departures-for-stop](../methods/arrivals-and-departures-for-stop.html)
* [arrival-and-departure-for-stop](../methods/arrival-and-departure-for-stop.html)

## Example

    <arrivalAndDeparture>
      <routeId>1_65</routeId>
      <tripId>1_15551341</tripId>
      <serviceDate>1291536000000</serviceDate>
      <stopId>1_75403</stopId>
      <stopSequence>42</stopSequence>
      <blockTripSequence>2</blockTripSequence>
      <routeShortName>65</routeShortName>
      <routeLongName>...</routeLongName>
      <tripHeadsign>UNIVERSITY DISTRICT</tripHeadsign>
      <arrivalEnabled>true</arrivalEnabled>
      <departureEnabled>true</departureEnabled>
      <scheduledArrivalTime>1291581547000</scheduledArrivalTime>
      <scheduledDepartureTime>1291581547000</scheduledDepartureTime>
      <frequency>...</frequency>
      <predicted>true</predicted>
      <predictedArrivalTime>1291581546000</predictedArrivalTime>
      <predictedDepartureTime>1291581546000</predictedDepartureTime>
      <distanceFromStop>7982.740408789774</distanceFromStop>
      <numberOfStopsAway>31</numberOfStopsAway>
      <tripStatus>...</tripStatus>
    </arrivalAndDeparture>

## Details

* routeId - the route id for the arriving vehicle
* tripId - the trip id for the arriving vehicle
* serviceDate - time, in ms since the unix epoch, of midnight for start of the service date for the trip.
* stopId - the stop id of the stop the vehicle is arriving at
* stopSequence - the index of the stop into the sequence of stops that make up the trip for this arrival. This value is 0-indexed, and is generated internally by OneBusAway (it is not the GTFS stop_sequence). The first stop in the trip will always have stopSequence = 0, while the last stop in the trip will always have stopSequence = totalStopsInTrip - 1.
* totalStopsInTrip - the total number of stops visited on the trip for this arrival. If the same stop is visited more than once in this trip, each visitation is counted towards the total.
* blockTripSequence - the index of this arrival's trip into the sequence of trips for the active block.  Compare to `blockTripSequence` in the [OneBusAwayRestApi_TripStatusElementV2 tripStatus element] to determine where the arrival-and-departure is on the block in comparison to the active block location.
* routeShortName - the route short name that potentially overrides the route short name in the referenced [`<route/>` element](route.html) - *OPTIONAL*
* routeLongName - the route long name that potentially overrides the route long name in the referenced [`<route/>` element](route.html) - *OPTIONAL*
* tripHeadsign - the trip headsign that potentially overrides the trip headsign in the referenced [`<trip/>` element](trip.html) - *OPTIONAL*
* arrivalEnabled - true if this transit vehicle is one that riders could arrive on
* departureEnabled - true if this transit vehicle is one that riders can depart on
* scheduledArrivalTime - scheduled arrival time, ms since unix epoch
* scheduledDepartureTime - scheduled departure time, ms since unix epoch
* frequency - information about [frequency based scheduling](frequency.html), if applicable to the trip - *OPTIONAL*
* predicted - true if we have real-time arrival info available for this trip
* predictedArrivalTime - predicted arrival time, ms since unix epoch, zero if no real-time available
* predictedDepartureTime - predicted departure time, ms since unix epoch, zero if no real-time available
* distanceFromStop - distance of the arriving transit vehicle from the stop, in meters
* numberOfStopsAway - the number of stops between the arriving transit vehicle and the current stop (doesn't include the current stop)
* tripStatus - [`<tripStatus/>` element](trip-status.html) giving trip-specific status for the arriving transit vehicle - *OPTIONAL*

## Notes

It's important to note that the active trip contained in the `<tripStatus/>` element may be different than the `tripId` specified in the `<arrivalAndDeparture/>`, especially in the case of blocks of trips where the vehicle is currently servicing the previous trip that will link to the trip that will ultimately service the current stop.

In order to support frequency-based scheduling in legacy clients, we will set the `scheduledArrivalTime` and `scheduledDepartureTime` to be the current time + the headway for frequency-based scheduled trips.
