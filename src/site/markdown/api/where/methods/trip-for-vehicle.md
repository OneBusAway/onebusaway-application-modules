[Back to API parent page](../index.html)

# Method: trip-for-vehicle

Get extended trip details for a specific transit vehicle.  That is, given a vehicle id for a transit vehicle currently operating in the field, return extended trips details about the current trip for the vehicle.

## Sample Request

http://api.onebusaway.org/api/where/trip-for-vehicle/1_4210.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="entryWithReferences">
        <references>...</references>
        <entry class="tripDetails">
          <tripId>1_15456175</tripId>
          <status>
            <serviceDate>1276671600000</serviceDate>
            <position>
              <lat>47.66873615517241</lat>
              <lon>-122.37614189655173</lon>
            </position>
            <predicted>true</predicted>
            <scheduleDeviation>147</scheduleDeviation>
            <vehicleId>1_4210</vehicleId>
            <closestStop>1_29700</closestStop>
            <closestStopTimeOffset>6</closestStopTimeOffset>
          </status>
        </entry>
      </data>
    </response>

## Request Parameters

* id - the id of the vehicle, encoded directly in the url:
    * `http://api.onebusaway.org/api/where/trip-for-vehicle/[ID GOES HERE].xml`
* includeTrip - Can be true/false to determine whether full [<trip/>](../elements/trip.html) element is included in the `<references/>` section.  Defaults to false.
* includeSchedule - Can be true/false to determine whether full `<schedule/>` element is included in the `<tripDetails/>` section.  Defaults to fale.
* includeStatus - Can be true/false to determine whether the full `<status/>` element is include in the `<tripDetails/>` section.  Defaults to true.

## Response

The `<entry/>` element is a `<tripDetails/>` element that captures extended details about a trip beyond those already captured in the [<trip/> element](../elements/trip.html).  For documentation on the `<tripDetails/>` element, see the documentation for the [trip-details api call](trip-details.html).