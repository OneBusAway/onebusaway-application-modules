[Back to API parent page](../index.html)

# Method: vehicles-for-agency

Search for active vehicles for a particular agency by id.

## Sample Request

http://api.onebusaway.org/api/where/vehicles-for-agency/1.xml?key=TEST

## Sample Response

~~~
<response>
  <version>2</version>
  <code>200</code>
  <text>OK</text>
  <currentTime>1270614730908</currentTime>
  <data class="listWithRangeAndReferences">
    <references>...</references>
    <list>
      <vehicleStatus>...</vehicleStatus>
      <vehicleStatus>...</vehicleStatus>
      <vehicleStatus>...</vehicleStatus>
      ...
    </list>
    <limitExceeded>false</limitExceeded>
    <outOfRange>false</outOfRange>
  </data>
</response>
~~~

## Request Parameters

* id - the id of the agency, encoded directly in the URL:
    * `http://api.onebusaway.org/api/where/vehicles-for-agency/[ID GOES HERE].xml`
* time - by default, the method returns the status of the system right now.  However, the system
  can also be queried at a specific time.  This can be useful for testing.  See [timestamps](../index.html#Timestamps)
  for details on the format of the `time` parameter.

## Response

The response is a list of
[`<vehicleStatus/>` elements](../elements/vehicle-status.html) that captures extended
details about each active vehicle associated with the specified agency.
