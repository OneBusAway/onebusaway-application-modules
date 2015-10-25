[Back to API parent page](../index.html)

# Method: stops-ids-for-agency

Retrieve the list of all stops for a particular agency by id

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/stop-ids-for-agency/40.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="listWithReferences">
        <references/>
        <list>
          <string>40_C_1303</string>
          <string>40_C_1305</string>
          <string>40_C_1366</string>
          <string>...</string>
        </list>
        <limitExceeded>false</limitExceeded>
      </data>
    </response>

## Request Parameters

* id - the id of the agency, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/stop-ids-for-agency/[ID GOES HERE].xml`

## Response

Returns a list of all stop ids for stops served by the specified agency.  Note that `<stop/>` elements for the referenced stops will NOT be included in the `<references/>` section, since there are potentially a large number of stops for an agency.