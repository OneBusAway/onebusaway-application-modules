[Back to API parent page](../index.html)

# Method: stops-for-location

Search for stops near a specific location, optionally by stop code

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/stops-for-location.xml?key=TEST&amp;lat=47.653435&amp;lon=-122.305641

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="listWithReferences">
        <references>...</references>
        <list>
          <stop>...</stop>
          <!-- More stops -->
        </list>
        <limitExceeded>true</limitExceeded>
        <outOfRange>false</outOfRange>
      </data>
    </response>

## Request Parameters

* lat - The latitude coordinate of the search center
* lon - The longitude coordinate of the search center
* radius - The search radius in meters (optional)
* latSpan/lonSpan - An alternative to `radius` to set the search bounding box (optional)
* query	- A specific stop code to search for (optional)

If you just specify a lat,lon search location, the `stops-for-location` method will just return nearby stops.  If you specify an optional `query` parameter, we'll search for nearby stops with the specified code.  This is the primary method from going from a user-facing stop code like "75403" to the actual underlying stop id unique to a stop for a particular transit agency.

## Response

The `stops-for-location` method returns a [list result](../elements/list-result.html), so see additional documentation on controlling the number of elements returned and interpreting the results.  The list contents are `<stop/>` elements, so see details about the various properties of the [`<stop/>` element](../elements/stop.html).
