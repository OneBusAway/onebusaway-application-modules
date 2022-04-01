# The OneBusAway RESTful API

OneBusAway provides a RESTful (REpresentational State Transfer) API that allows you access to the same information that
powers the OneBusAway website and mobile tools.  You can use the api to write cool new apps of your own.

## Release Notes

Check out the [Release Notes](release-notes.html) for details about what's changed with the API
    
## API Keys

The following parameter must be included in all API requests:

  * key - your assigned application key

Example:

    /some/api/call.xml?key=YOUR_KEY_HERE

The assigned application key is used to track usage statistics across applications.  API keys can be managed in a number
of ways.      

## Output Format

Supported output formats include JSON and XML.  The output format is determined by the request extension.  For example:

    /some/api/call.xml

will return XML results, while

    /some/api/call.json

will return JSON.  The JSON method all supports a `callback` parameter, which is useful for cross-site scripting access:

    /some/api/call.json?callback=some_function_name

will return:

    some_function_name({"key":value,...})

## Response Element

All responses are wrapped in a response element.

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data>
        <references/>
        ...
      </data>
    </response>

The response element carries the following fields:

* `version` - response version information  
* code - a machine-readable response code with the following semantics:
    * `200` - Success
    * `400` - The request could not be understood due to an invalid request parameter or some other error
    * `401` - The application key is either missing or invalid
    * `404` - The specified resource was not found
    * `500` - A service exception or error occurred while processing the request
* `text` - a human-readable version of the response `code` 
* `currentTime` - current system time on the api server as milliseconds since the unix epoch
* `data` - the response payload
    * `references` see the discussion of references below

## References

The `<references/>` element contains a dictionary of objects referenced by the main result payload.  For elements that
are often repeated in the result payload, the elements are instead included in the `<references/>` section and the
payload will refer to elements by and object id that can be used to lookup the object in the `<references/>` dictionary.

Right now, only a few types of objects will ever appear in the references section: agencies, routes, stops, trips, and
situations.

    <references>
      <agencies>
        <agency>...</agency>
      </agencies>
      <routes>
        <route>...</route>
      </routes>
      <stops>
        <stop>...</stop>
      </stops>
      <trips>
        <trip>...</trip>
      </trips>
      <situations>
        <situation>...</situation>
      </situations>
    </references>

They will always appear in that order, since stops and trips reference routes and routes reference agencies.  If you
are processing the result stream in order, you should always be able to assume that an referenced entity would already
have been included in the references section.

Every API method supports an optional `includeReferences=true|false` parameter that determines if the `<references/>`
section is included in a response.  If you don't need the contents of the `<references/>` section, perhaps because
you've pre-cached all the elements, then setting `includeReferences=false` can be a good way to reduce the response
size.

## Methods

The current list of supported API methods. 

* [agencies-with-coverage](methods/agencies-with-coverage.html) - list all supported agencies along with the center of their coverage area
* [agency](methods/agency.html) - get details for a specific agency
* [arrival-and-departure-for-stop](methods/arrival-and-departure-for-stop.html) - details about a specific arrival/departure at a stop
* [arrivals-and-departures-for-stop](methods/arrivals-and-departures-for-stop.html) - get current arrivals and departures for a stop
* [block](methods/block.html) - get block configuration for a specific block
* [cancel-alarm](methods/cancel-alarm.html) - cancel a registered alarm
* [current-time](methods/current-time.html) - retrieve the current system time
* [register-alarm-for-arrival-and-departure-at-stop](methods/register-alarm-for-arrival-and-departure-at-stop.html) - register an alarm for an arrival-departure event
* [report-problem-with-stop](methods/report-problem-with-stop.html) - submit a user-generated problem for a stop
* [report-problem-with-trip](methods/report-problem-with-trip.html) - submit a user-generated problem for a trip
* [route-ids-for-agency](methods/route-ids-for-agency.html) - get a list of all route ids for an agency
* [route](methods/route.html) - get details for a specific route
* [routes-for-agency](methods/routes-for-agency.html) - get a list of all routes for an agency
* [routes-for-location](methods/routes-for-location.html) - search for routes near a location, optionally by route name
* [schedule-for-route](methods/schedule-for-route.html) - get the full schedule for a route on a particular day
* [schedule-for-stop](methods/schedule-for-stop.html) - get the full schedule for a stop on a particular day
* [shape](methods/shape.html) - get details for a specific shape (polyline drawn on a map)
* [stop-ids-for-agency](methods/stop-ids-for-agency.html) - get a list of all stops for an agency
* [stop](methods/stop.html) - get details for a specific stop
* [stops-for-location](methods/stops-for-location.html) - search for stops near a location, optionally by stop code
* [stops-for-route](methods/stops-for-route.html) - get the set of stops and paths of travel for a particular route
* [trip-details](methods/trip-details.html) - get extended details for a specific trip
* [trip-for-vehicle](methods/trip-for-vehicle.html) - get extended trip details for current trip of a specific transit vehicle
* [trip](methods/trip.html) - get details for a specific trip
* [trips-for-location](methods/trips-for-location.html) - get active trips near a location
* [trips-for-route](methods/trips-for-route.html) - get active trips for a route
* [vehicles-for-agency](methods/vehicles-for-agency.html) - get active vehicles for an agency

(Trip planning is no longer supported, check out the [OpenTripPlanner](http://www.opentripplanner.org/) project instead)

## Common Elements

See more discussion of Version 2 of the API and how element references have changed:

* [agency](elements/agency.html)
* [arrivalAndDeparture](elements/arrival-and-departure.html)
* [blockConfiguration](elements/block-configuration.html)
* [frequency](elements/frequency.html)
* [list](elements/list-result.html)
* [route](elements/route.html)
* [situation](elements/situation.html)
* [stop](elements/stop.html)
* [tripDetails](elements/trip-details.html)
* [tripStatus](elements/trip-status.html)
* [trip](elements/trip.html)
* [vehicleStatus](elements/vehicle-status.html)

## Timestamps

Many API methods return timestamps.  For the most part, a OneBusAway timestamp is a measure of the number of milliseconds
since midnight, January 1, 1970 UTC.

Many API methods also accept a "time" parameter that can be used to query the API at a specific point in time (eg. list all
active service alerts on a particular date).  The semantics of how the time parameter is used by the method is method-specific
but the parameter is parsed in the same way.  You can specify time in two possible forms:

* Millisecond since the epoch: time=1365259214945
* "Human-friendly": time=yyyy-MM-dd_HH-mm-ss

In human-friendly mode, the time will be parsed relative to the timezone where the OBA server is operating.
