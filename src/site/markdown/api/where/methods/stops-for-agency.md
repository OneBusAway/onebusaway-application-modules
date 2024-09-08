[Back to API parent page](../index.html)

# Method: stops-for-agency

Retrieve the list of all stops for a particular agency by id

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/stops-for-agency/40.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="listWithReferences">
        <references/>
        <list>
          <stop>...</stop>
          <!-- More stops -->
        </list>
        <limitExceeded>false</limitExceeded>
      </data>
    </response>

## Request Parameters

* id - the id of the agency, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/stops-for-agency/[ID GOES HERE].xml`

## Response

Returns a list of all stops served by the specified agency.