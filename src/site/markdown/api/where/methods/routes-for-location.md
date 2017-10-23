[Back to API parent page](../index.html)

# Method: routes-for-location

Search for routes near a specific location, optionally by name

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/routes-for-location.xml?key=TEST&amp;lat=47.653435&amp;lon=-122.305641

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="listWithReferences">
        <references>...</references>
        <list>
          <route>...</route>
          <!-- More routes -->
        </list>
        <limitExceeded>true</limitExceeded>
      </data>
    </response>

## Request Parameters

* lat - The latitude coordinate of the search center
* lon - The longitude coordinate of the search center
* radius - The search radius in meters (optional)
* latSpan/lonSpan - An alternative to `radius` to set the search bounding box (optional)
* query	- A specific route short name to search for (optional)

If you just specify a lat,lon search location, the `routes-for-location` method will just return nearby routes.  If you specify an optional `query` parameter, we'll search for nearby routes with the specified route short name.  This is the primary method from going from a user-facing route name like "44" to the actual underlying route id unique to a route for a particular transit agency.

## Response

The `routes-for-location` method returns a [list result](../elements/list-result.html), so see additional documentation on controlling the number of elements returned and interpreting the results.  The list contents are [`<route/>` elements](../elements/route.html).
