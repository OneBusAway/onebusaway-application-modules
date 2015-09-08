[Back to API parent page](../index.html)

# Method: trip-for-vehicle

Get extended trip details for a specific transit vehicle.  That is, given a vehicle id for a transit vehicle currently operating in the field, return extended trips details about the current trip for the vehicle.

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/trip-for-vehicle/1_4210.xml?key=TEST

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
      <tripId>1_15456175</tripId>
    </entry>
  </data>
</response>
~~~

## Request Parameters

* id - the id of the vehicle, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/trip-for-vehicle/[ID GOES HERE].xml`
* includeTrip - Can be true/false to determine whether full [`<trip/>` element](../elements/trip.html) is included in the `<references/>` section.  Defaults to false.
* includeSchedule - Can be true/false to determine whether full `<schedule/>` element is included in the `<tripDetails/>` section.  Defaults to fale.
* includeStatus - Can be true/false to determine whether the full `<status/>` element is include in the `<tripDetails/>` section.  Defaults to true.
* time - by default, the method returns the status of the system right now.  However, the system
  can also be queried at a specific time.  This can be useful for testing.  See [timestamps](../index.html#Timestamps)
  for details on the format of the `time` parameter.

## Response

The respone `<entry/>` element is a
[`<tripDetails/>` element](../elements/trip-details.html) that captures extended
details about a trip.
