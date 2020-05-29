[Back to API parent page](../index.html)

# Method: trip-details

Get extended details for a specific trip

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/trip-details/1_47805860.xml?key=TEST

## Sample Response

~~~
<response>
  <version>2</version>
  <code>200</code>
  <text>OK</text>
  <currentTime>1270614730908</currentTime>
  <data class="entryWithReferences">
    <references>...</references>
    <entry class="tripDetails">
      <tripId>1_12540399</tripId>
      <serviceDate>1271401200000</serviceDate>
      <frequency>...</frequency> 
      <status>...</status>
      <schedule>...</schedule>
    </entry>
  </data>
</response>
~~~

## Request Parameters

* id - the id of the trip, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/trip-details/[ID GOES HERE].xml`
* serviceDate - the service date for the trip as unix-time in ms (optional).  Used to disambiguate different versions of the same trip.  See [Glossary#ServiceDate the glossary entry for service date].
* includeTrip - Can be true/false to determine whether full [`<trip/>`](../elements/trip.html) element is included in the `<references/>` section.  Defaults to true.
* includeSchedule - Can be true/false to determine whether full `<schedule/>` element is included in the `<tripDetails/>` section.  Defaults to true.
* includeStatus - Can be true/false to determine whether the full `<status/>` element is include in the `<tripDetails/>` section.  Defaults to true.
* time - by default, the method returns the status of the system right now.  However, the system
  can also be queried at a specific time.  This can be useful for testing.  See [timestamps](../index.html#Timestamps)
  for details on the format of the `time` parameter.

## Response

The response `<entry/>` element is a
[`<tripDetails/>` element](../elements/trip-details.html) that captures extended
details about a trip.

The status element will indicate whether the trip is scheduled or canceled.
