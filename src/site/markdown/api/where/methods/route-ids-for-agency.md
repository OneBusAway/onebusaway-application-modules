[Back to API parent page](../index.html)

# Method: route-ids-for-agency

Retrieve the list of all route ids for a particular agency.

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/route-ids-for-agency/40.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="listWithReferences">
        <references/>
        <list>
          <string>40_510</string>
          <string>40_511</string>
          <string>40_513</string>
          <string>...</string>
        </list>
        <limitExceeded>false</limitExceeded>
      </data>
    </response>

## Request Parameters

* id - the id of the agency, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/route-ids-for-agency/[ID GOES HERE].xml?key=TEST`

## Response

Returns a list of all route ids for routes served by the specified agency.  Note that `<route/>` elements for the referenced routes will NOT be included in the `<references/>` section, since there are potentially a large number of routes for an agency.