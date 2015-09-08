[Back to API parent page](../index.html)

# The &lt;tripDetails/&gt; Element

The `<tripDetails/>` element captures extended information about a particular
trip, potentially including the trip instance information, schedule, status, and
active service alert information.  It is returned as a sub-element in a number of API calls.  For example:

* [trip-details](../methods/trip-details.html)
* [trip-for-vehicle](../methods/trip-for-vehicle.html)
* [trips-for-location](../methods/trips-for-location.html)
* [trips-for-route](../methods/trips-for-route.html)

## Example

~~~
<tripId>1_12540399</tripId>
<serviceDate>1271401200000</serviceDate>
<frequency>...</frequency>
<status>...</status>
<schedule>
  <timeZone>America/Los_Angeles</timeZone>
  <stopTimes>
    <tripStopTime>
      <arrivalTime>81706</arrivalTime>
      <departureTime>82620</departureTime>
      <stopId>1_18085</stopId>
    </tripStopTime>
    <tripStopTime>...</tripStopTime>
  </stopTimes>
  <previousTripId>1_12541128</previousTripId>
  <nextTripId>1_14469030</nextTripId>
</schedule>
<situationIds>
  <string>1_1289973261968</string>
</situationIds>
~~~

## Details

We start with the `tripId` for the trip, which can be used to look up the
referenced [`<trip/>` element](trip.html) in the `<references/>`
section.

Next we have optional trip instance information, including the `<serviceDate/>`
and optional [`<frequency/>` element](frequency.html).

The optional `<status/>` element captures real-time information about the trip,
where applicable.  For more details on the `<status/>` element, see the [`<tripStatus/>`](trip-status.html).

The `<schedule/>` section, which includes the following elements:

* timeZone - the id of the default time zone for this trip
* stopTimes - specific details about which stops are visited during the course of the trip and at what times.  See `<tripStopTime/>` below for more info.
* previousTripId - if this trip is part of a block and has an incoming trip from another route, this element will give the id of the incoming trip
* nextTrip - if this trip is part of a block and has an outgoing trip to another route, this element will give the id of the outgoing trip

Details about the `<tripStopTime/>` element.  Note that arrival and departure times are the scheduled times and do not reflect real-time arrival information, even if it's available:

* arrivalTime - time, in seconds since the start of the service date, when the trip arrives at the specified stop
* departureTime - time, in seconds since the start of the service date, when the trip arrives at the specified stop
* stopId - the stop id of the stop visited during the trip

Finally, the `<situationIds/>` element contains ids for any active [`<situation/>` elements](situation.html) that currently apply to the trip.
