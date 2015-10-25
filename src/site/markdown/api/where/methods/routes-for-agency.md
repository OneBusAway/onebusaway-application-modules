[Back to API parent page](../index.html)

# Method: routes-for-agency

Retrieve the list of all routes for a particular agency by id

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/routes-for-agency/1.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="listWithReferences">
        <references/>
        <list>
          <route>
            <id>1_1</id>
            <shortName>1</shortName>
            <description>kinnear</description>
            <type>3</type>
            <url>http://metro.kingcounty.gov/tops/bus/schedules/s001_0_.html</url>
            <agencyId>1</agencyId>
          </route>
          ...
        </list>
        <limitExceeded>false</limitExceeded>
      </data>
    </response>

## Request Parameters

* id - the id of the agency, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/routes-for-agency/[ID GOES HERE].xml`

## Response

Returns a list of all route ids for routes served by the specified agency.  See the full description for the [`<route/>` element](../elements/route.html).
