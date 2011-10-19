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

## Methods

The current list of supported API methods.  Methods that are subject to changed are marked <font color="red">BETA</font>.

* [agencies-with-coverage](methods/agencies-with-coverage.html) - list all supported agencies along with the center of their coverage area
* [agency](methods/agency.html) - get details for a specific agency
* [arrival-and-departure-for-stop](methods/arrival-and-departure-for-stop.html) - details about a specific arrival/departure at a stop
* [arrivals-and-departures-for-stop](methods/arrivals-and-departures-for-stop.html) - get current arrivals and departures for a stop
* [cancel-alarm](methods/cancel-alarm.html) - cancel a registered alarm
* [current-time](methods/current-time.html) - retrieve the current system time
* [plan-trip](methods/plan-trip.html) - plan a trip <font color="red">BETA</font>
* [register-alarm-for-arrival-and-departure-at-stop](methods/register-alarm-for-arrival-and-departure-at-stop.html) - register an alarm for an arrival-departure event
* [route-ids-for-agency](methods/route-ids-for-agency.html) - get a list of all route ids for an agency
* [route](methods/route.html) - get details for a specific route
* [routes-for-agency](methods/routes-for-agency.html) - get a list of all routes for an agency
* [routes-for-location](methods/routes-for-location.html) - search for routes near a location, optionally by route name
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

## Common Elements

See more discussion of [OneBusAwayRestApi_Version2 Version 2] of the api and how element references have changed:

* [agency](elements/agency.html)
* [arrivalAndDeparture](elements/arrival-and-departure.html)
* [blockConfiguration](elements/block-configuration.html)
* [frequency](elements/frequency.html)
* [list](elements/list-result.html)
* [route](elements/route.html)
* [situation](elements/situation.html)
* [stop](elements/stop.html)
* [tripStatus](elements/trip-status.html)
* [trip](elements/trip.html)