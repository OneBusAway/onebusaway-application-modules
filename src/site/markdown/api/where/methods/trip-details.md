[Back to API parent page](../index.html)

# Method: trip-details

Get extended details for a specific trip

## Sample Request

http://api.onebusaway.org/api/where/trip-details/1_12540399.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="entryWithReferences">
        <references>...</references>
        <entry class="tripDetails">
          <tripId>1_12540399</tripId>
          <status>...</status>
            <serviceDate>1271401200000</serviceDate>
            <position>
              <lat>47.66166765182482</lat>
              <lon>-122.34439975182481</lon>
            </position>
            <predicted>true</predicted>
            <scheduleDeviation>13</scheduleDeviation>
            <vehicleId>1_4207</vehicleId>
            <closestStop>1_29530</closestStop>
            <closestStopTimeOffset>-10</closestStopTimeOffset>
          </status>
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
        </entry>
      </data>
    </response>

## Request Parameters

* id - the id of the trip, encoded directly in the url:
    * `http://api.onebusaway.org/api/where/trip-details/[ID GOES HERE].xml`
* serviceDate - the service date for the trip as unix-time in ms (optional).  Used to disambiguate different versions of the same trip.  See [Glossary#ServiceDate the glossary entry for service date].
* includeTrip - Can be true/false to determine whether full [<trip/>](../elements/trip.html) element is included in the `<references/>` section.  Defaults to true.
* includeSchedule - Can be true/false to determine whether full `<schedule/>` element is included in the `<tripDetails/>` section.  Defaults to true.
* includeStatus - Can be true/false to determine whether the full `<status/>` element is include in the `<tripDetails/>` section.  Defaults to true.

## Response

The `<entry/>` element is a `<tripDetails/>` element that captures extended details about a trip beyond those already captured in the [<trip/> element](../elements/trip.html).

We start with the `tripId` for the trip, which can be used to look up the referenced `<trip/>` element in the `<references/>` section.

For details on the `<status/>` element, see [tripStatus](../elements/trip-status.html).

Finally, the `<schedule/>` section, which includes the following elements:

* timeZone - the id of the default time zone for this trip
* stopTimes - specific details about which stops are visited during the course of the trip and at what times.  See `<tripStopTime/>` below for more info.
* previousTripId - if this trip is part of a block and has an incoming trip from another route, this element will give the id of the incoming trip
* nextTrip - if this trip is part of a block and has an outgoing trip to another route, this element will give the id of the outgoing trip

Details about the `<tripStopTime/>` element.  Note that arrival and departure times are the scheduled times and do not reflect real-time arrival information, even if it's available:

* arrivalTime - time, in seconds since the start of the service date, when the trip arrives at the specified stop
* departureTime - time, in seconds since the start of the service date, when the trip arrives at the specified stop
* stopId - the stop id of the stop visited during the trip

## Proposed Additions

<font color="red">BETA: These are proposed additions and are subject to change, even if they are available on test or production servers.</font>

In order to better support frequency-based scheduling, we propose the addition of a `<frequency/>` element to `<schedule/>` that would indicate that the schedule is frequency-based and that the stop times should be treated as relative, not absolute, arrival times.  The element would look like:

    <frequency>
      <startTime>1289579400000</startTime>
      <endTime>1289602799000</endTime>
      <headway>600</headway>
    </frequency>

We include three fields:

* `startTime` - the start time (unix timestamp) that the frequency block starts
* `endTime` - the end time (unix timestamp) that the frequency block starts
* `headway` - the frequency of service, in seconds