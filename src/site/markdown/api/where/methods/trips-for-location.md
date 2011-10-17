[Back to API parent page](../index.html)

# Method: trips-for-location

Search for active trips near a specific location

## Sample Request

http://api.onebusaway.org/api/where/trips-for-location.xml?key=TEST&lat=47.653&lon=-122.307&latSpan=0.008&lonSpan=0.008

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="listWithReferences">
        <references>...</references>
        <list>
          <tripDetails>
            <tripId>1_11259712</tripId>
            <status>
              <serviceDate>1271401200000</serviceDate>
              <position>
                <lat>47.65325024742268</lat>
                <lon>-122.30605031958763</lon>
              </position>
              <predicted>false</predicted>
              <scheduleDeviation>0</scheduleDeviation>
            </status>
          </tripDetails>
        </list>
        <limitExceeded>false</limitExceeded>
      </data>
    </response>

## Request Parameters

* lat - The latitude coordinate of the search center
* lon - The longitude coordinate of the search center
* latSpan/lonSpan - Set the limits of the search bounding box
* includeTrips - Can be true/false to determine whether full [<trip/>](../elements/trip.html) elements are included in the `<references/>` section.  Defaults to false.
* includeSchedules - Can be true/false to determine whether full `<schedule/>` elements are included in the `<tripDetails/>` section.  Defaults to false.

## Response

The set of active trips in the current search area.  Active trips are ones where the transit vehicle is currently located within the search radius.  We use real-time arrival data to determine the position of transit vehicles when available, otherwise we determine the location of vehicles from the static schedule.  For documentation on the `<tripDetails/>` element, see the documentation for the [trip-details api call](trip-details.html).