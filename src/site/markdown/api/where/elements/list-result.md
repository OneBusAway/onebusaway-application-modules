[Back to API parent page](../index.html)

# The List Result

Many API methods return a list of elements as their response.  There are a couple of common request parameters and response elements to consider for these methods.

## Request

Many list response methods accept a `maxCount` parameter that controls the maximum number of elements returned in a response.  Each method has a default value for `maxCount`, which you can override to get more or less results.  Note that most methods also have an upper limit on the number of results they will return, no matter how large you set `maxCount`.

## Response

The response is composed of two elements.  The first, `<list/>`, is the actual list of elements returned by the method.  The type of element for individual list entries is determined by the method.

Additionally, the list response will have a `<limitExceeded/>` element, which a single true or false value.  This value will be true if the number of elements that could have been potentially returned exceeded the limit sent by `maxCount`, either explicitly or the default value.

Finally, some responses will also include an `<outOfRange/>` element, which will indicate if the search request was made outside the current areas of service for OneBusAway (see [agencies-with-coverage](../methods/agencies-with-coverage.html)).  The following geographic query methods currently include the `<outOfRange/>` element:

* [routes-for-location](../methods/routes-for-location.html)
* [stops-for-location](../methods/stops-for-location.html)
* [trips-for-location](../methods/trips-for-location.html)
