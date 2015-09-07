[Back to API parent page](../index.html)

# Method: stop

Retrieve info for a specific stop by id

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/stop/1_75403.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="entryWithReferences">
        <references>...</references>
        <entry class="stop">
          <id>1_75403</id>
          <lat>47.6543655</lat>
          <lon>-122.305206</lon>
          <direction>S</direction>
          <name>Stevens Way &amp; BENTON LANE</name>
          <code>75403</code>
          <locationType>0</locationType>
          <routeIds>
            <string>1_31</string>
            <string>...</string>
          </routeIds>
        </entry>
      </data>
    </response>

## Request Parameters

* `id` - the id of the requested stop, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/stop/[ID GOES HERE].xml` 

## Response

See details about the various properties of the [`<stop/>` element](../elements/stop.html).