[Back to API parent page](../index.html)

# Method: trips-for-location

Search for active trips near a specific location.

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/trips-for-location.xml?key=TEST&amp;lat=47.653&amp;lon=-122.307&amp;latSpan=0.008&amp;lonSpan=0.008

## Sample Response

~~~
<response>
  <version>2</version>
  <code>200</code>
  <text>OK</text>
  <currentTime>1270614730908</currentTime>
  <data class="listWithReferences">
    <references>...</references>
    <list>
      <tripDetails>...</tripDetails>
      <tripDetails>...</tripDetails>
      ...
    </list>
    <limitExceeded>false</limitExceeded>
  </data>
</response>
~~~

## Request Parameters

* lat - The latitude coordinate of the search center
* lon - The longitude coordinate of the search center
* latSpan/lonSpan - Set the limits of the search bounding box
* includeTrip - Can be true/false to determine whether full [`<trip/>` elements](../elements/trip.html) are included in the `<references/>` section.  Defaults to false.
* includeSchedule - Can be true/false to determine whether full `<schedule/>` elements are included in the `<tripDetails/>` section.  Defaults to false.
* time - by default, the method returns the status of the system right now.  However, the system
  can also be queried at a specific time.  This can be useful for testing.  See [timestamps](../index.html#Timestamps)
  for details on the format of the `time` parameter.

## Response

The response is a list of
[`<tripDetails/>` element](../elements/trip-details.html) that captures extended
details about each active trip.  Active trips are ones where the transit vehicle
is currently located within the search radius.  We use real-time arrival data to
determine the position of transit vehicles when available, otherwise we
determine the location of vehicles from the static schedule.
