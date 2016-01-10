[Back to API parent page](../index.html)

# Method: trip

Get details of a specific trip by id

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/trip/1_12540399.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="entryWithReferences">
        <references>...</references>
        <entry class="trip">
          <id>1_12540399</id>
          <routeId>1_44</routeId>
          <tripShortName>LOCAL</tripShortName>
          <tripHeadsign>Downtown via University District</tripHeadsign>
          <serviceId>1_114-115-WEEK</serviceId>
          <shapeId>1_20044006</shapeId>
          <directionId>1</directionId>
        </entry>
      </data>
    </response>

## Request Parameters

* id - the id of the trip, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/trip/[ID GOES HERE].xml`

## Response

See details about the various properties of the [`<trip/>` element](../elements/trip.html).
