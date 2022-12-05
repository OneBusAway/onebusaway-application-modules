[Back to API parent page](../index.html)

# Method: arrivals-and-departures-for-location

Get current arrivals and departures for stops identified by location

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/arrivals-and-departures-for-location.xml?key=TEST&amp;lat=47.653&amp;lon=-122.307&amp;latSpan=0.008&amp;lonSpan=0.008

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="listWithReferences">
        <references>...</references>
        <entry class="stopsWithArrivalsAndDepartures">
          <stopIds>
            <string>1_75403</string>
          </stopIds>
          <arrivalsAndDepartures>
            <arrivalAndDeparture>...</arrivalAndDeparture>
            <arrivalAndDeparture>...</arrivalAndDeparture>
            <arrivalAndDeparture>...</arrivalAndDeparture>
          </arrivalsAndDepartures>
          <nearbyStopIds>
            <stopWithDistance>
              <stopId>1_75414</stopId>
              <distanceFromQuery>63.30975615515593</distanceFromQuery>
            </stopWithDistance>
          </nearbyStopIds>
          <limitExceeded>false</limitExceeded>
        </entry>
      </data>
    </response>

## Request Parameters

* id - the stop id, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/arrivals-and-departures-for-location.xml`
* lat - The latitude coordinate of the search center
* lon - The longitude coordinate of the search center
* latSpan/lonSpan - Set the limits of the search bounding box
* time - by default, the method returns the status of the system right now.  However, the system
  can also be queried at a specific time.  This can be useful for testing.  See [timestamps](../index.html#Timestamps)
  for details on the format of the `time` parameter.
* maxCount - The size of the list of nearbyStopIds, arrivalsAndDepartures, and stops to return.  Defaults to 250, can be up to 1000.
* routeType - optional list of GTFS routeTypes to filter by (comma delimited)


## Response

The response is primarily composed of [`<arrivalAndDeparture/>` elements](../elements/arrival-and-departure.html),  so see the element documentation for specific details.

The nearby stop list is designed to capture stops that are very close by (like across the street) for quick navigation.  This is sorted by distance and truncated to maxCount.

Trips will not show up in the results if the schedule_relationship is SKIPPED.