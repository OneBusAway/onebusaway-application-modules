[Back to API parent page](../index.html)

# Method: arrivals-and-departures-for-stop

Get current arrivals and departures for a stop identified by id

## Sample Request

http://api.onebusaway.org/api/where/arrivals-and-departures-for-stop/1_75403.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="listWithReferences">
        <references>...</references>
        <entry class="stopWithArrivalsAndDepartures">
          <stopId>1_75403</stopId>
          <arrivalsAndDepartures>
            <arrivalAndDeparture>...</arrivalAndDeparture>
            <arrivalAndDeparture>...</arrivalAndDeparture>
            <arrivalAndDeparture>...</arrivalAndDeparture>
          </arrivalsAndDepartures>
          <nearbyStopIds>
            <string>1_75414</string>
            <string>...</string>
          </nearbyStopIds>
        </entry>
      </data>
    </response>

## Request Parameters

* id - the stop id, encoded directly in the url:
    * `http://api.onebusaway.org/api/where/arrivals-and-departures-for-stop/[ID GOES HERE].xml`
* minutesBefore=n - include vehicles having arrived or departed in the previous n minutes (default=5)
* minutesAfter=n - include vehicles arriving or departing in the next n minutes (default=35)
* time=n - the time for which the schedule will be generated, as either ms since the unix epoch or of the form YYYY-MM-DD_HH-MM-SS (note this doesn't currently deal well with timezones).  The default query time is NOW.

## Response

The response is primarily composed of [`<arrivalAndDeparture/>` elements](../elements/arrival-and-departure.html),  so see the element documentation for specific details.

The nearby stop list is designed to capture stops that are very close by (like across the street) for quick navigation.