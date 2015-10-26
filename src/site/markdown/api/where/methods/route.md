[Back to API parent page](../index.html)

# Method: route

Retrieve info for a specific route by id.

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/route/1_44.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="entryWithReferences">
        <references>...</references>
        <entry class="route">
          <id>1_44</id>
          <shortName>44</shortName>
          <longName>ballard/montlake</longName>
          <description>ballard/montlake</description>
          <type>3</type>
          <url>http://metro.kingcounty.gov/tops/bus/schedules/s044_0_.html</url>
          <agencyId>1</agencyId>
        </entry>
      </data>
    </response>

## Request Parameters

* `id` - the id of the route, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/route/[ID GOES HERE].xml`

## Response

See details about the various properties of the [`<route/>` element](../elements/route.html).

